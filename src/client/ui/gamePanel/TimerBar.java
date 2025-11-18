package client.ui.gamePanel;

import javax.swing.*;
import java.awt.*;

public class TimerBar extends JPanel {

    private JLabel timeLabel;
    private int time;
    private Timer timer;
    private Runnable onTimeout;

    public TimerBar(int sec, Runnable onTimeout) {
        this.time = sec;
        this.onTimeout = onTimeout;

        setPreferredSize(new Dimension(800, 80));
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        timeLabel = new JLabel(sec + "초", SwingConstants.CENTER);
        timeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 36));
        timeLabel.setForeground(Color.RED);

        add(timeLabel, BorderLayout.CENTER);

        timer = new Timer(1000, e -> tick());
    }

    private void tick() {
        time--;
        if (time <= 0) {
            time = 0;
            timer.stop();
            timeLabel.setText("0초");
            if (onTimeout != null) onTimeout.run();
        } else {
            timeLabel.setText(time + "초");
        }
    }

    public void start(int sec) {
        this.time = sec;
        timeLabel.setText(sec + "초");
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
