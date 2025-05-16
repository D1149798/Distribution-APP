package fcu.app.distributionapp.model;

public class GroupMember {
    private String name;
    private boolean isSelected;

    public GroupMember(String name) {
        this.name = name;
        this.isSelected = false;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
