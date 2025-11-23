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

    public PlayerCard(String name, ImageIcon rawIcon) {

        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(180, 240));

        // ---- 턴 표시 화살표 ----
        arrowLabel = new JLabel("▼", SwingConstants.CENTER);
        arrowLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        arrowLabel.setForeground(Color.RED);
        arrowLabel.setVisible(false);
        add(arrowLabel, BorderLayout.NORTH);

        // ---- 캐릭터 이미지 ----
        ImageIcon resized = resize(rawIcon, 120, 120);
        imageLabel = new JLabel(resized);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
            setBorder(null);
            setOpaque(false);
            return;
        }

        arrowLabel.setVisible(turn);

        if (turn) {
            // 내 차례일 때 시각적 강조
            setBorder(BorderFactory.createLineBorder(new Color(255, 80, 80), 4));
            setBackground(new Color(255, 240, 240));
            setOpaque(true);
            nameLabel.setForeground(Color.RED);
        }
        else
        {
            // 평소 상태
            setBorder(null);
            setBackground(null);
            setOpaque(false);
            nameLabel.setForeground(Color.BLACK);
        }
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
        imageLabel.setLocation(imageLabel.getX(), 10 + jumpOffset);
    }
}
