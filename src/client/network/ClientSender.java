package client.network;

import java.io.BufferedWriter;

public class ClientSender extends Thread {

    private BufferedWriter writer;
    private String message;

    public ClientSender(BufferedWriter writer, String message) {
        this.writer = writer;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("메시지 전송 오류");
        }
    }
}
