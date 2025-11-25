package client.ui;

import client.controller.GameController;
import client.model.PlayerInfo;
import client.ui.gamePanel.PlayerCard;
import client.ui.gamePanel.TimerBar;
import client.ui.gamePanel.WordBoard;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientGame extends JFrame {

    private List<PlayerCard> playerCards = new ArrayList<>();
    private GameController gameController;
    private Runnable onGameFinished;

    private String myName;
    private TimerBar timerBar;
    private WordBoard wordBoard;
    private JTextField input;
    private boolean myTurnNow = false;
    private JLabel levelLabel;

    public ClientGame(String myName, GameController gameController, List<PlayerInfo> players) {

        this.myName = myName;
        this.gameController = gameController;

        setTitle("끝말잇기 데스매치");
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 전체 배경색
        getContentPane().setBackground(new Color(245, 242, 235));

        // ====== 상단 패널 ======
        timerBar = new TimerBar(gameController.getLevelTime(), () -> {
            gameController.sendTimeout();
        });

        levelLabel = new JLabel("레벨 1", SwingConstants.CENTER);
        levelLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        levelLabel.setForeground(new Color(70, 70, 70));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        topPanel.add(levelLabel, BorderLayout.NORTH);
        topPanel.add(timerBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // ====== 중앙 칠판 ======
        // ====== 중앙 칠판 ======
        wordBoard = new WordBoard();

// 가운데 전체를 칠판이 가로로 꽉 채우도록
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
// 좌우 여백만 남기고 꽉 채우기
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        centerWrapper.add(wordBoard, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);


        // ====== 입력창 ======
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);

        input = new JTextField();
        input.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
        setInputEnabled(false);

        input.addActionListener(e -> {
            if (!input.isEnabled()) return;

            String text = input.getText();
            if (!text.isEmpty()) {
                gameController.sendWord(text);
                input.setText("");
            }
        });

        inputPanel.add(input, BorderLayout.CENTER);

        // ====== 플레이어 카드 ======
        JPanel playersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        playersPanel.setOpaque(false);

        for (PlayerInfo p : players) {
            ImageIcon icon = new ImageIcon(
                    getClass().getClassLoader().getResource("client/resource/PlayerImage.png")
            );
            boolean isSelf = p.getName().equals(myName);

            PlayerCard card = new PlayerCard(p.getName(), icon, isSelf);
            playerCards.add(card);
            playersPanel.add(card);
        }

        JLabel playersTitle = new JLabel("플레이어", SwingConstants.LEFT);
        playersTitle.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        playersTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        JPanel playersWrapper = new JPanel(new BorderLayout());
        playersWrapper.setOpaque(false);
        playersWrapper.add(playersTitle, BorderLayout.NORTH);
        playersWrapper.add(playersPanel, BorderLayout.CENTER);

        // ====== 하단 바(배경) ======
        JPanel bottom = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(230, 225, 215),
                        0, h, new Color(245, 240, 230)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(200, 190, 170));
                g2.drawLine(0, 0, w, 0);

                g2.dispose();
            }
        };
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        bottom.add(inputPanel, BorderLayout.NORTH);
        bottom.add(playersWrapper, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);

        // ===== 서버 이벤트 연결 =====

        gameController.setOnWord(wordBoard::setWord);

        gameController.setOnTurn(name -> {

            for (PlayerCard card : playerCards) card.setTurn(false);

            boolean isMyTurn = name.equals(myName);

            for (PlayerCard card : playerCards) {
                if (card.getPlayerName().equals(name) && !card.isDead()) {
                    card.setTurn(true);
                    break;
                }
            }

            setInputEnabled(isMyTurn);

            if (!gameController.isCountdownDone()) {
                if (!isMyTurn) timerBar.stop();
                return;
            }

            if (isMyTurn) timerBar.start(gameController.getLevelTime());
            else timerBar.stop();
        });

        gameController.setOnLifeLost(name -> {
            for (PlayerCard card : playerCards)
                if (card.getPlayerName().equals(name))
                    card.loseLife();

            checkWinner();
        });

        gameController.setOnGameOver(winner -> {
            JOptionPane.showMessageDialog(this, winner + "님 승리!");
            dispose();
            if (onGameFinished != null) onGameFinished.run();
        });

        gameController.setOnInvalidWord(data -> {
            String[] parts = data.split(":", 2);
            String playerName = parts[0];
            String word = (parts.length > 1) ? parts[1] : "";

            wordBoard.showInvalidWord(word);

            if (playerName.equals(myName)) flashInputError();
        });

        gameController.setOnLevelUp(lv -> {

            levelLabel.setText("레벨 " + lv);

            timerBar.stop();

            showLevelUpOverlay(lv, () -> {
                if (myTurnNow && gameController.isCountdownDone()) {
                    timerBar.start(gameController.getLevelTime());
                }
            });
        });

        showCountdownOverlay();
    }


    private void showCountdownOverlay() {
        JWindow overlay = new JWindow(this);
        overlay.setSize(getWidth(), getHeight());
        overlay.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 0, 0, 180));
        overlay.add(panel);

        JLabel text = new JLabel("", SwingConstants.CENTER);
        text.setFont(new Font("맑은 고딕", Font.BOLD, 120));
        text.setForeground(Color.WHITE);
        panel.add(text, BorderLayout.CENTER);

        overlay.setVisible(true);

        new Thread(() -> {
            try {
                for (int i = 3; i >= 1; i--) {
                    int n = i;
                    SwingUtilities.invokeLater(() -> text.setText(String.valueOf(n)));
                    Thread.sleep(1000);
                }

                SwingUtilities.invokeLater(() -> text.setText("START!"));
                Thread.sleep(900);

                overlay.dispose();
                gameController.triggerCountdownFinished();

                if (myTurnNow)
                    timerBar.start(gameController.getLevelTime());

            } catch (Exception ignored) {}
        }).start();
    }

    private void setInputEnabled(boolean enabled) {
        myTurnNow = enabled;
        input.setEnabled(enabled);
        input.setBackground(enabled ? Color.WHITE : new Color(230, 230, 230));
        if (!enabled) input.setText("");
    }

    private void checkWinner() {
        int alive = 0;
        String lastAlive = null;

        for (PlayerCard card : playerCards) {
            if (!card.isDead()) {
                alive++;
                lastAlive = card.getPlayerName();
            }
        }

        if (alive == 1) {
            gameController.sendWinner(lastAlive);
        }
    }

    private void flashInputError() {
        Color originalBg = input.getBackground();
        input.setBackground(new Color(255, 220, 220));
        input.requestFocus();

        Timer t = new Timer(200, e -> input.setBackground(originalBg));
        t.setRepeats(false);
        t.start();
    }

    private void showLevelUpOverlay(int newLevel, Runnable after) {

        JWindow overlay = new JWindow(this);
        overlay.setSize(getWidth(), getHeight());
        overlay.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 0, 0, 180));
        overlay.add(panel);

        JLabel label = new JLabel("LEVEL " + newLevel, SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 120));
        label.setForeground(Color.YELLOW);
        panel.add(label, BorderLayout.CENTER);

        overlay.setVisible(true);

        new Thread(() -> {
            try { Thread.sleep(1500); } catch (Exception ignored) {}
            overlay.dispose();
            if (after != null) after.run();
        }).start();
    }

    public void setOnGameFinished(Runnable onGameFinished) {
        this.onGameFinished = onGameFinished;
    }
}
