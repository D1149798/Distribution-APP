package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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
            String friendEmail = editFriendMail.getText().toString().trim();
            String friendId = friendEmail.split("@")[0]; //只取前段當欄位名

            if (TextUtils.isEmpty(friendId)) {
                Toast.makeText(getContext(), "請輸入好友帳號", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserId = user.getEmail().split("@")[0];

            // 先檢查 /users/{friendId} 是否存在
            db.collection("users").document(friendId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            // 存在，新增雙向好友
                            addFriend(currentUserId, friendId);
                            addFriend(friendId, currentUserId); // 反向加入
                        } else {
                            //沒有這個帳號
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
    private void addFriend(String userId, String friendId) {
        db.collection("friends")
                .document(userId)
                .update(friendId, true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "已加入好友：" + friendId, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // 若 document 不存在，先建立
                    Map<String, Object> data = new HashMap<>();
                    data.put(friendId, true);
                    db.collection("friends")
                            .document(userId)
                            .set(data);
                });
    }
}