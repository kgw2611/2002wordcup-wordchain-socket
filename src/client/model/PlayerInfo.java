package client.model;

public class PlayerInfo {
    private String name;
    private int score;
    private boolean isReady;
    public PlayerInfo(){}
    public PlayerInfo(String name){
        this.name=name;
        this.score=0;
        this.isReady=false;
    }

    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name=name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}
