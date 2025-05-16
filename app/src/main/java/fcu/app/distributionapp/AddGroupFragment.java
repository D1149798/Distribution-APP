package fcu.app.distributionapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fcu.app.distributionapp.R;


public class AddGroupFragment extends Fragment {

    private EditText etGroupName;
    private Button btnCreateGroup;
    private LinearLayout layoutMemberCheckboxes;

    private FirebaseFirestore db;
    private String currentUserId;

    public AddGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_group, container, false);

        etGroupName = view.findViewById(R.id.et_add_group_name);
        btnCreateGroup = view.findViewById(R.id.btn_create_group);
        layoutMemberCheckboxes = view.findViewById(R.id.layout_member_checkboxes);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        currentUserId = user.getEmail().split("@")[0];

        // 讀取好友資料並動態建立勾選框
        db.collection("friends").document(currentUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Map<String, Object> friendsMap = snapshot.getData();
                        for (String friendId : friendsMap.keySet()) {
                            CheckBox checkBox = new CheckBox(getContext());
                            checkBox.setText(friendId);
                            layoutMemberCheckboxes.addView(checkBox);
                        }
                    }
                });

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();

            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(getContext(), "請輸入群組名稱", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> members = new ArrayList<>();
            members.add(currentUserId); // 一定包含自己

            for (int i = 0; i < layoutMemberCheckboxes.getChildCount(); i++) {
                View child = layoutMemberCheckboxes.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    if (cb.isChecked()) {
                        members.add(cb.getText().toString());
                    }
                }
            }
            if (members.size() <= 1) {
                Toast.makeText(getContext(), "請至少選一位好友加入群組", Toast.LENGTH_SHORT).show();
                return;
            }

            // 寫入 Firestore
            Map<String, Object> group = new HashMap<>();
            group.put("name", groupName);
            group.put("members", members);
            group.put("createdBy", currentUserId);
            group.put("createdAt", FieldValue.serverTimestamp());

            db.collection("newGroups").add(group)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(getContext(), "群組建立成功！", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "群組建立失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        return view;
    }
}