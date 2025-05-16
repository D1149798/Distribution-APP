package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.distributionapp.R;
import fcu.app.distributionapp.model.Group;
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder>{

    public interface OnItemClickListener {
        void onItemClick(Group group);
    }
    private List<Group> groupList;
    private OnItemClickListener listener;

    public GroupAdapter(List<Group> groupList, OnItemClickListener listener) {

        this.groupList = groupList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        //TextView tvMessage, tvTime;
        //ImageView imgAvatar;

        public ViewHolder(View view) {
            super(view);
            tvGroupName = view.findViewById(R.id.tv_group_name);
            //tvMessage = view.findViewById(R.id.tv_message);
            //tvTime = view.findViewById(R.id.tv_time);
            //imgAvatar = view.findViewById(R.id.img_avatar);
        }
        public void bind(Group group, OnItemClickListener listener) {
            tvGroupName.setText(group.getName());
            itemView.setOnClickListener(v -> listener.onItemClick(group));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(groupList.get(position), listener);
        //Group group = groupList.get(position);
        //holder.tvGroupName.setText(group.getName());
//        holder.tvMessage.setText(group.message);
//        holder.tvTime.setText(group.time);
//        holder.imgAvatar.setImageResource(group.avatarResId);

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }
}
