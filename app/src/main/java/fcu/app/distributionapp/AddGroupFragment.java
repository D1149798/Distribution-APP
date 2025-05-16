package fcu.app.distributionapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import fcu.app.distributionapp.R;


public class AddGroupFragment extends Fragment {

    private EditText etGroupName;
    private EditText etGroupMembers;
    private Button btnCreateGroup;


    public AddGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_group, container, false);

        etGroupName = view.findViewById(R.id.et_add_group_name);
        etGroupMembers = view.findViewById(R.id.et_add_group_members);
        btnCreateGroup = view.findViewById(R.id.btn_create_group);

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String membersRaw = etGroupMembers.getText().toString().trim();

            if(TextUtils.isEmpty(groupName) || TextUtils.isEmpty(membersRaw)){
                Toast.makeText(getActivity(), "請輸入群組名稱與成員", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        return inflater.inflate(R.layout.fragment_add_group, container, false);
    }
}