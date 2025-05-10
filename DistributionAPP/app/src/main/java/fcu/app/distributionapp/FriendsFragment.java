package fcu.app.distributionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fcu.app.distributionapp.adapter.FriendAdapter;
import fcu.app.distributionapp.model.FriendGroup;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private List<FriendGroup> groupList;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = view.findViewById(R.id.recycler_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        groupList.add(new FriendGroup("台南一日遊(3)", "你欠錢一週年!!! 🎉🎉", "2d", R.drawable.ic_launcher_foreground));
        groupList.add(new FriendGroup("畢旅分帳(4)", "晚餐還沒收", "3d", R.drawable.ic_launcher_foreground));
        groupList.add(new FriendGroup("大一室友(5)", "明天要不要吃火鍋", "1d", R.drawable.ic_launcher_foreground));

        adapter = new FriendAdapter(groupList);
        recyclerView.setAdapter(adapter);

        return view;
        //return inflater.inflate(R.layout.fragment_friends, container, false);
    }
}
