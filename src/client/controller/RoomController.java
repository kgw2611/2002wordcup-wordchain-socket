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

        // 🔥 ViewModel → RoomController → Server 로 캐릭터 선택 전달
        viewModel.setOnCharacterChangedListener(type -> {
            String msg = "CHARACTER:" + viewModel.getPlayer().getName() + ":" + type;
            socket.sendMessage(msg);
        });
    }

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

        // 🧡 플레이어 목록
        if (msg.startsWith("PLAYER_LIST:")) {
            List<PlayerInfo> list = parsePlayers(msg.substring(12));
            viewModel.updatePlayers(list);
            if (onPlayersChanged != null) onPlayersChanged.accept(list);
            return;
        }

        // 🔥 캐릭터 업데이트
        if (msg.startsWith("CHARACTER_UPDATE:")) {
            // FORMAT → CHARACTER_UPDATE:홍길동:TYPE2
            String[] sp = msg.split(":");
            if (sp.length == 3) {
                String name = sp[1];
                String type = sp[2];

                for (PlayerInfo p : viewModel.getPlayers()) {
                    if (p.getName().equals(name)) {
                        p.setCharacterType(type);
                    }
                }

                if (onPlayersChanged != null) onPlayersChanged.accept(viewModel.getPlayers());
            }
            return;
        }

        // READY 리스트
        if (msg.startsWith("PLAYER_READY_LIST:")) {
            if (onPlayerReady != null) onPlayerReady.accept(msg.substring(18));
            return;
        }

        // 채팅
        if (msg.startsWith("CHAT:")) {
            if (onChat != null) onChat.accept(msg.substring(5));
            return;
        }

        if(msg.startsWith("[SYSTEM]")) {
            if (onChat != null) onChat.accept(msg);
            return;
        }

        if (msg.equals("GAME_START")) {
            gameController = new GameController(socket);
            if (onGameStart != null) onGameStart.run();
            return;
        }

        if (gameController != null) handleGameMessage(msg);
    }

    private void handleGameMessage(String msg) {
        if (msg.startsWith("TURN:")) {
            String name = msg.substring(5);
            gameController.triggerTurn(name);
            return;
        }

        // 단어 표시
        if (msg.startsWith("WORD:")) {
            String w = msg.substring(5);
            gameController.triggerWord(w);
            return;
        }

        // 잘못된 단어
        if (msg.startsWith("WORD_INVALID:")) {
            String data = msg.substring(13);
            gameController.triggerInvalidWord(data);
            return;
        }

        // 목숨 감소
        if (msg.startsWith("LIFE_LOST:")) {
            String name = msg.substring(10);
            gameController.triggerLifeLost(name);
            return;
        }

        // 레벨업
        if (msg.startsWith("LEVEL_UP:")) {
            int lv = Integer.parseInt(msg.substring(9));
            gameController.triggerLevelUp(lv);
            return;
        }

        // 게임 종료
        if (msg.startsWith("GAME_OVER:")) {
            String winner = msg.substring(10);
            gameController.triggerGameOver(winner);
            return;
        }
    }

    private List<PlayerInfo> parsePlayers(String raw) {

        List<PlayerInfo> list = new ArrayList<>();

        String[] names = raw.split(",");

        for (String name : names) {
            if (name.isEmpty()) continue;

            // 기존 VM에 있는 플레이어 찾기
            PlayerInfo existing = null;
            for (PlayerInfo p : viewModel.getPlayers()) {
                if (p.getName().equals(name)) {
                    existing = p;
                    break;
                }
            }

            if (existing != null) {
                // 🔥 기존 객체 그대로 사용 (캐릭터 타입 유지)
                list.add(existing);

            } else {
                // 🔥 새 플레이어 → DEFAULT 캐릭터로 추가
                PlayerInfo newP = new PlayerInfo(name);
                newP.setCharacterType("DEFAULT");
                list.add(newP);
            }
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
