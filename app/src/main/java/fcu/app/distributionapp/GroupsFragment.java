package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fcu.app.distributionapp.adapter.FriendAdapter;
import fcu.app.distributionapp.adapter.GroupAdapter;
import fcu.app.distributionapp.model.FriendGroup;
import fcu.app.distributionapp.model.GroupGroup;

public class GroupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<GroupGroup> groupList;
    public GroupsFragment() {
        // Required empty public constructor
    }

    public static GroupsFragment newInstance(String param1, String param2) {
        GroupsFragment fragment = new GroupsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = view.findViewById(R.id.recycler_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        groupList.add(new GroupGroup("台南一日遊(3)", "你欠錢一週年!!! 🎉🎉", "2d", R.drawable.ic_launcher_foreground));
        groupList.add(new GroupGroup("畢旅分帳(4)", "晚餐還沒收", "3d", R.drawable.ic_launcher_foreground));
        groupList.add(new GroupGroup("大一室友(5)", "明天要不要吃火鍋", "1d", R.drawable.ic_launcher_foreground));

        adapter = new GroupAdapter(groupList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}