package client.resource;

import java.awt.*;
import java.io.InputStream;

public class Fonts {

    private static Font baseFont;

    static {
        try {
            // 클래스패스에서 shilla.ttf 로드
            InputStream is = Fonts.class.getResourceAsStream("/client/resource/Shilla.ttf");
            baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            System.out.println("❌ Failed to load shilla.ttf, fallback to default font.");
            baseFont = new Font("맑은 고딕", Font.PLAIN, 16);
        }
    }

    // 이제 deriveFont로 크기/스타일만 지정해서 사용
    public static final Font TITLE  = baseFont.deriveFont(Font.BOLD, 32f);
    public static final Font LABEL  = baseFont.deriveFont(Font.BOLD, 22f);
    public static final Font NORMAL = baseFont.deriveFont(Font.PLAIN, 20f);
    public static final Font BUTTON = baseFont.deriveFont(Font.BOLD, 22f);
}
