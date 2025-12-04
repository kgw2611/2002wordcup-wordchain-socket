package client.ui;

import client.resource.Images;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankingDialog extends JDialog {

    public RankingDialog(JFrame parent, List<String> ranks) {
        super(parent, "게임 순위", true);

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // ===== 상단 타이틀 =====
        JLabel title = new JLabel("게임 순위", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 36));
        title.setForeground(new Color(90, 70, 50));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ===== 메인 Podium Panel =====
        JPanel podiumPanel = new JPanel(null);
        podiumPanel.setOpaque(false);

        add(podiumPanel, BorderLayout.CENTER);

        // 포디움 높이
        int h1 = 180; // 1등
        int h2 = 140; // 2등
        int h3 = 120; // 3등

        int baseY = 350;

        // ===== 1등 =====
        podiumPanel.add(makeRankBox(
                ranks.size() > 0 ? ranks.get(0) : "",
                1,
                180,
                baseY - h1,
                h1,
                new Color(255, 230, 170)
        ));

        // ===== 2등 =====
        podiumPanel.add(makeRankBox(
                ranks.size() > 1 ? ranks.get(1) : "",
                2,
                50,
                baseY - h2,
                h2,
                new Color(240, 220, 200)
        ));

        // ===== 3등 =====
        podiumPanel.add(makeRankBox(
                ranks.size() > 2 ? ranks.get(2) : "",
                3,
                310,
                baseY - h3,
                h3,
                new Color(240, 220, 200)
        ));

        // 닫기 버튼
        JButton closeBtn = new JButton("닫기");
        closeBtn.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        closeBtn.setBackground(new Color(200, 170, 130));
        closeBtn.setForeground(Color.WHITE);

        closeBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ===== 포디움 박스 만드는 함수 =====
    private JPanel makeRankBox(String name, int rank, int x, int y, int h, Color color) {

        JPanel box = new JPanel(null);
        box.setBounds(x, y, 140, h);
        box.setBackground(color);
        box.setOpaque(true);
        box.setBorder(BorderFactory.createLineBorder(new Color(150, 120, 80), 3));

        // 왕관/순위
        JLabel rankLabel = new JLabel();

        if (rank == 1) rankLabel.setText("👑 1등");
        else if (rank == 2) rankLabel.setText("2등");
        else rankLabel.setText("3등");

        rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rankLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        rankLabel.setBounds(0, 10, 140, 30);
        rankLabel.setForeground(new Color(100, 60, 40));
        box.add(rankLabel);

        // 플레이어 이름
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        nameLabel.setBounds(0, h - 50, 140, 30);
        nameLabel.setForeground(new Color(70, 50, 40));
        box.add(nameLabel);

        return box;
    }
}
