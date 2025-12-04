package serversettings;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerMain {

    private static final int PORT = 8080;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final Set<String> dictionary = new HashSet<>(370_000); // 단어 사전
    private static final Set<String> usedWords = new HashSet<>(); // 사용된 단어 사전
    private static String lastWord = null;
    private static int level = 1;        // 현재 레벨
    private static int wordCount = 0;

    // 게임 데이터
    private static final Map<String, Integer> lives = new HashMap<>();
    private static int turnIndex = 0;
    private static boolean gameStarted = false;

    public static void main(String[] args) throws IOException {
        System.out.println("🔥 Server Started : " + PORT);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("src/serversettings/MiniDictionary.txt"), StandardCharsets.UTF_8)))
        {
            String line;
            while((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                dictionary.add(line);
            }
            System.out.println("dictionary loaded : " + dictionary.size());
        } catch (IOException e) { e.printStackTrace(); }

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==== 메시지 브로드캐스트 ====
    public static synchronized void broadcast(String msg) {
        for (ClientHandler c : clients) c.send(msg);
    }

    // READY 전체 확인
    public static boolean allReady() {
        if (clients.size() < 2) return false;

        for (ClientHandler c : clients)
            if (!c.isReady) return false;
        return true;
    }

    // 생존자 목록
    public static synchronized List<String> getAlivePlayers() {
        List<String> alive = new ArrayList<>();
        for (ClientHandler c : clients) {
            if (lives.getOrDefault(c.playerName, 0) > 0)
                alive.add(c.playerName);
        }
        return alive;
    }

    // 턴 넘기기
    private static synchronized void nextTurn(String currentPlayer) {

        if (!gameStarted) return;

        List<String> alive = getAlivePlayers();

        // 승자 검증
        if (alive.size() == 1) {
            broadcast("GAME_OVER:" + alive.get(0));
            gameStarted = false;
            resetReady();
            return;
        }

        // currentPlayer가 alive 리스트에서 몇 번째인지 찾기
        int idx = alive.indexOf(currentPlayer);

        if (idx == -1) {
            // 혹시 이미 목록에서 빠졌다면(예외 케이스), 0번부터 시작
            idx = 0;
        } else {
            // 그 다음 사람으로 이동
            idx = (idx + 1) % alive.size();
        }

        broadcast("TURN:" + alive.get(idx));
    }

    // ==== 클라이언트 핸들러 ====
    static class ClientHandler extends Thread {

        Socket socket;
        BufferedReader reader;
        BufferedWriter writer;

        String playerName = "";
        boolean isReady = false;

        ClientHandler(Socket s) { this.socket = s; }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String msg;
                while ((msg = reader.readLine()) != null) {

                    // JOIN
                    if (msg.startsWith("JOIN:")) {
                        isReady = false;
                        broadcastReadyList();

                        playerName = msg.substring(5);
                        System.out.println("JOIN: " + playerName);

                        lives.put(playerName, 3);
                        broadcast("[SYSTEM] " + playerName + " 입장");

                        broadcastPlayerList();
                        continue;
                    }

                    // CHAT
                    if (msg.startsWith("CHAT:")) {
                        broadcast(msg);
                        continue;
                    }
                    // CHARACTER
                    if (msg.startsWith("CHARACTER:")) {
                        // FORMAT: CHARACTER:홍길동:TYPE1
                        String[] sp = msg.split(":");
                        if (sp.length == 3) {
                            String name = sp[1];
                            String type = sp[2];


                            // playerCharacters.put(name, type);

                            // 모든 클라이언트에게 캐릭터 변경 브로드캐스트
                            broadcast("CHARACTER_UPDATE:" + name + ":" + type);
                            System.out.println("[SERVER] CHARACTER_UPDATE " + name + " -> " + type);
                        }
                        continue;
                    }



                    // READY
                    if (msg.startsWith("READY:")) {
                        isReady = !isReady;
                        if(isReady) {
                            broadcast("[SYSTEM] " + playerName + "님이 준비 완료했습니다");
                        }
                        else {
                            broadcast("[SYSTEM] " + playerName + "님이 준비를 취소했습니다.");
                        }

                        broadcastReadyList();

                        if (allReady()) {
                            gameStarted = true;
                            broadcast("[SYSTEM] 모든 인원이 준비되었습니다. 3초 후 게임이 시작됩니다.");

                            // 3초 대기 후 게임 시작
                            new Thread(() -> {
                                try {
                                    Thread.sleep(3000); // 3초 대기
                                } catch (InterruptedException ignored) {}

                                // 사용된 단어 사전 초기화
                                synchronized (usedWords) {
                                    usedWords.clear();
                                }

                                List<String> alive = getAlivePlayers();
                                if (alive.isEmpty()) return;
                                lastWord = null;

                                turnIndex = 0;
                                level = 1;
                                wordCount = 0;
                                broadcast("GAME_START");
                                broadcast("TURN:" + alive.get(0));
                            }).start();
                        }
                        continue;
                    }


                    // WORD

                    if (msg.startsWith("WORD:")) {
                        String word = msg.substring(5).trim();

                        boolean valid = true;

                        // 1) 사전에 없음
                        if (!isValidWord(word)) valid = false;

                        // 2) 중복 단어
                        synchronized (usedWords) {
                            if (usedWords.contains(word)) valid = false;
                        }

                        // 3) 끝말 규칙 불일치
                        if (lastWord != null) {
                            char prev = lastWord.charAt(lastWord.length() - 1);
                            char curr = word.charAt(0);
                            if (prev != curr) valid = false;
                        }

                        // ===== 틀린 단어 처리 =====
                        if (!valid) {
                            broadcast("WORD_INVALID:" + playerName + ":" + word);
                            continue;
                        }

                        // ===== 올바른 단어 처리 =====
                        synchronized (usedWords) { usedWords.add(word); }
                        lastWord = word;

                        broadcast("WORD:" + word);

                        // ===== 레벨업 체크 =====
                        wordCount++;

                        if (wordCount >= 12) {
                            level++;
                            wordCount = 0;

                            // 모든 클라이언트에게 레벨업 알림
                            broadcast("LEVEL_UP:" + level);

                            // 라운드 리셋
                            lastWord = null;

                            // 레벨업 후 → 단어 입력한 사람부터 다시 시작
                            nextTurn(playerName);
                        } else {
                            // 평소처럼 다음 사람에게 턴 넘김
                            nextTurn(playerName);
                        }

                        continue;
                    }



                    // TIMEOUT → LIFE_LOST 처리

                    // TIMEOUT → LIFE_LOST 처리
                    if (msg.equals("TIMEOUT")) {

                        int remain = lives.get(playerName) - 1;
                        lives.put(playerName, remain);

                        broadcast("LIFE_LOST:" + playerName);

                        // ❗ 시간 초과 → 체인 끊기 (새 라운드 느낌)
                        lastWord = null;

                        if (remain <= 0) {
                            // 죽었으면 다음 생존자에게 턴 넘김
                            nextTurn(playerName);
                        } else {
                            // 아직 살아 있으면, 본인부터 새 단어로 다시 시작
                            broadcast("TURN:" + playerName);
                        }
                        continue;
                    }



                    // WINNER 직접 전달
                    if (msg.startsWith("WINNER:")) {
                        String winner = msg.substring(7);
                        broadcast("GAME_OVER:" + winner);

                        gameStarted = false;
                        resetReady();
                    }
                }

            } catch (Exception ignored) {}

            finally {
                try { socket.close(); } catch (Exception ignore) {}
                clients.remove(this);
                lives.remove(playerName);

                broadcast("[SYSTEM] " + playerName + " 퇴장");
                broadcastPlayerList();
            }
        }

        void send(String msg) {
            try {
                writer.write(msg + "\n");
                writer.flush();
            } catch (Exception ignored) {}
        }
    }

    // 플레이어 목록 브로드캐스트
    private static void broadcastPlayerList() {
        StringBuilder sb = new StringBuilder("PLAYER_LIST:");
        for (ClientHandler c : clients) sb.append(c.playerName).append(",");
        broadcast(sb.toString());
    }

    private static void broadcastReadyList() {
        StringBuilder sb = new StringBuilder("PLAYER_READY_LIST:");
        for (ClientHandler c : clients) {
            sb.append(c.playerName)
                    .append(",")
                    .append(c.isReady)
                    .append(";");
        }
        broadcast(sb.toString());
    }

    // 단어 검증 함수
    private static boolean isValidWord(String word) {
        if (word == null) return false;
        word = word.trim();
        if (word.length() < 2) return false; // 1글자 패스
        return dictionary.contains(word);
    }

    private static void resetReady() {
        for (ClientHandler c : clients) {
            c.isReady = false;
        }
        broadcastReadyList();
    }
}
