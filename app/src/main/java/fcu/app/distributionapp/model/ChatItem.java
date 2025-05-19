package fcu.app.distributionapp.model;

public class ChatItem {
    public enum Type{
        DATE, MESSAGE
    }

    public Type type;
    public Message message;
    public String date;

    //日期建構子
    public ChatItem(Type type, String date) {
        this.type = type.DATE;
        this.date = date;
    }

    //訊息建構子
    public ChatItem(Type type, Message message) {
        this.type = type.MESSAGE;
        this.message = message;
    }
}
