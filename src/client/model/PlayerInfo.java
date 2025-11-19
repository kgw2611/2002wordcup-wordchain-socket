package client.model;

public class PlayerInfo {
    private String name;
    private boolean isReady;
    public PlayerInfo(){}
    public PlayerInfo(String name){
        this.name=name;
        this.isReady=false;
    }

    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name=name;
    }

    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean ready) {
        isReady = ready;
    }
}
