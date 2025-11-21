package client.ui.gamePanel;

import javax.swing.*;
import java.awt.*;

public class WordBoard extends JPanel {

    private JLabel wordLabel;

    public WordBoard() {
        setPreferredSize(new Dimension(800, 120));
        setBackground(new Color(50, 80, 50));
        setBorder(BorderFactory.createLineBorder(new Color(120, 70, 20), 10));

        wordLabel = new JLabel("-", SwingConstants.CENTER);
        wordLabel.setFont(new Font("맑은 고딕", Font.BOLD, 40));
        wordLabel.setForeground(Color.WHITE);

        setLayout(new BorderLayout());
        add(wordLabel, BorderLayout.CENTER);
    }

    public void setWord(String word) {
        wordLabel.setForeground(Color.WHITE);  // 정상은 흰색
        wordLabel.setText(word);
    }

    public void showInvalidWord(String playerName, String word) {
        wordLabel.setForeground(Color.RED);   // 오답은 빨간색
        wordLabel.setText(playerName + word);

        // 흔들리는 애니메이션 효과
        shakeAnimation();
    }

    private void shakeAnimation() {
        Point original = wordLabel.getLocation();
        int shakeDistance = 8;

        Timer timer = new Timer(20, null);
        final int[] count = {0};

        timer.addActionListener(e -> {
            int offset = (count[0] % 2 == 0) ? shakeDistance : -shakeDistance;
            wordLabel.setLocation(original.x + offset, original.y);
            count[0]++;

            if (count[0] > 10) {
                timer.stop();
                wordLabel.setLocation(original);
            }
        });

        timer.start();
    }
}
