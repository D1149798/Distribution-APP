package fcu.app.distributionapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fcu.app.distributionapp.R;


public class AddGroupFragment extends Fragment {

    private EditText etGroupName, etAddMember;
    private Button btnCreateGroup, btnAddMember;
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
        etAddMember = view.findViewById(R.id.et_add_member);
        btnAddMember = view.findViewById(R.id.btn_add_member);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 動態建立好友 checkbox：顯示名稱但 tag 綁 UID
        db.collection("friends").document(currentUser.getUid()).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Map<String, Object> friendsMap = snapshot.getData();
                for (Map.Entry<String, Object> entry : friendsMap.entrySet()) {
                    String friendUid = entry.getKey();
                    String friendName = String.valueOf(entry.getValue());

                    CheckBox checkBox = new CheckBox(getContext());
                    checkBox.setText(friendName); // 顯示名稱
                    checkBox.setTag(friendUid);   // 實際值是 UID
                    layoutMemberCheckboxes.addView(checkBox);
                }
            }
        });

        // 加入非好友成員（輸入 email）
        btnAddMember.setOnClickListener(v -> {
            String inputEmail = etAddMember.getText().toString().trim();
            String currentUserId = currentUser.getUid();

            if (TextUtils.isEmpty(inputEmail)) {
                Toast.makeText(getContext(), "請輸入欲加入群組成員帳號", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").whereEqualTo("Email", inputEmail).get().addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String friendUid = doc.getId();
                    String friendName = doc.getString("Name");

                    if (friendUid.equals(currentUserId)) {
                        Toast.makeText(getContext(), "不能加自己為成員", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 檢查是否已在 checkbox 中
                    boolean alreadyAdded = false;
                    for (int i = 0; i < layoutMemberCheckboxes.getChildCount(); i++) {
                        View child = layoutMemberCheckboxes.getChildAt(i);
                        if (child instanceof CheckBox) {
                            CheckBox cb = (CheckBox) child;
                            if (cb.getTag().equals(friendUid)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                    }

                    if (alreadyAdded) {
                        Toast.makeText(getContext(), "此成員已在列表中", Toast.LENGTH_SHORT).show();
                    } else {
                        CheckBox newMember = new CheckBox(getContext());
                        newMember.setText(friendName != null ? friendName : inputEmail);
                        newMember.setTag(friendUid);
                        newMember.setChecked(true);
                        layoutMemberCheckboxes.addView(newMember);

                        Toast.makeText(getContext(), "成功加入：" + (friendName != null ? friendName : inputEmail), Toast.LENGTH_SHORT).show();
                        etAddMember.setText(""); // 清空輸入欄
                    }

                } else {
                    Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "查詢失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();

            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(getContext(), "請輸入群組名稱", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> memberUids = new ArrayList<>();
            memberUids.add(currentUser.getUid()); // 加入自己 UID

            for (int i = 0; i < layoutMemberCheckboxes.getChildCount(); i++) {
                View child = layoutMemberCheckboxes.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    if (cb.isChecked()) {
                        String uid = (String) cb.getTag(); // tag 綁的是 UID
                        memberUids.add(uid);
                    }
                }
            }

            if (memberUids.size() <= 1) {
                Toast.makeText(getContext(), "請至少選一位好友加入群組", Toast.LENGTH_SHORT).show();
                return;
            }

            // 建立群組資料
            Map<String, Object> group = new HashMap<>();
            group.put("name", groupName);
            group.put("members", memberUids);
            group.put("createdBy", currentUser.getUid());
            group.put("createdAt", FieldValue.serverTimestamp());

            db.collection("newGroups").add(group)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "群組建立失敗", Toast.LENGTH_SHORT).show()
                    );
        });
        return view;
    }
}