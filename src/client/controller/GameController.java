package client.controller;

import client.network.ClientSocket;

import java.util.function.Consumer;

/*
    실질적 게임 관리/진행 컨트롤러
    1. 화면이 넘어가도 소켓 연결이 유지되도록 멤버 변수에 포트와 소켓을 가짐
    2. RoomController에서 트리거로 넘어온 부분들을 서버와 통신해 실제로 처리
    3. 플레이어 턴 / 단어 전송 / 타이머 처리 / 레벨 로직 / 게임 종료 / 목숨 등 게임 진행 전부 관리
*/
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

    // LIFE_LOST 콜백 등록
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

    // 서버에서 "LIFE_LOST:이름" 왔을 때 호출
    public void triggerLifeLost(String name) {
        if (onLifeLost != null) onLifeLost.accept(name);
    }

    public void triggerInvalidWord(String data) {
        if (onInvalidWord != null) onInvalidWord.accept(data); // data = "이름:단어"
    }
}
