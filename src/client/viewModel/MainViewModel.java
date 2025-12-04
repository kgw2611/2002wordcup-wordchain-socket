package client.viewModel;

import client.model.PlayerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainViewModel {

    private PlayerInfo selfPlayer;
    private final List<PlayerInfo> players = new ArrayList<>();

    // 🔥 캐릭터 변경 시 RoomController가 받을 콜백
    private Consumer<String> onCharacterChanged;

    public MainViewModel() {
        selfPlayer = new PlayerInfo();
        selfPlayer.setCharacterType("DEFAULT"); // 기본 캐릭터
    }

    public void setPlayerName(String name) {
        selfPlayer.setName(name);
        players.clear();
        players.add(selfPlayer);
    }

    public PlayerInfo getPlayer() {
        return selfPlayer;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public void updatePlayers(List<PlayerInfo> newList) {
        players.clear();
        players.addAll(newList);
    }

    // 🔥 캐릭터 선택 처리
    public void setSelectedCharacter(String type) {
        selfPlayer.setCharacterType(type);

        if (onCharacterChanged != null) {
            onCharacterChanged.accept(type);
        }
    }

    public String getSelectedCharacter() {
        return selfPlayer.getCharacterType();
    }

    // 🔥 RoomController가 콜백 등록하는 함수
    public void setOnCharacterChangedListener(Consumer<String> cb) {
        this.onCharacterChanged = cb;
    }

    // --------------------------------------------------
    // 입력 검증
    // --------------------------------------------------
    public boolean validateName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public boolean validatePort(String port) {
        try {
            int p = Integer.parseInt(port);
            return p >= 1024 && p <= 65535;
        } catch (Exception e) {
            return false;
        }
    }
}
