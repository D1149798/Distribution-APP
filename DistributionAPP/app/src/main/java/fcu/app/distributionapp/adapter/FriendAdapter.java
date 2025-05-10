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
        TextView tvGroupName, tvMessage, tvTime;
        ImageView imgAvatar;

        public ViewHolder(View view) {
            super(view);
            tvGroupName = view.findViewById(R.id.tv_group_name);
            tvMessage = view.findViewById(R.id.tv_message);
            tvTime = view.findViewById(R.id.tv_time);
            imgAvatar = view.findViewById(R.id.img_avatar);
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
        holder.tvGroupName.setText(group.groupName);
        holder.tvMessage.setText(group.message);
        holder.tvTime.setText(group.time);
        holder.imgAvatar.setImageResource(group.avatarResId);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }
}


/*
private List<FriendGroup> groupList;

    public FriendAdapter(List<FriendGroup> list) {
        this.groupList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.txtName.setText(restaurant.getName());
        holder.txtOpeningHours.setText("Open: " + restaurant.getOpeningHours());
        holder.txtType.setText("Type: " + restaurant.getType());
        holder.imgRestaurant.setImageResource(restaurant.getImageResId());
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtOpeningHours, txtType;
        ImageView imgRestaurant;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.tv_name);
            txtOpeningHours = itemView.findViewById(R.id.tv_opening_hours);
            txtType = itemView.findViewById(R.id.tv_type);
            imgRestaurant = itemView.findViewById(R.id.iv_logo);
        }
    }
 */