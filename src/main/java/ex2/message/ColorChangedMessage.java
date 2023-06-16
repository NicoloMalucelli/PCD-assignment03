package ex2.message;

public class ColorChangedMessage extends Message{

    private int color;

    public ColorChangedMessage(){}
    public ColorChangedMessage(String id, int color){
        super(id);
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
