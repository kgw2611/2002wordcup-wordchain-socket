package serversettings;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
    서버 프로그램
    1. 소켓을 열어 클라이언트와 연결
    2. 클라이언트 핸들러를 통한 클라이언트 관리 및 메시지 프로토콜 처리
    3. 단어 사전 생성 및 파일 로드
    4. 끝말잇기 게임 로직 검증
    5. 대기방 - 게임 - 순위 까지 전체 게임 진행
    6. 유저 준비 상태, 게임 진행 사항 초기화
*/
public class ServerMain {

    private static final int PORT = 8080; // 포트 설정 -> 포트 별로 방 생성
    private static final List<ClientHandler> clients = new ArrayList<>(); // 유저 관리용 컬렉션
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
        System.out.println("Server Started : " + PORT);

        // 사전 파일 Set으로 로딩
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

        // 실제 소켓 열기
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 메시지 브로드캐스트
    public static synchronized void broadcast(String msg) {
        for (ClientHandler c : clients) c.send(msg);
    }

    // 준비 상태 확인
    public static boolean allReady() {
        if (clients.size() < 2) return false;

        for (ClientHandler c : clients)
            if (!c.isReady) return false;
        return true;
    }

    // 생존자 목록 get
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

            resetReady(); // 준비 상태 초기화
            resetGameState(); // 게임 진행 상태 초기화

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

        String next = alive.get(idx);
        broadcast("TURN:" + next);
    }

    // 클라이언트 핸들러
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

                // msg를 통한 프로토콜 검증 및 실행
                while ((msg = reader.readLine()) != null) {

                    // JOIN - 대기방 입장 시 프로토콜
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

                    // CHAT - 대기방 채팅 프로토콜
                    if (msg.startsWith("CHAT:")) {
                        broadcast(msg);
                        continue;
                    }
                    // CHARACTER - 캐릭터 변경 프로토콜
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



                    // READY - 대기방 준비 프로토콜
                    if (msg.startsWith("READY:")) {
                        isReady = !isReady;
                        if(isReady) {
                            broadcast("[SYSTEM] " + playerName + "님이 준비 완료했습니다");
                        }
                        else {
                            broadcast("[SYSTEM] " + playerName + "님이 준비를 취소했습니다.");
                        }

                        broadcastReadyList();

                        // 대기방 인원이 모두 준비했을 때
                        if (allReady()) {
                            broadcast("[SYSTEM] 모든 인원이 준비되었습니다. 3초 후 게임이 시작됩니다.");

                            // 3초 대기 후 게임 시작
                            new Thread(() -> {
                                try {
                                    Thread.sleep(3000); // 3초 대기
                                } catch (InterruptedException ignored) {}

                                // 게임 초기화
                                synchronized (ServerMain.class) {
                                    resetGameState();
                                    gameStarted = true;

                                    List<String> alive = getAlivePlayers();
                                    if (alive.isEmpty()) return;

                                    broadcast("GAME_START");
                                    broadcast("TURN:" + alive.get(0));
                                }
                            }).start();
                        }
                        continue;
                    }


                    // WORD - 게임 내 단어 입력 프로토콜
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

                        // 틀린 단어 처리
                        if (!valid) {
                            broadcast("WORD_INVALID:" + playerName + ":" + word);
                            continue;
                        }

                        // 올바른 단어 처리
                        synchronized (usedWords) { usedWords.add(word); }
                        lastWord = word;

                        broadcast("WORD:" + word);

                        // 레벨업 체크
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

                    // TIMEOUT - 게임 내 입력 시간 초과 프로토콜
                    if (msg.equals("TIMEOUT")) {

                        // 죽은 사람은 제외
                        if (lives.get(playerName) <= 0) continue;

                        int remain = lives.get(playerName) - 1;
                        lives.put(playerName, remain);

                        broadcast("LIFE_LOST:" + playerName);

                        // 시간 초과 → 체인 끊기 (중복 단어가 아닌 아무 단어 입력 가능)
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



                    // WINNER - 게임 승리 판별 프로토콜
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

    // 준비 완료 플레이어 목록 확인용 브로드캐스트 함수
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

    // 게임 종료 시 준비 상태 초기화 함수
    private static void resetReady() {
        for (ClientHandler c : clients) {
            c.isReady = false;
        }
        broadcastReadyList();
    }

    // 이전에 진행된 게임 정보 초기화 함수
    private static synchronized void resetGameState() {
        // 마지막 단어 / 레벨 / 단어 수
        lastWord = null;
        level = 1;
        wordCount = 0;

        // 사용된 단어들 비우기
        synchronized (usedWords) {
            usedWords.clear();
        }

        // 모든 플레이어 목숨을 3으로 리셋
        for (ClientHandler c : clients) {
            lives.put(c.playerName, 3);
        }

        // 턴 초기화
        turnIndex = 0;
    }
}
