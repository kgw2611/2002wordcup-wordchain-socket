package client.network;

import javax.swing.*;
import java.io.BufferedReader;
import java.util.function.Consumer;

public class ClientReceiver extends Thread {

    private final BufferedReader reader;
    private final Consumer<String> onMessage;

    public ClientReceiver(BufferedReader reader, Consumer<String> onMessage) {
        this.reader = reader;
        this.onMessage = onMessage;
    }

    @Override
    public void run() {

        try {
            String msg;

            while ((msg = reader.readLine()) != null) {
                final String incoming = msg;

                SwingUtilities.invokeLater(() -> onMessage.accept(incoming));
            }

        } catch (Exception e) {
            System.out.println("서버와 연결 종료됨.");
        }
    }
}
