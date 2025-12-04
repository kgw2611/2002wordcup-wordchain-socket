package client.resource;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Images {
    public static final ImageIcon MAIN_BG =
            new ImageIcon("src/client/resource/background.png");
    public static final ImageIcon PLAYER_IMG=
            new ImageIcon("src/client/resource/PlayerImage.png");
    public static final ImageIcon PLAYER_DIE_IMG=
            new ImageIcon("src/client/resource/PlayerDieImage.png");
    public static final ImageIcon FULL_HEART=
            new ImageIcon("src/client/resource/full_heart.png");
    public static final ImageIcon EMPTY_HEART=
            new ImageIcon("src/client/resource/empty_heart.png");
    public static final ImageIcon PLAYER2_IMG=
            new ImageIcon("src/client/resource/PlayerImage2.png");
    public static final ImageIcon PLAYER3_IMG=
            new ImageIcon("src/client/resource/PlayerImage3.png");
    public static final ImageIcon GAME_BACKGROUND=
            new ImageIcon("src/client/resource/GameBackground.png");
    public static final ImageIcon GAME_BACKGROUND2=
            new ImageIcon("src/client/resource/GameBackground2.png");
    public static final ImageIcon WordBoard=
            new ImageIcon("src/client/resource/WordBoard.jpg");
    public static final ImageIcon First=
            new ImageIcon("src/client/resource/First.png");
    public static final ImageIcon Second=
            new ImageIcon("src/client/resource/Second.png");
    public static final ImageIcon Third=
            new ImageIcon("src/client/resource/Third.png");
    public static final ImageIcon Loser=
            new ImageIcon("src/client/resource/Loser.png");
    private static final Map<String, ImageIcon> CHARACTER_MAP = new HashMap<>();

    static {
        CHARACTER_MAP.put("DEFAULT", PLAYER_IMG);
        CHARACTER_MAP.put("TYPE1", PLAYER2_IMG);
        CHARACTER_MAP.put("TYPE2", PLAYER3_IMG);


        // 필요하면 더 추가 가능
        // CHARACTER_MAP.put("DIE", PLAYER_DIE);
    }

    // 🔥 캐릭터 아이디로 이미지 가져오는 메서드
    public static ImageIcon getCharacter(String type) {
        return CHARACTER_MAP.getOrDefault(type, PLAYER_IMG);
    }
}

