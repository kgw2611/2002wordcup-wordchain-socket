package serversettings.network;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerMain {

    private static final int PORT = 8080;
    private static final List<ClientHandler> clients = new ArrayList<>();

    // 게임 데이터
    private static final Map<String, Integer> lives = new HashMap<>();
    private static int turnIndex = 0;
    private static boolean gameStarted = false;

    public static void main(String[] args) {
        System.out.println("🔥 Server Started : 8080");

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
                        broadcast("[SYSTEM] " + playerName + "님이 준비 완료하였습니다");

                        if (allReady()) {
                            gameStarted = true;
                            broadcast("GAME_START");

                            // 첫 턴 지정
                            List<String> alive = getAlivePlayers();
                            turnIndex = 0;
                            broadcast("TURN:" + alive.get(0));
                        }
                        continue;
                    }

                    // WORD
                    if (msg.startsWith("WORD:")) {
                        broadcast(msg);
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
}
