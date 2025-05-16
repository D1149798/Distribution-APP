package fcu.app.distributionapp.model;

public class FriendGroup {
    public String friendName;
    public String friendEmail;
    // String time;
    public int avatarResId;

    public FriendGroup(String friendName, String friendEmail, String time, int avatarResId) {
        this.friendName = friendName;
        this.friendEmail = friendEmail;
        //this.time = time;
        this.avatarResId = avatarResId;
    }
}
