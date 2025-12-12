package client.ui.gamePanel;

import javax.swing.*;
import java.awt.*;
import client.resource.Fonts;
import client.resource.Images;  // ★ 이미지 불러오기용

/*
    입력한 단어가 보이는 칠판 UI
    1. 칠판 이미지를 붙이고 이미지가 보이도록 JLabel 투명 처리
    2. 올바른 단어는 하얀색 글자로 표시
    3. 불가능한 단어는 붉은 색 글자로 강조 표시
    4. 패널 구석에 마지막 단어 표시
*/
public class WordBoard extends JPanel {

    private JLabel wordLabel;
    private JLabel lastWordLabel;
    private String lastValidWord;

    public WordBoard() {

        // 갈색 테두리는 그대로 유지
        setBorder(BorderFactory.createLineBorder(new Color(120, 70, 20), 10));

        // JLabel 투명하게 설정 → 이미지가 보이도록
        setOpaque(false);

        wordLabel = new JLabel("-", SwingConstants.CENTER);
        wordLabel.setFont(Fonts.TITLE.deriveFont(40f));
        wordLabel.setForeground(Color.WHITE);

        lastWordLabel = new JLabel("마지막 단어: -", SwingConstants.RIGHT);
        lastWordLabel.setFont(Fonts.NORMAL.deriveFont(14f));
        lastWordLabel.setForeground(new Color(230, 230, 230));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(lastWordLabel, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(wordLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 배경 이미지 직접 그림
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Image img = Images.WordBoard.getImage(); // ★ 새로 추가할 이미지 리소스
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }

    public void setWord(String word) {
        wordLabel.setForeground(Color.WHITE);
        wordLabel.setText(word);

        lastValidWord = word;
        lastWordLabel.setText("마지막 단어: " + word);
    }

    public void showInvalidWord(String word) {
        wordLabel.setForeground(Color.RED);
        wordLabel.setText(word);
        shakeAnimation();
    }

    public void resetLastWord() {
        lastValidWord = null;
        lastWordLabel.setText("마지막 단어: -");
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
