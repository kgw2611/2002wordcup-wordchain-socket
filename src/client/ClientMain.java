package client;

import java.awt.EventQueue;
import java.awt.event.*;
import java.util.regex.Pattern;
import javax.swing.*;

public class ClientMain extends JFrame {

    private JPanel contentPane;
    private JTextField txtUserName;
    private JTextField txtIpAddress;
    private JTextField txtPortNumber;
    private JButton btnConnect;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ClientMain frame = new ClientMain();
                frame.setVisible(true);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    // GUI
    public ClientMain() {
        setTitle("끝말잇기 클라이언트");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 280, 340);
        contentPane = new JPanel();
        contentPane.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblUser = new JLabel("User Name");
        lblUser.setBounds(12, 30, 82, 28);
        contentPane.add(lblUser);

        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(110, 30, 140, 28);
        contentPane.add(txtUserName);

        JLabel lblIp = new JLabel("IP Address");
        lblIp.setBounds(12, 90, 82, 28);
        contentPane.add(lblIp);

        txtIpAddress = new JTextField("127.0.0.1");
        txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        txtIpAddress.setBounds(110, 90, 140, 28);
        contentPane.add(txtIpAddress);

        JLabel lblPort = new JLabel("Port Number");
        lblPort.setBounds(12, 150, 82, 28);
        contentPane.add(lblPort);

        txtPortNumber = new JTextField("30000");
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setBounds(110, 150, 140, 28);
        contentPane.add(txtPortNumber);

        btnConnect = new JButton("Connect");
        btnConnect.setBounds(12, 220, 238, 40);
        contentPane.add(btnConnect);

        MyAction action = new MyAction();
        btnConnect.addActionListener(action);
        txtUserName.addActionListener(action);
        txtIpAddress.addActionListener(action);
        txtPortNumber.addActionListener(action);
    }

    // 연결 버튼 클릭 시
    class MyAction implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            String username = txtUserName.getText().trim();
            String host = txtIpAddress.getText().trim();
            String portStr = txtPortNumber.getText().trim();

            int port;
            try {
                port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ClientMain.this,
                        "포트 번호는 1~65535 사이의 정수여야 합니다.",
                        "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!validateIp(host)) {
                JOptionPane.showMessageDialog(ClientMain.this,
                        "IP 주소 형식이 올바르지 않습니다.",
                        "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
    }

    private boolean validateIp(String ip) {
        if ("localhost".equalsIgnoreCase(ip)) return true;
        return Pattern.matches(
                "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$",
                ip
        );
    }
}
