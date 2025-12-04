package client.ui;

import client.resource.Fonts;
import client.resource.Images;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankingDialog extends JDialog {

    public RankingDialog(JFrame parent, List<String> ranks) {
        super(parent, "게임 순위", true);

        setSize(520, 650);
        setLocationRelativeTo(parent);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(250, 245, 235));
        add(container);

        // =======================
        //        타이틀
        // =======================
        JLabel title = new JLabel("게임 순위", SwingConstants.CENTER);
        title.setFont(Fonts.TITLE.deriveFont(45f));
        title.setForeground(new Color(90, 60, 40));
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 30, 0));
        container.add(title, BorderLayout.NORTH);

        // ====== 메인 랭킹 리스트 ======
        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

// 리스트 패널 래퍼
        JPanel listWrapper = new JPanel(new BorderLayout());  // ← 수정됨!
        listWrapper.setOpaque(false);
        listWrapper.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        listWrapper.add(listPanel, BorderLayout.CENTER);

// ★★★ 스크롤 추가 ★★★
        JScrollPane scroll = new JScrollPane(listWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

// 🚫 가로 스크롤 완전 제거
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

// 세로스크롤만
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

// 스크롤바 디자인
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 160, 130);
                this.trackColor = new Color(240, 235, 220);
            }
        });

// 중앙에 삽입
        container.add(scroll, BorderLayout.CENTER);



        // 💡 사용자 수만큼만 추가!
        for (int i = 0; i < ranks.size(); i++) {
            listPanel.add(makeRankRow(i + 1, ranks.get(i)));
            listPanel.add(Box.createVerticalStrut(22));
        }

        // =======================
        //      닫기 버튼
        // =======================
        JButton btnLobby = new JButton("대기방으로 이동");
        btnLobby.setFont(Fonts.NORMAL.deriveFont(20f));
        btnLobby.setBackground(new Color(180, 150, 110));  // 기존 스타일 유지
        btnLobby.setForeground(Color.WHITE);
        btnLobby.setFocusPainted(false);
        btnLobby.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnLobby.addActionListener(e -> dispose());

        JButton btnExit = new JButton("게임 종료");
        btnExit.setFont(Fonts.NORMAL.deriveFont(20f));
        btnExit.setBackground(new Color(200, 90, 70));  // 종료는 경고 느낌의 레드/브라운 톤
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnExit.addActionListener(e -> System.exit(0));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        bottom.add(btnLobby);
        bottom.add(Box.createHorizontalStrut(20)); // 버튼 간 간격
        bottom.add(btnExit);

        container.add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ==========================
    //      한 줄 랭킹 패널
    // ==========================
    private JPanel makeRankRow(int rank, String name) {

        // 💡 배경색 통일 (중앙 UI와 어울리는 톤)
        Color rowColor = new Color(255, 250, 240);

        RoundedPanel row = new RoundedPanel(rowColor);
        row.setPreferredSize(new Dimension(450, 110));

        // ===== 메달 영역 =====
        JLabel medal = new JLabel("", SwingConstants.CENTER);
        medal.setPreferredSize(new Dimension(130, 110));

        Icon icon;
        if (rank == 1) icon = Images.First;
        else if (rank == 2) icon = Images.Second;
        else if (rank == 3) icon = Images.Third;
        else icon = Images.Loser;

        // 이미지 크기 조정
        Image originalImage = ((ImageIcon) icon).getImage();
        Image scaled = originalImage.getScaledInstance(85, 85, Image.SCALE_SMOOTH);
        medal.setIcon(new ImageIcon(scaled));

        JPanel medalWrap = new JPanel(new GridBagLayout());
        medalWrap.setOpaque(false);
        medalWrap.add(medal);

        row.add(medalWrap, BorderLayout.WEST);

        // ===== 이름 =====
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(Fonts.TITLE.deriveFont(32f));
        nameLabel.setForeground(new Color(80, 50, 40));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        row.add(nameLabel, BorderLayout.CENTER);

        return row;
    }

    // ==========================
    //       둥근 패널
    // ==========================
    private static class RoundedPanel extends JPanel {
        private final Color bg;

        public RoundedPanel(Color bg) {
            this.bg = bg;
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 35;
            int shadowOffset = 4;

            // ===== 그림자 영역 =====
            g2.setColor(new Color(0, 0, 0, 45)); // 은은한 그림자
            g2.fillRoundRect(
                    shadowOffset, shadowOffset,
                    getWidth() - shadowOffset, getHeight() - shadowOffset,
                    arc, arc
            );

            // ===== 메인 배경 =====
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);

            // ===== 테두리 =====
            g2.setColor(new Color(180, 160, 120));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, getWidth() - shadowOffset - 1, getHeight() - shadowOffset - 1, arc, arc);

            g2.dispose();
        }
    }

    /*public static void main(String[] args) {
        // 더미 데이터 생성
        java.util.List<String> dummyRanks = java.util.Arrays.asList("Player1", "Player2", "Player3", "Player4");

        // Swing 컴포넌트는 Event Dispatch Thread(EDT)에서 실행해야 함
        SwingUtilities.invokeLater(() -> {
            // 부모 프레임 없이 null로 실행해도 됨
            new RankingDialog(null, dummyRanks);
        });
    }*/
}
