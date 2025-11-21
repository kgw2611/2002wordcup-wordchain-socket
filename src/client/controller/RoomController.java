package client.controller;

import client.model.PlayerInfo;
import client.network.ClientSocket;
import client.viewModel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RoomController {

    private final MainViewModel viewModel;
    private final ClientSocket socket;

    private Consumer<List<PlayerInfo>> onPlayersChanged;
    private Consumer<String> onChat;
    private Consumer<String> onPlayerReady;
    private Runnable onGameStart;

    private GameController gameController;
    private final int port;

    public RoomController(MainViewModel vm, int port) {
        this.viewModel = vm;
        this.socket = new ClientSocket();
        this.port = port;
    }

    // Getter: ClientRoom → ClientGame 전달 위함
    public GameController getGameController() {
        return gameController;
    }

    public void setOnPlayersChanged(Consumer<List<PlayerInfo>> cb) { this.onPlayersChanged = cb; }
    public void setOnChatReceived(Consumer<String> cb) { this.onChat = cb; }
    public void setOnPlayerReady(Consumer<String> cb) { this.onPlayerReady = cb; }
    public void setOnGameStart(Runnable cb) { this.onGameStart = cb; }

    public boolean connect() {
        return socket.connect("localhost", port, this::handleMessage);
    }

    private void handleMessage(String msg) {

        // 🧡 대기방 플레이어 목록
        if (msg.startsWith("PLAYER_LIST:")) {
            List<PlayerInfo> list = parsePlayers(msg.substring(12));
            viewModel.updatePlayers(list);
            if (onPlayersChanged != null) onPlayersChanged.accept(list);
            return;
        }

        // 🧡 READY 리스트
        if (msg.startsWith("PLAYER_READY_LIST:")) {
            if (onPlayerReady != null) onPlayerReady.accept(msg.substring(18));
            return;
        }

        // 🧡 채팅
        if (msg.startsWith("CHAT:")) {
            String chatContent = msg.substring(5);
            if (onChat != null) onChat.accept(chatContent);
            return;
        }

        // 시스템 메시지
        if(msg.startsWith("[SYSTEM]")) {
            if (onChat != null) onChat.accept(msg);
            return;
        }

        // 🧡 게임 시작
        if (msg.equals("GAME_START")) {

            // GameController 생성
            gameController = new GameController(socket);

            // ClientRoom으로 신호
            if (onGameStart != null) onGameStart.run();
            return;
        }

        // 🧡 게임 내부 메시지 처리 (게임 시작 후)
        if (gameController != null) handleGameMessage(msg);
    }

    private void handleGameMessage(String msg) {

        if (msg.startsWith("WORD_INVALID:")) {
            // "이름:단어" 부분만 잘라서 GameController로 전달
            gameController.triggerInvalidWord(msg.substring(13));
            return;
        }

        // TURN:이름
        if (msg.startsWith("TURN:")) {
            gameController.triggerTurn(msg.substring(5));
            return;
        }

        // WORD:사과
        if (msg.startsWith("WORD:")) {
            String word = msg.substring(5).trim();
            gameController.triggerWord(word);
            return;
        }

        // LIFE_LOST:홍길동
        if (msg.startsWith("LIFE_LOST:")) {
            gameController.triggerLifeLost(msg.substring(10));
            return;
        }

        // GAME_OVER:이름
        if (msg.startsWith("GAME_OVER:")) {
            gameController.triggerGameOver(msg.substring(10));
            return;
        }

        // LEVEL_UP:2
        if (msg.startsWith("LEVEL_UP:")) {
            int lv = Integer.parseInt(msg.substring(9));
            gameController.triggerLevelUp(lv);
        }
    }

    private List<PlayerInfo> parsePlayers(String raw) {
        List<PlayerInfo> list = new ArrayList<>();
        for (String s : raw.split(",")) {
            if (!s.isEmpty()) list.add(new PlayerInfo(s));
        }
        return list;
    }

    public void joinRoom() {
        socket.sendMessage("JOIN:" + viewModel.getPlayer().getName());
    }

    public void sendChat(String text) {
        socket.sendMessage("CHAT:" + viewModel.getPlayer().getName() + ":" + text);
    }

    public void sendReady() {
        socket.sendMessage("READY:" + viewModel.getPlayer().getName());
    }
}
