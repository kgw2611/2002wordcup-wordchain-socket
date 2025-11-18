package client.network;

import java.io.*;
import java.net.Socket;

public class ClientSocket {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ClientReceiver receiver;

    public boolean connect(String host, int port, java.util.function.Consumer<String> onMessage) {
        try {
            socket = new Socket(host, port);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            receiver = new ClientReceiver(reader, onMessage);
            receiver.start();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void sendMessage(String msg) {
        new ClientSender(writer, msg).start();
    }

    public void close() {
        try { socket.close(); } catch (Exception ignored) {}
    }
}
