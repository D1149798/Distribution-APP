package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fcu.app.distributionapp.R;
import fcu.app.distributionapp.model.GroupMember;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private final List<GroupMember> memberList;
    private boolean checkBoxVisible = true;

    public GroupMemberAdapter(List<GroupMember> memberList) {
        this.memberList = memberList;
    }

    public void setCheckBoxVisible(boolean visible) {
        this.checkBoxVisible = visible;
        notifyDataSetChanged();
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

    public void setSelectedMembers(List<String> names) {
        Set<String> selectedNames = new HashSet<>(names);
        for (GroupMember member : memberList) {
            member.setSelected(selectedNames.contains(member.getName()));
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        for (GroupMember member : memberList) {
            member.setSelected(false);
        }
        notifyDataSetChanged();
    }

    public void updateData(List<GroupMember> newMembers) {
        memberList.clear();
        memberList.addAll(newMembers);
        notifyDataSetChanged();
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

        holder.checkBox.setOnCheckedChangeListener(null); // 避免觸發多次
        holder.checkBox.setChecked(member.isSelected());
        holder.checkBox.setVisibility(checkBoxVisible ? View.VISIBLE : View.GONE);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            member.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }
}
