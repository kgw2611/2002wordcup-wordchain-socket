package client.ui;

import client.controller.GameController;
import client.controller.RoomController;
import client.model.PlayerInfo;
import client.resource.*;
import client.viewModel.MainViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClientRoom extends JFrame {

    private JTextArea chatArea;
    private JTextField chatInput;


    private JPanel slotPanel;
    private MainViewModel viewModel;
    private RoomController controller;

    private JButton readyBtn;

    private boolean isReady = false;

    public ClientRoom( MainViewModel viewModel, RoomController controller) {
        this.viewModel = viewModel;
        this.controller = controller;


        setTitle("끝말잇기 - 대기방");
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Colors.BACKGROUND);

        JLabel title = new JLabel("대기방", SwingConstants.CENTER);
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        add(title, BorderLayout.NORTH);

        readyBtn = new JButton("준비 완료");
        Styles.styleButton(readyBtn);
        add(readyBtn, BorderLayout.SOUTH);

        readyBtn.addActionListener(e -> {
            isReady = !isReady;
            controller.sendReady();
            
            if(isReady) {
                readyBtn.setText("준비 취소");
            }
            else {
                readyBtn.setText("준비 완료");
            }
        });


        slotPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        slotPanel.setOpaque(false);
        slotPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        for (int i = 0; i < 4; i++) {
            slotPanel.add(makeSlotCell());
        }

        // 자기 자신 플레이어 표시
        PlayerInfo self = viewModel.getPlayer();
        JPanel firstSlot = (JPanel) slotPanel.getComponent(0);
        firstSlot.removeAll();
        firstSlot.add(new PlayerPanel(self, true, this, viewModel), BorderLayout.CENTER);


        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setOpaque(false);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel chatHeader = new JLabel();
        chatHeader.setFont(Fonts.LABEL);
        chatHeader.setForeground(Colors.TEXT_DARK);
        chatHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        chatPanel.add(chatHeader, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(Fonts.NORMAL);
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));

        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setPreferredSize(new Dimension(0, 350));
        chatPanel.add(scroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);

        chatInput = new JTextField();
        chatInput.setFont(Fonts.NORMAL);
        chatInput.setPreferredSize(new Dimension(0, 45));
        chatInput.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));

        JButton sendBtn = new JButton("전송");
        Styles.styleButton(sendBtn);

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> {
            String text = chatInput.getText();
            if (!text.isEmpty()) {
                controller.sendChat(text);
                chatInput.setText("");
            }
        });
        chatInput.addActionListener(e -> {
            String text = chatInput.getText();
            if (!text.isEmpty()) {
                controller.sendChat(text);
                chatInput.setText("");
            }
        });


        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                slotPanel,
                chatPanel
        );

        splitPane.setDividerSize(6);
        splitPane.setEnabled(false);
        splitPane.setResizeWeight(0.45);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.45));

        add(splitPane, BorderLayout.CENTER);




        // 1) 플레이어 리스트 업데이트
        controller.setOnPlayersChanged(players -> updateSlotsFromServer(players));

        // 2) 채팅, 시스템 메시지 수신
        controller.setOnChatReceived(msg -> chatArea.append(msg + "\n"));

        // 3) READY 리스트 수신
        controller.setOnPlayerReady(data -> updateReadyStates(data));

        // 4) 게임 시작 수신
        controller.setOnGameStart(() -> {
            chatArea.append("[SYSTEM] 게임이 시작됩니다!\n");
            startGame();

        });


        controller.joinRoom();
    }
    private void startGame() {

        String myName = viewModel.getPlayer().getName();   // ← 이거 추가
        List<PlayerInfo> players = viewModel.getPlayers();
        GameController gc = controller.getGameController();

        ClientGame game = new ClientGame(myName, gc, players);

        // 게임 종료 시
        game.setOnGameFinished(() -> {
            SwingUtilities.invokeLater(() -> {
                resetReadyUI();
                this.setVisible(true);   // 숨겨놨던 대기방 다시 등장
            });
        });

        this.setVisible(false);      // 대기방은 닫지 말고 숨기기만
        game.setVisible(true);
    }


    private void updateSlotsFromServer(List<PlayerInfo> players) {

        slotPanel.removeAll();
        String myName = viewModel.getPlayer().getName();

        for (int i = 0; i < 4; i++) {

            if (i < players.size()) {
                PlayerInfo p = players.get(i);
                boolean isSelf = p.getName().equals(myName);

                JPanel cell = new JPanel(new BorderLayout());
                cell.setOpaque(false);
                cell.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));
                cell.add(new PlayerPanel(p, isSelf,this, viewModel), BorderLayout.CENTER);

                slotPanel.add(cell);

            } else {
                slotPanel.add(makeSlotCell());
            }
        }

        slotPanel.revalidate();
        slotPanel.repaint();
    }

    // 게임 종료 시 준비 상태 초기화
    private void resetReadyUI() {
        isReady = false;
        readyBtn.setText("준비 완료");

        int count = slotPanel.getComponentCount();
        for (int i = 0; i < count; i++) {
            Component comp = slotPanel.getComponent(i);
            if (comp instanceof JPanel cell && cell.getComponentCount() > 0) {
                Component inner = cell.getComponent(0);
                if (inner instanceof PlayerPanel pp) {
                    pp.updateReadyState(false);
                }
            }
        }
    }



    // 빈 슬롯
    private JPanel makeSlotCell() {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(false);
        cell.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));

        JLabel empty = new JLabel("빈 자리", SwingConstants.CENTER);
        empty.setFont(Fonts.LABEL);
        empty.setForeground(Colors.BORDER);

        cell.add(empty, BorderLayout.CENTER);
        return cell;
    }




    private void updateReadyStates(String data) {


        List<PlayerInfo> players = viewModel.getPlayers();

        String[] arr = data.split(";");

        for (String s : arr) {
            if (s.isEmpty()) continue;

            String[] parts = s.split(",");
            String name = parts[0];
            boolean ready = Boolean.parseBoolean(parts[1]);

            // 슬롯에서 찾기
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).getName().equals(name)) {

                    JPanel cell = (JPanel) slotPanel.getComponent(i);
                    PlayerPanel pp = (PlayerPanel) cell.getComponent(0);

                    pp.updateReadyState(ready);
                }
            }
        }
    }
}
