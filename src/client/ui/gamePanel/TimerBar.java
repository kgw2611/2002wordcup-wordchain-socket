package client.ui.gamePanel;

import javax.swing.*;
import java.awt.*;
import client.resource.Fonts;

/*
    게임 화면 상단 단어 입력 타이머 바 UI
    1. 시간이 줄어들 수록 바 길이 짧아지도록 그래픽 처리
    2. 시간이 줄어들 수록 바 색 변화 (초록 -> 노랑 -> 빨강)
*/
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

        timeLabel = new JLabel(sec + "초", SwingConstants.CENTER);
        timeLabel.setFont(Fonts.LABEL.deriveFont(26f));   // ★ 폰트 변경
        add(timeLabel, BorderLayout.NORTH);

        bar = new JProgressBar(0, sec);
        bar.setValue(sec);
        bar.setStringPainted(false);
        bar.setForeground( new Color(120, 170, 120));
        bar.setPreferredSize(new Dimension(800, 30));
        add(bar, BorderLayout.CENTER);

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

        if (time <= 3) {
            bar.setForeground(new Color(200, 80, 80));
        } else if (time <= 5) {
            bar.setForeground(new Color(220, 140, 70));
        } else {
            bar.setForeground( new Color(120, 170, 120));
        }
    }

    public void start(int sec) {
        this.time = sec;
        this.maxTime = sec;
        timeLabel.setText(sec + "초");
        bar.setMaximum(sec);
        bar.setValue(sec);
        bar.setForeground( new Color(120, 170, 120));
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
