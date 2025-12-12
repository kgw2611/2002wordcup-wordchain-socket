package client.ui;

import client.controller.RoomController;
import client.resource.*;
import client.viewModel.MainViewModel;

import javax.swing.*;
import java.awt.*;

/*
    게임 첫 화면 프로그램
    1. 이름 / 포트 입력
    2. 올바른 포트라면 해당 포트의 게임 대기방 이동
    3. 배경 화면 이미지, 폰트 처리
*/
public class ClientMain extends JFrame {

    private JTextField nameField;
    private JTextField portField;
    private JButton enterBtn;

    private MainViewModel viewModel;

    public ClientMain() {
        MusicPlayer.playLoop("src/client/resource/BackgroundMusic.mp3");
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
        
        // 이름 라벨
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("이름");
        nameLabel.setFont(Fonts.LABEL);           // 폰트 변경
        nameLabel.setForeground(Colors.TEXT_DARK);
        rightPanel.add(nameLabel, gbc);
        
        // 이름 입력창
        gbc.gridy = 1;
        nameField = new JTextField();
        Styles.styleTextField(nameField);         // 내부에서 Fonts.NORMAL 쓰면 자동 적용됨
        rightPanel.add(nameField, gbc);
        
        // 포트 라벨
        gbc.gridy = 2;
        JLabel portLabel = new JLabel("포트");
        portLabel.setFont(Fonts.LABEL);           // 폰트 변경
        portLabel.setForeground(Colors.TEXT_DARK);
        rightPanel.add(portLabel, gbc);

        // 포트 입력창
        gbc.gridy = 3;
        portField = new JTextField();
        Styles.styleTextField(portField);         // 내부에서 Fonts.NORMAL 적용
        rightPanel.add(portField, gbc);

        // 대기방 입장 버튼
        gbc.gridy = 4;
        enterBtn = new JButton("대기방 입장");
        Styles.styleButton(enterBtn);             // 내부에서 Fonts.BUTTON 적용
        rightPanel.add(enterBtn, gbc);

        enterBtn.setFont(Fonts.BUTTON);           // 🔥 혹시 styleButton에서 폰트 안 넣으면 여기도 적용

        enterBtn.addActionListener(e -> onEnterRoom());
    }

    // 실제 소켓 연결 함수
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

        if (!controller.connect()) {
            JOptionPane.showMessageDialog(this,
                    "서버에 연결할 수 없습니다.\n포트 번호를 확인해주세요!",
                    "연결 실패",
                    JOptionPane.ERROR_MESSAGE);

            portField.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            return;
        }

        ClientRoom room = new ClientRoom(viewModel, controller);
        room.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientMain().setVisible(true));
    }
}
