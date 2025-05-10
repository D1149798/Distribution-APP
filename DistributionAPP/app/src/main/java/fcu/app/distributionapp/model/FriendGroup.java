package fcu.app.distributionapp.model;

public class FriendGroup {
    public String groupName;
    public String message;
    public String time;
    public int avatarResId;

    public FriendGroup(String groupName, String message, String time, int avatarResId) {
        this.groupName = groupName;
        this.message = message;
        this.time = time;
        this.avatarResId = avatarResId;
    }
}
