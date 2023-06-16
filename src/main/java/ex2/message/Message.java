package ex2.message;

public class Message {
    private String id;
    public Message(){}
    public Message(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }
    public void setId(String address){
        this.id = address;
    }
}
