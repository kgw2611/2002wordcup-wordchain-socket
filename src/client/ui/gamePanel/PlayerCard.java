package client.ui.gamePanel;

import javax.swing.*;
import java.awt.*;

public class PlayerCard extends JPanel {

    private JLabel arrowLabel;
    private JLabel nameLabel;
    private JLabel imageLabel;
    private JLabel[] hearts;

    private ImageIcon fullHeart, emptyHeart;
    private int lives = 3;
    private boolean dead = false;

    private Timer jumpTimer;
    private int jumpOffset = 0;
    private boolean goingUp = true;
    private boolean isSelf = false;   // 자기 자신 여부
    private boolean isCurrentTurn = false;

    // 이미지 리사이즈 공통 함수
    private ImageIcon resize(ImageIcon icon, int w, int h) {
        Image img = icon.getImage();
        Image resized = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
    }

    // 리소스 이미지 로드 함수
    private ImageIcon load(String name) {
        return new ImageIcon(getClass().getClassLoader().getResource("client/resource/" + name));
    }

    public PlayerCard(String name, ImageIcon rawIcon, boolean isSelf) {

        this.isSelf = isSelf;

        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(180, 240));

        // ---- 턴 표시 화살표 ----
        arrowLabel = new JLabel("▼", SwingConstants.CENTER);
        arrowLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        arrowLabel.setForeground(new Color(255, 80, 80));
        arrowLabel.setVisible(false);
        add(arrowLabel, BorderLayout.NORTH);

        // ---- 캐릭터 이미지 ----
        ImageIcon resized = resize(rawIcon, 120, 120);
        imageLabel = new JLabel(resized);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 약간 위로 공간 확보해 주면 리본이랑 안 겹침
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(imageLabel, BorderLayout.CENTER);

        // ==== 하트 + 이름 패널 ====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        // ❤️ 리소스에서 하트 불러오기
        fullHeart = resize(load("full_heart.png"), 28, 28);
        emptyHeart = resize(load("empty_heart.png"), 28, 28);

        JPanel heartPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        heartPanel.setOpaque(false);

        hearts = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            hearts[i] = new JLabel(fullHeart);
            heartPanel.add(hearts[i]);
        }

        bottom.add(nameLabel, BorderLayout.NORTH);
        bottom.add(heartPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        // 점프 애니메이션
        jumpTimer = new Timer(30, e -> animateJump());
    }

    public String getPlayerName() { return nameLabel.getText(); }

    public boolean isDead() { return dead; }

    public void setTurn(boolean turn) {
        if (dead) {
            arrowLabel.setVisible(false);
            isCurrentTurn = false;
            repaint();
            return;
        }

        isCurrentTurn = turn;
        arrowLabel.setVisible(turn);
        repaint();
    }

    // ---- 목숨 감소 ----
    public void loseLife() {
        if (dead) return;

        lives--;
        if (lives >= 0) hearts[lives].setIcon(emptyHeart);

        startJump();

        if (lives <= 0) die();
    }

    private void die() {
        dead = true;
        arrowLabel.setVisible(false);

        // 💀 사망 이미지 불러오기
        ImageIcon deadIcon = load("PlayerDieImage.png");
        deadIcon = resize(deadIcon, 120, 120);
        imageLabel.setIcon(deadIcon);

        nameLabel.setForeground(Color.GRAY);
        repaint();
    }

    private void startJump() {
        jumpOffset = 0;
        goingUp = true;
        jumpTimer.start();
    }

    private void animateJump() {
        if (goingUp) {
            jumpOffset -= 3;
            if (jumpOffset <= -20) goingUp = false;
        } else {
            jumpOffset += 3;
            if (jumpOffset >= 0) {
                jumpOffset = 0;
                jumpTimer.stop();
            }
        }
        // 살짝 y값만 변경
        imageLabel.setLocation(imageLabel.getX(), 10 + jumpOffset);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 카드 배경 색
        Color bg;
        if (dead) {
            bg = new Color(230, 230, 230);
        } else if (isSelf) {
            bg = new Color(255, 245, 210); // 자기 자신: 연한 노랑
        } else {
            bg = new Color(245, 245, 245);
        }

        // 카드 배경
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 30, 30);

        // 그림자 느낌 테두리
        g2.setColor(new Color(200, 200, 200));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 30, 30);

        // 내 턴이면 굵은 테두리 + 이름 빨간색
        if (isCurrentTurn && !dead) {
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(new Color(255, 80, 80));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 30, 30);
            nameLabel.setForeground(new Color(220, 40, 40));
        } else if (!dead) {
            nameLabel.setForeground(Color.BLACK);
        }

        // 자기 자신이면 왼쪽 위에 "YOU" 리본
        if (isSelf && !dead) {
            int rw = 60;
            int rh = 26;
            g2.setColor(new Color(255, 120, 80));
            g2.fillRoundRect(10, 10, rw, rh, 12, 12);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            String txt = "YOU";
            int tx = 10 + (rw - fm.stringWidth(txt)) / 2;
            int ty = 10 + ((rh - fm.getHeight()) / 2 + fm.getAscent());
            g2.drawString(txt, tx, ty);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
