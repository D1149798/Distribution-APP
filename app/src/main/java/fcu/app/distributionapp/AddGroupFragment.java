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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 動態建立好友 checkbox：顯示名稱但 tag 綁 UID
        db.collection("friends").document(currentUser.getUid()).get()
                .addOnSuccessListener(snapshot -> {
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
                        Toast.makeText(getContext(), "群組建立成功！", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "群組建立失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
        return view;
    }
}