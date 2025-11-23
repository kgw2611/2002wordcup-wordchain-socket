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

    public void setOnGameFinished(Runnable onGameFinished) {
        this.onGameFinished = onGameFinished;
    }

    private String myName;
    private TimerBar timerBar;
    private WordBoard wordBoard;
    private JTextField input;
    private boolean myTurnNow = false;

    public ClientGame(String myName, GameController gameController, List<PlayerInfo> players) {

        this.myName = myName;
        this.gameController = gameController;

        setTitle("끝말잇기 데스매치");
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 타이머
        timerBar = new TimerBar(gameController.getLevelTime(), () -> {
            gameController.sendTimeout();
        });
        add(timerBar, BorderLayout.NORTH);

        // 칠판
        wordBoard = new WordBoard();
        add(wordBoard, BorderLayout.CENTER);

        // 입력창
        JPanel inputPanel = new JPanel(new BorderLayout());
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

        // 플레이어 패널(가로 4명)
        JPanel playersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        playersPanel.setOpaque(false);

        for (PlayerInfo p : players) {
            ImageIcon icon = new ImageIcon(
                    getClass().getClassLoader().getResource("client/resource/PlayerImage.png")
            );

            PlayerCard card = new PlayerCard(p.getName(), icon);
            playerCards.add(card);
            playersPanel.add(card);
        }

        // input + players 합친 영역
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputPanel, BorderLayout.NORTH);
        bottom.add(playersPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        // ====== 서버 이벤트 ======

        gameController.setOnWord(wordBoard::setWord);

        gameController.setOnTurn(name -> {

            //if (!gameController.isCountdownDone()) return;

            for (PlayerCard card : playerCards) card.setTurn(false);

            boolean isMyTurn = name.equals(myName);

            for (PlayerCard card : playerCards) {
                if (card.getPlayerName().equals(name) && !card.isDead()) {
                    card.setTurn(true);
                    break;
                }
            }

            setInputEnabled(isMyTurn);
            if(isMyTurn) {
                timerBar.start(gameController.getLevelTime());
            }
            else {
                timerBar.stop();
            }
        });

        gameController.setOnLifeLost(name -> {
            for (PlayerCard card : playerCards) {
                if (card.getPlayerName().equals(name)) {
                    card.loseLife();
                }
            }
            checkWinner();
        });

        gameController.setOnGameOver(winner -> {
            JOptionPane.showMessageDialog(this, winner + "님 승리!");
            dispose();

            if(onGameFinished != null) onGameFinished.run();
        });

        gameController.setOnInvalidWord(data -> {
            // data 형식: "플레이어이름:단어"
            String[] parts = data.split(":", 2);
            String playerName = parts[0];
            String word = (parts.length > 1) ? parts[1] : "";

            // 보드에 오답 표시
            wordBoard.showInvalidWord(playerName, word);

            // 내가 낸 오답이면 입력창에 에러 효과
            if (playerName.equals(myName)) {
                flashInputError();
            }
        });

        // 화면 진입 후 카운트다운 시작!
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

            } catch (Exception ignored) {}
        }).start();
    }

    private void setInputEnabled(boolean enabled) {
        myTurnNow = enabled;
        input.setEnabled(enabled);
        input.setBackground(enabled ? Color.WHITE : new Color(230, 230, 230));
        if (!enabled) {
            input.setText("");
        }
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
        input.setBackground(new Color(255, 220, 220)); // 연한 빨간 배경
        input.requestFocus();

        Timer t = new Timer(200, e -> input.setBackground(originalBg));
        t.setRepeats(false);
        t.start();
    }
}
