package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fcu.app.distributionapp.R;
import fcu.app.distributionapp.model.GroupMember;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private List<GroupMember> memberList;

    public GroupMemberAdapter(List<GroupMember> memberList) {
        this.memberList = memberList;
    }

    public List<GroupMember> getSelectedMembers() {
        List<GroupMember> selected = new ArrayList<>();
        for (GroupMember member : memberList) {
            if (member.isSelected()) {
                selected.add(member);
            }
        }
        return selected;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkbox_member);
        }
    }

    @NonNull
    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberAdapter.ViewHolder holder, int position) {
        GroupMember member = memberList.get(position);
        holder.checkBox.setText(member.getName());
        holder.checkBox.setChecked(member.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> member.setSelected(isChecked));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }
    public void updateData(List<GroupMember> newMembers) {
        this.memberList.clear();
        this.memberList.addAll(newMembers);
        notifyDataSetChanged();
    }
    public void clearSelection() {
        for (GroupMember member : memberList) {
            member.setSelected(false);
        }
        notifyDataSetChanged();
    }

}
