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
        wordLabel.setText(word);
    }
}
