package serversettings;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 30000; // 예시 포트
    private static Set<String> dictionary = new HashSet<>();

    public static void main(String[] args) {

        loadDictionary("src/serversettings/MiniDictionary.txt");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버가 포트 " + PORT + "에서 대기 중입니다...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());
                //new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDictionary(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String word;
            while ((word = br.readLine()) != null) {
                word = word.trim();
                if (!word.isEmpty()) {
                    dictionary.add(word);
                }
            }
            System.out.println("사전 로드 완료: " + dictionary.size() + "개 단어");
        } catch (IOException e) {
            System.err.println("사전 파일을 불러올 수 없습니다: " + e.getMessage());
        }
    }

    public static boolean isValidWord(String word) {
        return dictionary.contains(word);
    }

    public static boolean isWordChainValid(String prev, String current) {
        if (prev == null || prev.isEmpty()) return true;
        return prev.charAt(prev.length() - 1) == current.charAt(0);
    }

    class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                String prevWord = null;
                String input;

                while ((input = in.readLine()) != null) {
                    System.out.println("클라이언트 입력: " + input);

                    if (!Server.isValidWord(input)) {
                        out.println("❌ 사전에 없는 단어입니다.");
                    } else if (!Server.isWordChainValid(prevWord, input)) {
                        out.println("❌ 끝말잇기 규칙 위반!");
                    } else {
                        out.println("✅ 통과!");
                        prevWord = input;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
