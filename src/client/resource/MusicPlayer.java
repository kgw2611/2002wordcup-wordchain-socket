package client.resource;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class MusicPlayer {

    private static Player player;
    private static boolean running = false;

    // 무한반복 재생
    public static void playLoop(String filepath) {

        if (running) return;  // 이미 재생중이면 무시
        running = true;

        new Thread(() -> {
            while (running) {
                try {
                    BufferedInputStream buffer =
                            new BufferedInputStream(new FileInputStream(filepath));

                    player = new Player(buffer);
                    player.play(); // 끝나면 while문 반복 → 자연스러운 loop
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 정지
    public static void stop() {
        try {
            running = false;
            if (player != null) player.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
