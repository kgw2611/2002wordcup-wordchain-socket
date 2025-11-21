package client.controller;

import client.network.ClientSocket;

import java.util.function.Consumer;

public class GameController {
    private ClientSocket socket;
    private Runnable onGameStart;
    private Consumer<String> onTurn;
    private Consumer<String> onWord;
    private Consumer<Integer> onTimer;
    private Consumer<Integer> onLevelUp;
    private Consumer<String> onGameOver;
    private Consumer<String> onLifeLost;
    private Consumer<String> onInvalidWord;

    private int level = 1;
    private boolean countdownDone = false;

    public boolean isCountdownDone() {
        return countdownDone;
    }

    public void triggerCountdownFinished() {
        countdownDone = true;
    }

    public GameController(ClientSocket socket) {
        this.socket = socket;
    }

    public void sendWord(String word) {
        socket.sendMessage("WORD:" + word);
    }

    public void sendTimeout() {
        socket.sendMessage("TIMEOUT");
    }

    public void sendWinner(String winnerName) {
        socket.sendMessage("WINNER:" + winnerName);
    }

    public int getLevelTime() {
        return switch (level) {
            case 1 -> 10;
            case 2 -> 8;
            case 3 -> 6;
            case 4 -> 4;
            case 5 -> 2;
            default -> 1;
        };


    }

    // ===== 콜백 등록 =====
    public void setOnGameStart(Runnable cb) {
        onGameStart = cb;
    }

    public void setOnTurn(Consumer<String> cb) {
        onTurn = cb;
    }

    public void setOnWord(Consumer<String> cb) {
        onWord = cb;
    }

    public void setOnTimer(Consumer<Integer> cb) {
        onTimer = cb;
    }

    public void setOnLevelUp(Consumer<Integer> cb) {
        onLevelUp = cb;
    }

    public void setOnGameOver(Consumer<String> cb) {
        onGameOver = cb;
    }

    // 🔥 LIFE_LOST 콜백 등록
    public void setOnLifeLost(Consumer<String> cb) {
        onLifeLost = cb;
    }

    public void setOnInvalidWord(Consumer<String> cb) { onInvalidWord = cb; }

    // ===== ClientReceiver 가 호출 =====
    public void triggerGameStart() {
        if (onGameStart != null) onGameStart.run();
    }

    public void triggerTurn(String name) {
        if (onTurn != null) onTurn.accept(name);
    }

    public void triggerWord(String w) {
        if (onWord != null) onWord.accept(w);
    }

    public void triggerTimer(int t) {
        if (onTimer != null) onTimer.accept(t);
    }

    public void triggerLevelUp(int lv) {
        level = lv;
        if (onLevelUp != null) onLevelUp.accept(lv);
    }

    public void triggerGameOver(String winner) {
        if (onGameOver != null) onGameOver.accept(winner);
    }

    // 🔥 서버에서 "LIFE_LOST:이름" 왔을 때 호출
    public void triggerLifeLost(String name) {
        if (onLifeLost != null) onLifeLost.accept(name);
    }

    public void triggerInvalidWord(String data) {
        if (onInvalidWord != null) onInvalidWord.accept(data); // data = "이름:단어"
    }
}
