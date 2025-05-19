package fcu.app.distributionapp;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fcu.app.distributionapp.adapter.GroupMemberAdapter;
import fcu.app.distributionapp.model.Group;
import fcu.app.distributionapp.model.GroupMember;
import fcu.app.distributionapp.model.Transaction;


public class TransactionDetailFragment extends Fragment {

    private Spinner payerSpinner, currencySpinner;
    private EditText etCost, etNote;
    private TextView tvDate;
    private Button btnSave, btnDelete;
    private RecyclerView rvBeneficiaries;

    private FirebaseFirestore db;
    private GroupMemberAdapter adapter;
    private final List<GroupMember> memberList = new ArrayList<>();
    private final List<String> currencyCodeList = new ArrayList<>();
    private boolean isEditMode = false;

    private Transaction transaction;
    private String groupId = "your_group_id"; // ← 替換成實際 group id

    private Calendar selectedDate = Calendar.getInstance();

    public TransactionDetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();

        payerSpinner = view.findViewById(R.id.payerSpinner);
        currencySpinner = view.findViewById(R.id.currencySpinner);
        rvBeneficiaries = view.findViewById(R.id.rv_beneficiaries);
        etCost = view.findViewById(R.id.et_cost);
        etNote = view.findViewById(R.id.et_note);
        tvDate = view.findViewById(R.id.tv_date);
        btnSave = view.findViewById(R.id.btn_save);
        btnDelete = view.findViewById(R.id.btn_delete);

        adapter = new GroupMemberAdapter(memberList);
        rvBeneficiaries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBeneficiaries.setAdapter(adapter);

        loadCurrencyFromCSV();
        loadGroupMembers(groupId);

        // 預設為新增模式
        isEditMode = transaction == null;
        setEditMode(isEditMode);

        tvDate.setOnClickListener(v -> {
            if (!isEditMode) return;
            showDatePickerDialog();
        });

        btnSave.setOnClickListener(v -> {
            Toast.makeText(getContext(), "儲存功能尚未實作", Toast.LENGTH_SHORT).show();
            // TODO: 儲存到 Firebase
        });

        btnDelete.setOnClickListener(v -> {
            Toast.makeText(getContext(), "刪除功能尚未實作", Toast.LENGTH_SHORT).show();
            // TODO: 從 Firebase 刪除
        });

        return view;
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    tvDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void setEditMode(boolean enabled) {
        etCost.setEnabled(enabled);
        etNote.setEnabled(enabled);
        payerSpinner.setEnabled(enabled);
        currencySpinner.setEnabled(enabled);
        tvDate.setEnabled(enabled);
        btnSave.setVisibility(enabled ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(enabled ? View.VISIBLE : View.GONE);
        adapter.setCheckBoxVisible(enabled);
        adapter.notifyDataSetChanged();
    }

    private void loadCurrencyFromCSV() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(R.raw.currencies)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                currencyCodeList.add(line.trim());
            }
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, currencyCodeList);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGroupMembers(String groupId) {
        db.collection("groups")
                .document(groupId)
                .collection("members")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> members = new ArrayList<>();
                    memberList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        if (name != null) {
                            members.add(name);
                            memberList.add(new GroupMember(name));
                        }
                    }

                    ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, members);
                    payerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    payerSpinner.setAdapter(payerAdapter);
                    adapter.notifyDataSetChanged();

                    if (transaction != null) {
                        fillTransactionData(transaction);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "讀取成員失敗", Toast.LENGTH_SHORT).show());
    }

    private void fillTransactionData(Transaction t) {
        etNote.setText(t.getNote());
        etCost.setText(String.valueOf(t.getAmount()));
        tvDate.setText(t.getDate());

        int payerIndex = getMemberIndex(t.getPayer());
        if (payerIndex != -1) payerSpinner.setSelection(payerIndex);

        int currencyIndex = currencyCodeList.indexOf(t.getCurrency());
        if (currencyIndex != -1) currencySpinner.setSelection(currencyIndex);

        adapter.setSelectedMembers(t.getBeneficiaries());
    }

    private int getMemberIndex(String name) {
        for (int i = 0; i < memberList.size(); i++) {
            if (memberList.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}