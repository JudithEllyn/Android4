package hk.edu.cuhk.ie.iems5722.a2_1155162647;

public class MessageEntity {
    private String message;
    private String username;
    private String time;
    private int userid;

    public MessageEntity(String username, int userid, String time,String message){
        this.message = message;
        this.username = username;
        this.time = time;
        this.userid = userid;
    }

    public String getMessage(){
        return this.message;
    }
    public String getUsername(){
        return this.username;
    }
    public String getTime(){
        return this.time;
    }
    public int getUserid(){
        return this.userid;
    }

}
