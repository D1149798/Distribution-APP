package fcu.app.distributionapp;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fcu.app.distributionapp.adapter.GroupMemberAdapter;
import fcu.app.distributionapp.model.GroupMember;
import fcu.app.distributionapp.model.Group;

public class AddTransactionFragment extends Fragment {
    private FirebaseFirestore db;
    private Spinner groupSpinner, payerSpinner, currencySpinner;
    private RecyclerView beneficiaryRecyclerView;
    private GroupMemberAdapter adapter;
    private List<GroupMember> memberList = new ArrayList<>();
    private final List<String> displayCurrencyList = new ArrayList<>();
    private final List<String> currencyCodeList = new ArrayList<>();

    private List<Group> groupList = new ArrayList<>();

    private EditText etCost, etNote;
    private TextView tvDate;
    private Button btnSave;

    private Calendar selectedDate = Calendar.getInstance();

    public AddTransactionFragment() {
        // Required empty public constructor
    }

    public static AddTransactionFragment newInstance() {
        return new AddTransactionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();

        groupSpinner = view.findViewById(R.id.groupSpinner);
        payerSpinner = view.findViewById(R.id.payerSpinner);
        currencySpinner = view.findViewById(R.id.currencySpinner);
        beneficiaryRecyclerView = view.findViewById(R.id.rv_beneficiaries);
        etCost = view.findViewById(R.id.et_cost);
        etNote = view.findViewById(R.id.et_note);
        tvDate = view.findViewById(R.id.tv_date);
        btnSave = view.findViewById(R.id.btn_save);

        loadCurrencyFromCSV();

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, displayCurrencyList);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        // 初始 RecyclerView
        adapter = new GroupMemberAdapter(memberList,false);
        beneficiaryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        beneficiaryRecyclerView.setAdapter(adapter);

        // 載入群組資料
        loadGroupsFromFirestore();

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Group selectedGroup = (Group) parent.getItemAtPosition(position);
                loadGroupMembers(selectedGroup.getId()); // 用 groupId 查詢
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // 日期選擇器
        tvDate.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, y, m, d) -> {
                selectedDate.set(y, m, d);
                tvDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d));
            }, year, month, day);

            datePickerDialog.show();
        });

        // 儲存按鈕
        btnSave.setOnClickListener(v -> {
            String amount = etCost.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String date = tvDate.getText().toString().trim();

            if (groupSpinner.getSelectedItem() == null) {
                Toast.makeText(getContext(), "請選擇群組", Toast.LENGTH_SHORT).show();
                return;
            }

            Group selectedGroup = groupList.get(groupSpinner.getSelectedItemPosition());
            String groupId = selectedGroup.getId();
            String groupName = selectedGroup.getName();
            String payer = payerSpinner.getSelectedItem().toString();
            int currencyIndex = currencySpinner.getSelectedItemPosition();
            String currency = currencyCodeList.get(currencyIndex);

            if (amount.isEmpty() || date.equals("請選擇日期")) {
                Toast.makeText(getContext(), "請填寫金額與日期", Toast.LENGTH_SHORT).show();
                return;
            }

            List<GroupMember> selected = adapter.getSelectedMembers();
            List<String> beneficiaries = new ArrayList<>();
            for (GroupMember member : selected) {
                beneficiaries.add(member.getName());
            }

            // 建立資料 Map
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("group", groupName);
            transaction.put("payer", payer);
            transaction.put("currency", currency);
            transaction.put("amount", Double.parseDouble(amount));
            transaction.put("note", note);
            transaction.put("date", date);
            transaction.put("beneficiaries", beneficiaries);

            // 儲存到 groupId/transactions 子集合
            db.collection("newGroups")
                    .document(groupId)
                    .collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "交易已儲存", Toast.LENGTH_SHORT).show();
                        etCost.setText("");
                        etNote.setText("");
                        tvDate.setText("請選擇日期");
                        adapter.clearSelection();
                    }).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "儲存失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    public List<GroupMember> getSelectedBeneficiaries() {
        return adapter.getSelectedMembers();
    }

    private void loadCurrencyFromCSV() {
        try {
            InputStream is = getResources().openRawResource(R.raw.currencies);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    currencyCodeList.add(code);
                    displayCurrencyList.add(code + " - " + name);
                }
            }
            reader.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), "讀取 currencies.csv 失敗", Toast.LENGTH_LONG).show();
            Log.e("CurrencyCSV", "讀取 currencies.csv 發生錯誤", e);
        }
    }

    private void loadGroupsFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = user.getUid();

        db.collection("newGroups")
                .whereArrayContains("members", currentUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    groupList.clear();
                    for (var doc : querySnapshot) {
                        String groupName = doc.getString("name");
                        String groupId = doc.getId();
                        groupList.add(new Group(groupId, groupName));
                    }

                    ArrayAdapter<Group> groupAdapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, groupList);
                    groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    groupSpinner.setAdapter(groupAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "載入群組失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "載入群組失敗", e);
                });
    }

    private void loadGroupMembers(String groupId) {
        db.collection("newGroups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> uidList = (List<String>) documentSnapshot.get("members");
                    if (uidList == null) uidList = new ArrayList<>();

                    int finalCount = uidList.size();
                    List<String> displayNames = new ArrayList<>();
                    memberList.clear();

                    for (String uid : uidList) {
                        db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("Name");
                                    if (name != null) {
                                        displayNames.add(name);
                                        memberList.add(new GroupMember(name));
                                    }

                                    // 檢查是否所有 UID 都處理完成
                                    if (displayNames.size() + (finalCount - memberList.size()) == finalCount) {
                                        // 更新 payerSpinner
                                        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(requireContext(),
                                                android.R.layout.simple_spinner_item, displayNames);
                                        payerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        payerSpinner.setAdapter(payerAdapter);

                                        adapter.clearSelection();
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "載入群組成員失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "載入成員失敗", e);
                });
    }


}
