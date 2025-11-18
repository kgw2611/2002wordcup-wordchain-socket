package client.viewModel;

import client.model.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel {

    private PlayerInfo selfPlayer;
    private final List<PlayerInfo> players = new ArrayList<>();

    public MainViewModel() {
        selfPlayer = new PlayerInfo();
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

    // --------------------------------------------------
    // 🔥 여기 추가: 입력 검증 (UI → ViewModel)
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
