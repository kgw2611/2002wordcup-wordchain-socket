package client.ui.gamePanel;

import javax.swing.*;
import java.awt.*;

public class TimerBar extends JPanel {

    private int time;
    private int maxTime;
    private Timer timer;
    private Runnable onTimeout;

    private JLabel timeLabel;
    private JProgressBar bar;

    public TimerBar(int sec, Runnable onTimeout) {
        this.time = sec;
        this.maxTime = sec;
        this.onTimeout = onTimeout;

        setPreferredSize(new Dimension(800, 80));
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 타이머 숫자
        timeLabel = new JLabel(sec + "초", SwingConstants.CENTER);
        timeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        add(timeLabel, BorderLayout.NORTH);

        // 바 형태 프로그레스바
        bar = new JProgressBar(0, sec);
        bar.setValue(sec);
        bar.setStringPainted(false);
        bar.setForeground(new Color(0, 200, 0)); // 초록
        bar.setPreferredSize(new Dimension(800, 30));
        add(bar, BorderLayout.CENTER);

        // 1초마다 tick
        timer = new Timer(1000, e -> tick());
    }

    private void tick() {
        time--;
        if (time <= 0) {
            time = 0;
            timer.stop();
            bar.setValue(0);
            timeLabel.setText("0초");
            bar.setForeground(Color.RED);

            if (onTimeout != null) onTimeout.run();
            return;
        }

        timeLabel.setText(time + "초");
        bar.setValue(time);

        // 색상 변경
        if (time <= 3) {
            bar.setForeground(Color.RED);
        } else if (time <= 5) {
            bar.setForeground(new Color(255, 140, 0)); // 주황
        } else {
            bar.setForeground(new Color(0, 200, 0)); // 초록
        }
    }

    public void start(int sec) {
        this.time = sec;
        this.maxTime = sec;
        timeLabel.setText(sec + "초");
        bar.setMaximum(sec);
        bar.setValue(sec);
        bar.setForeground(new Color(0, 200, 0));
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
