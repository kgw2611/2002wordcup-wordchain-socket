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
    private static synchronized void nextTurn() {

        if (!gameStarted) return;

        List<String> alive = getAlivePlayers();

        // 승자 검증
        if (alive.size() == 1) {
            broadcast("GAME_OVER:" + alive.get(0));
            gameStarted = false;
            return;
        }

        turnIndex = (turnIndex + 1) % alive.size();
        broadcast("TURN:" + alive.get(turnIndex));
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
                                broadcast("GAME_START");
                                broadcast("TURN:" + alive.get(0));
                            }).start();
                        }
                        continue;
                    }

                    // WORD
                    if (msg.startsWith("WORD:")) {
                        String word = msg.substring(5).trim();

                        // 사전에 없는 단어
                        if (!isValidWord(word)) {
                            broadcast("WORD_INVALID:" + word);
                            // 턴 유지
                            continue;
                        }

                        // 중복 단어
                        synchronized (usedWords) { // 사용된 단어 사전에 대한 플레이어 임계 처리
                            if (usedWords.contains(word)) {
                                broadcast("WORD_INVALID:" + word);
                                // 턴 유지
                                continue;
                            }
                        }

                        // 끝말잇기 규칙 확인
                        if(lastWord != null) {
                            char prevLastChar = lastWord.charAt(lastWord.length() - 1);
                            char currFirstChar = word.charAt(0);

                            if (prevLastChar != currFirstChar) {
                                broadcast("WORD_INVALID:" + word);
                                continue;
                            }
                        }

                        // 사용된 단어 사전에 등록
                        synchronized (usedWords) {
                            usedWords.add(word);
                        }
                        lastWord = word; // 마지막 단어 업데이트

                        broadcast("WORD:"+ word);
                        nextTurn();
                        continue;
                    }

                    // TIMEOUT → LIFE_LOST 처리
                    if (msg.equals("TIMEOUT")) {

                        int remain = lives.get(playerName) - 1;
                        lives.put(playerName, remain);

                        broadcast("LIFE_LOST:" + playerName);

                        // 죽음 판정
                        if (remain <= 0) {
                            broadcast("[SYSTEM] " + playerName + " 탈락!");
                        }

                        nextTurn();
                        continue;
                    }

                    // WINNER 직접 전달
                    if (msg.startsWith("WINNER:")) {
                        String winner = msg.substring(7);
                        broadcast("GAME_OVER:" + winner);
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
}
