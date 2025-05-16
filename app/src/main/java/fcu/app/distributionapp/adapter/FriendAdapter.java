package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.distributionapp.R;
import fcu.app.distributionapp.model.FriendGroup;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder>{
    private List<FriendGroup> groupList;

    public FriendAdapter(List<FriendGroup> groupList) {
        this.groupList = groupList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        TextView tvFriendMail;
        //TextView tvTime;
        //ImageView imgAvatar;

        public ViewHolder(View view) {
            super(view);
            tvFriendName = view.findViewById(R.id.tv_friend_name);
            tvFriendMail = view.findViewById(R.id.tv_friend_mail);
            //imgAvatar = view.findViewById(R.id.img_avatar);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FriendGroup group = groupList.get(position);
        holder.tvFriendName.setText(group.friendName);
        holder.tvFriendMail.setText(group.friendEmail);
        //holder.imgAvatar.setImageResource(group.avatarResId);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }
}
