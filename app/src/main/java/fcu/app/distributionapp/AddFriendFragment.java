package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddFriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFriendFragment extends Fragment {

    private EditText editFriendName, editFriendMail;
    private Button btnAddFriend;
    private FirebaseFirestore db;

    public AddFriendFragment() {
        // Required empty public constructor
    }

    public static AddFriendFragment newInstance(String param1, String param2) {
        AddFriendFragment fragment = new AddFriendFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();

        editFriendMail = view.findViewById(R.id.et_friend_mail);
        btnAddFriend = view.findViewById(R.id.btn_add_friend);

        btnAddFriend.setOnClickListener(v ->{

            String inputEmail = editFriendMail.getText().toString().trim();

            if (TextUtils.isEmpty(inputEmail)) {
                Toast.makeText(getContext(), "請輸入好友帳號", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUid = currentUser.getUid();
            String currentName = currentUser.getEmail().split("@")[0];

            // 先檢查 /users/{friendId} 是否存在
            db.collection("users")
                    .whereEqualTo("Email", inputEmail)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            String friendUid = doc.getId(); //UID
                            String friendEmail = doc.getString("Email");
                            String friendName = doc.getString("Name");

                            if (friendUid.equals(currentUid)) {
                                Toast.makeText(getContext(), "不能加自己為好友", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Log.d("AddFriendDebug", "目前登入者 UID: " + currentUid);
                            Log.d("AddFriendDebug", "輸入 email: " + inputEmail);
                            Log.d("AddFriendDebug", "查到的好友 UID: " + friendUid);

                            // ✅ 加入雙向好友（key: UID, value: name）
                            addFriend(currentUid, friendUid, friendName);
                            addFriend(friendUid, currentUid, currentName);

                        } else {
                            Toast.makeText(getContext(), "此帳號尚未註冊", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "檢查失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        // Inflate the layout for this fragment
        return view;
    }
    private void addFriend(String userUid, String friendUid, String friendName) {
        Map<String, Object> data = new HashMap<>();
        data.put(friendUid, friendName); // 🔑 UID → 名稱

        db.collection("friends")
                .document(userUid)
                .set(data, SetOptions.merge()) // ✅ merge 避免覆蓋整份資料
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "已加入好友：" + friendName, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "加入好友失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}