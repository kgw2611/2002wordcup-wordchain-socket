package client.ui;

import client.model.PlayerInfo;
import client.resource.*;
import client.viewModel.MainViewModel;

import javax.swing.*;
import java.awt.*;

public class PlayerPanel extends JPanel {

    private JLabel nameLabel;
    private JLabel readyBadge;
    private JPanel bg;

    private final boolean isSelf;
    private Color bgColor;
    private JLabel avatar;

    private JFrame parentFrame;
    private client.viewModel.MainViewModel mainViewModel;

    public PlayerPanel(PlayerInfo player, boolean isSelf, JFrame parentFrame, MainViewModel viewModel) {

        this.isSelf = isSelf;
        this.parentFrame = parentFrame;
        this.mainViewModel = viewModel;
        bgColor = isSelf ? new Color(255, 245, 220) : Colors.BACKGROUND;

        setOpaque(false);

        bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            }
        };
        bg.setOpaque(false);
        bg.setLayout(new GridBagLayout());
        setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // 아바타 이미지
        ImageIcon baseIcon = Images.getCharacter(player.getCharacterType());
        Image avatarImg = baseIcon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
        avatar = new JLabel(new ImageIcon(avatarImg));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 2;
        bg.add(avatar, gbc);

        // 이름 라벨
        nameLabel = new JLabel(player.getName());
        nameLabel.setFont(Fonts.LABEL.deriveFont(22f));   // 🔥 FONT 적용
        nameLabel.setForeground(Colors.TEXT_DARK);

        gbc.gridheight = 1;
        gbc.gridx = 1; gbc.gridy = 0;
        bg.add(nameLabel, gbc);

        // ----------------------------
        //  캐릭터 변경 버튼
        // ----------------------------
        JButton changeCharacterBtn = new JButton("캐릭터 변경");

        changeCharacterBtn.setFocusPainted(false);
        changeCharacterBtn.setForeground(new Color(90, 50, 20));
        changeCharacterBtn.setFont(Fonts.NORMAL.deriveFont(Font.BOLD, 15f));  // 🔥 FONT 적용

        changeCharacterBtn.setBackground(new Color(255, 230, 180));
        changeCharacterBtn.setOpaque(true);
        changeCharacterBtn.setBorder(BorderFactory.createLineBorder(new Color(180,130,70), 2));
        changeCharacterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changeCharacterBtn.setPreferredSize(new Dimension(120, 35));

        // 캐릭터 변경 팝업
        changeCharacterBtn.addActionListener(e -> {
            new CharcterSelectDialog(parentFrame, mainViewModel);

            String type = mainViewModel.getSelectedCharacter();
            ImageIcon newIcon = Images.getCharacter(type);
            Image img = newIcon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));

            revalidate();
            repaint();
        });

        gbc.gridx = 1;
        gbc.gridy = 1;
        bg.add(changeCharacterBtn, gbc);

        // READY 배지
        readyBadge = new JLabel("READY");
        readyBadge.setFont(Fonts.NORMAL.deriveFont(Font.BOLD, 16f));  // 🔥 FONT 적용
        readyBadge.setForeground(Color.WHITE);
        readyBadge.setOpaque(true);
        readyBadge.setBackground(Color.RED);
        readyBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        readyBadge.setVisible(false);

        gbc.gridx = 2; gbc.gridy = 0;
        bg.add(readyBadge, gbc);
    }

    public void updateReadyState(boolean ready) {

        if (ready) {
            nameLabel.setForeground(Color.RED);
            readyBadge.setVisible(true);

            // 패널 효과
            bg.setOpaque(true);
            bg.setBackground(new Color(255, 230, 230));
            setBorder(BorderFactory.createLineBorder(Color.RED, 3));

            // 흔들림 애니메이션
            Timer timer = new Timer(10, null);
            int[] count = {0};
            timer.addActionListener(e -> {
                int offset = (count[0] % 4 < 2) ? 3 : -3;
                bg.setLocation(bg.getX() + offset, bg.getY());
                count[0]++;
                if (count[0] > 15) timer.stop();
            });
            timer.start();

        } else {
            nameLabel.setForeground(Colors.TEXT_DARK);
            readyBadge.setVisible(false);
            bg.setOpaque(false);
            setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));
        }

        repaint();
    }

    @Override
    public Dimension getPreferredSize() { return new Dimension(350, 180); }
    @Override
    public Dimension getMaximumSize() { return new Dimension(400, 200); }
}
