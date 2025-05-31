package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fcu.app.distributionapp.adapter.GroupAdapter;
import fcu.app.distributionapp.model.Group;

public class GroupsFragment extends Fragment {
    private FirebaseFirestore db;
    private Button btnAddGroup;
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Group> groupList;

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

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();
//
//        // 建立一個新的群組物件
//        Map<String, Object> group = new HashMap<>();
//        group.put("name", "台北三日遊");
//
//        // 自動產生 document ID
//        db.collection("groups")
//                .add(group)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("Firestore", "群組新增成功，ID: " + documentReference.getId());
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Firestore", "新增群組失敗", e);
//                });
        btnAddGroup = view.findViewById(R.id.btn_add_group);
        recyclerView = view.findViewById(R.id.recycler_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        adapter = new GroupAdapter(groupList, group -> {
            // 點擊某個群組後的動作，例如跳轉到聊天頁面
            //Fragment groupChatFragment = new GroupChatFragment();
/*
            Bundle args = new Bundle();
            args.putString("groupId", group.getId());      // 你需要在 Group 類別中有 getId()
            args.putString("groupName", group.getName());  // 也需要有 getName()
            groupChatFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_main, groupChatFragment)
                    .addToBackStack(null)
                    .commit();
                    */
            Toast.makeText(getContext(), "你點選了群組：" + group.getName(), Toast.LENGTH_SHORT).show();
            AccountsFragment accountsFragment = AccountsFragment.newInstance(group.getId(),"chat");
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_main, accountsFragment)
                    .addToBackStack(null)
                    .commit();

        });

        recyclerView.setAdapter(adapter);

        btnAddGroup.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_main, new AddGroupFragment())
                    .addToBackStack(null)
                    .commit();
        });

        loadGroupsFromFirestore();

        return view;
    }

    private void loadGroupsFromFirestore() {
        db.collection("newGroups")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    groupList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String groupId = doc.getId();
                        String groupName = doc.getString("name");
                        groupList.add(new Group(groupId, groupName));  // 目前只加 id + name
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "讀取群組失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}