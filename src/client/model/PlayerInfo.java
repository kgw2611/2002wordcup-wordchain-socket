package client.model;

/*
    플레이어 정보를 저장하는 PlayerInfo 클래스
    1. 멤버 변수는 준비 상태 / 닉네임 / 게임 캐릭터 타입
    2. 그 외 멤버 변수의 getter/setter 함수들
*/
public class PlayerInfo {
    private String name;
    private boolean isReady;
    private String characterType;   // 캐릭터 타입

    public PlayerInfo() {
        this.name = "";
        this.isReady = false;
        this.characterType = "DEFAULT";   // 기본 캐릭터
    }

    public PlayerInfo(String name){
        this.name = name;
        this.isReady = false;
        this.characterType = "DEFAULT";   // 기본 캐릭터
    }

    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean ready) {
        isReady = ready;
    }

    // 캐릭터 getter / setter
    public String getCharacterType() {
        return characterType;
    }
    public void setCharacterType(String type) {
        this.characterType = type;
    }
}
