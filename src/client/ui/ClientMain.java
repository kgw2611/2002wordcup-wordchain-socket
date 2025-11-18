package client.ui;

import client.controller.RoomController;
import client.resource.*;
import client.viewModel.MainViewModel;

import javax.swing.*;
import java.awt.*;

public class ClientMain extends JFrame {

    private JTextField nameField;
    private JTextField portField;
    private JButton enterBtn;

    private MainViewModel viewModel;

    public ClientMain() {
        viewModel = new MainViewModel();

        setTitle("끝말잇기");
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(Images.MAIN_BG.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        add(backgroundPanel);


        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Colors.BACKGROUND);
                g2.fillRoundRect(15, 15, getWidth() - 30, getHeight() - 30, 35, 35);

                g2.setColor(Colors.BORDER);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(15, 15, getWidth() - 30, getHeight() - 30, 35, 35);
            }
        };

        rightPanel.setPreferredSize(new Dimension(330, 0));
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        backgroundPanel.add(rightPanel, BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 15, 20, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("이름");
        nameLabel.setFont(Fonts.LABEL);
        nameLabel.setForeground(Colors.TEXT_DARK);
        rightPanel.add(nameLabel, gbc);


        gbc.gridy = 1;
        nameField = new JTextField();
        Styles.styleTextField(nameField);
        rightPanel.add(nameField, gbc);


        gbc.gridy = 2;
        JLabel portLabel = new JLabel("포트");
        portLabel.setFont(Fonts.LABEL);
        portLabel.setForeground(Colors.TEXT_DARK);
        rightPanel.add(portLabel, gbc);


        gbc.gridy = 3;
        portField = new JTextField();
        Styles.styleTextField(portField);
        rightPanel.add(portField, gbc);


        gbc.gridy = 4;
        enterBtn = new JButton("대기방 입장");
        Styles.styleButton(enterBtn);
        rightPanel.add(enterBtn, gbc);

        enterBtn.addActionListener(e -> onEnterRoom());
    }

    private void onEnterRoom() {
        String name = nameField.getText();
        String port = portField.getText();

        if (!viewModel.validateName(name)) {
            JOptionPane.showMessageDialog(this, "이름을 입력해주세요!");
            return;
        }
        if (!viewModel.validatePort(port)) {
            JOptionPane.showMessageDialog(this, "포트를 올바르게 입력해주세요! (1024~65535)");
            return;
        }

        viewModel.setPlayerName(name);
        int portNum = Integer.parseInt(port);

        RoomController controller = new RoomController(viewModel, portNum);

        //  connect() 성공 여부 체크
        if (!controller.connect()) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n포트 번호를 확인해주세요!",
                    "연결 실패",
                    JOptionPane.ERROR_MESSAGE);

            // 텍스트필드 강조 효과
            portField.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            return;
        }

        // 성공하면 대기방으로 이동
        ClientRoom room = new ClientRoom(viewModel, controller);
        room.setVisible(true);
        dispose();
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientMain().setVisible(true));
    }
}
