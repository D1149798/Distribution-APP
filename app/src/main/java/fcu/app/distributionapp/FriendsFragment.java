package fcu.app.distributionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.widget.Toast;

import fcu.app.distributionapp.adapter.FriendAdapter;
import fcu.app.distributionapp.model.FriendGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendsFragment extends Fragment {

    private Button btnAddFriend;
    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private List<FriendGroup> groupList;
    private FirebaseFirestore db;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        db = FirebaseFirestore.getInstance();
        btnAddFriend = view.findViewById(R.id.btn_add_friend);
        recyclerView = view.findViewById(R.id.recycler_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        adapter = new FriendAdapter(groupList);
        recyclerView.setAdapter(adapter);

        btnAddFriend.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_main, new AddFriendFragment())
                    .addToBackStack(null)
                    .commit();

        });

        loadFriendsFromFirestore();

        return view;
    }

    private void loadFriendsFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String currentUid = user.getUid();

        db.collection("friends").document(currentUid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Map<String, Object> friendsMap = snapshot.getData();
                        if (friendsMap != null) {
                            groupList.clear();
                            for (String friendUid : friendsMap.keySet()) {
                                db.collection("users").document(friendUid).get()
                                        .addOnSuccessListener(friendSnapshot -> {
                                            if (friendSnapshot.exists()) {
                                                String email = friendSnapshot.getString("Email");
                                                String name = friendSnapshot.getString("Name");
                                                groupList.add(new FriendGroup(name, email));
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "目前尚未有好友", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "讀取好友失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}