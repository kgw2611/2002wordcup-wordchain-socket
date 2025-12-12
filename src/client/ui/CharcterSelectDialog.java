package client.ui;

import client.resource.Fonts;
import client.resource.Images;
import client.viewModel.MainViewModel;

import javax.swing.*;
import java.awt.*;

/*
    캐릭터 선택 창을 띄우는 다이얼로그
    1. 대기방의 [캐릭터 선택] 버튼을 클릭 시 동작
    2. 캐릭터 이미지가 띄워지고, 그 중 하나를 클릭해 선택 가능
*/
public class CharcterSelectDialog extends JDialog {

    private String selectedId = null;  // 현재 선택된 캐릭터
    private JPanel grid;               // 선택 패널들을 담는 Grid

    public CharcterSelectDialog(JFrame parent, MainViewModel viewModel) {
        super(parent, "캐릭터 선택", true);

        setSize(520, 380);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 240, 230));

        grid = new JPanel(new GridLayout(1, 3, 20, 10));
        grid.setBackground(new Color(245, 240, 230));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        String[] characterIds = {"DEFAULT", "TYPE1", "TYPE2"};

        for (String id : characterIds) {

            // 캐릭터 이미지
            ImageIcon icon = Images.getCharacter(id);
            Image small = icon.getImage().getScaledInstance(130, 170, Image.SCALE_SMOOTH);
            Image large = icon.getImage().getScaledInstance(155, 200, Image.SCALE_SMOOTH);

            ImageIcon smallIcon = new ImageIcon(small);
            ImageIcon largeIcon = new ImageIcon(large);

            // 패널 생성
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(250, 245, 235));
            panel.setBorder(BorderFactory.createLineBorder(new Color(180, 160, 130), 2));
            panel.putClientProperty("id", id);         // ✔ 여기에 캐릭터 ID 저장
            panel.putClientProperty("small", smallIcon);
            panel.putClientProperty("large", largeIcon);

            JLabel imgLabel = new JLabel(smallIcon);
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(imgLabel, BorderLayout.CENTER);

            // 클릭 이벤트
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectedId = id;
                    updateSelectionUI();
                }
            });

            grid.add(panel);
        }

        // 하단 버튼 영역
        JButton okButton = new JButton("선택 완료");
        okButton.setFont(Fonts.NORMAL.deriveFont(Font.BOLD, 18f));
        okButton.setBackground(new Color(220, 170, 120));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);

        okButton.addActionListener(e -> {
            if (selectedId != null) {
                viewModel.setSelectedCharacter(selectedId);
            }
            dispose();
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(245, 240, 230));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        bottom.add(okButton);

        add(grid, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setResizable(false);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    // 패널 선택 UI 업데이트 (강조 + 확대 효과)
    private void updateSelectionUI() {

        for (Component comp : grid.getComponents()) {

            JPanel panel = (JPanel) comp;
            String id = (String) panel.getClientProperty("id");

            JLabel imgLabel = (JLabel) panel.getComponent(0);
            ImageIcon small = (ImageIcon) panel.getClientProperty("small");
            ImageIcon large = (ImageIcon) panel.getClientProperty("large");

            if (id.equals(selectedId)) {

                // 선택된 패널 스타일
                panel.setBackground(new Color(255, 210, 140));
                panel.setBorder(BorderFactory.createLineBorder(new Color(200, 120, 40), 4));

                // 이미지 확대
                imgLabel.setIcon(large);

            } else {

                // 일반 패널 스타일
                panel.setBackground(new Color(250, 245, 235));
                panel.setBorder(BorderFactory.createLineBorder(new Color(180, 160, 130), 2));

                // 원래 크기
                imgLabel.setIcon(small);
            }
        }

        grid.revalidate();
        grid.repaint();
    }
}
