package fcu.app.distributionapp;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fcu.app.distributionapp.adapter.GroupMemberAdapter;
import fcu.app.distributionapp.model.GroupMember;

public class AddTransactionFragment extends Fragment {

    private Spinner groupSpinner, payerSpinner, categorySpinner, currencySpinner;
    private RecyclerView beneficiaryRecyclerView;
    private GroupMemberAdapter adapter;
    private List<GroupMember> memberList = new ArrayList<>();

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

        groupSpinner = view.findViewById(R.id.groupSpinner);
        payerSpinner = view.findViewById(R.id.payerSpinner);
        currencySpinner = view.findViewById(R.id.currencySpinner);
        beneficiaryRecyclerView = view.findViewById(R.id.rv_beneficiaries);
        etCost = view.findViewById(R.id.et_cost);
        etNote = view.findViewById(R.id.et_note);
        tvDate = view.findViewById(R.id.tv_date);
        btnSave = view.findViewById(R.id.btn_save);

        // 範例選單資料
        String[] groupList = {"旅遊小組", "家庭", "同事", "朋友"};
        String[] payerList = {"小明", "小華", "小美"};
        String[] currencyList = {"TWD", "USD", "JPY", "EUR"};

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, groupList);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);

        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, payerList);
        payerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payerSpinner.setAdapter(payerAdapter);

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, currencyList);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        // 假資料
        memberList.clear();
        memberList.add(new GroupMember("小明"));
        memberList.add(new GroupMember("小華"));
        memberList.add(new GroupMember("小美"));
        memberList.add(new GroupMember("小綠"));

        adapter = new GroupMemberAdapter(memberList);
        beneficiaryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        beneficiaryRecyclerView.setAdapter(adapter);

        // 日期選擇
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
            String cost = etCost.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String date = tvDate.getText().toString().trim();

            if (cost.isEmpty() || date.equals("請選擇日期")) {
                Toast.makeText(getContext(), "請填寫金額與日期", Toast.LENGTH_SHORT).show();
                return;
            }

            List<GroupMember> selected = adapter.getSelectedMembers();
            Toast.makeText(getContext(),
                    "已儲存交易，受益人數量：" + selected.size(),
                    Toast.LENGTH_SHORT).show();

            // 可加入儲存邏輯到資料庫
        });
    }

    public List<GroupMember> getSelectedBeneficiaries() {
        return adapter.getSelectedMembers();
    }
}
