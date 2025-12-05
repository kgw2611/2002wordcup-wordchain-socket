package client.ui.gamePanel;

import client.resource.Fonts;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class WordHistoryPanel extends JPanel {

    private static final int MAX_HISTORY = 5;
    private final Deque<String> words = new ArrayDeque<>();

    public WordHistoryPanel() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    }

    public void addWord(String word) {
        if (word == null || word.isBlank()) return;

        words.addFirst(word.trim());
        while (words.size() > MAX_HISTORY) words.removeLast();

        rebuildCards();
    }

    private void rebuildCards() {
        removeAll();

        boolean first = true;
        for (String w : words) {
            add(createCard(w, first));  // 첫 번째 단어만 강조
            first = false;
        }

        revalidate();
        repaint();
    }

    private JPanel createCard(String word, boolean isLatest) {

        Color bgColor = isLatest
                ? new Color(255, 245, 200)   // ★ 최신 단어 강조 배경
                : new Color(255, 252, 240);

        Color borderColor = isLatest
                ? new Color(250, 180, 70)    // ★ 최신 단어 강조 테두리
                : new Color(240, 170, 90);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // 배경
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);

                // 테두리
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(2.8f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 20, 20);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(140, 50));
        card.setLayout(new BorderLayout());

        JLabel label = new JLabel(word, SwingConstants.CENTER);
        label.setFont(isLatest ? Fonts.TITLE.deriveFont(22f) : Fonts.NORMAL.deriveFont(20f)); // ★ 글씨도 약간 강조
        label.setForeground(isLatest ? new Color(50, 30, 0) : new Color(60, 60, 60));

        card.add(label, BorderLayout.CENTER);

        return card;
    }
}
