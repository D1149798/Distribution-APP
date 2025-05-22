package fcu.app.distributionapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import fcu.app.distributionapp.adapter.GroupMemberAdapter;
import fcu.app.distributionapp.model.GroupMember;
import fcu.app.distributionapp.model.Transaction;

public class EditTransactionFragment extends Fragment {

    private static final String ARG_TRANSACTION = "arg_transaction";
    private Transaction transaction;
    private String groupId;
    private Spinner payerSpinner, currencySpinner;
    private EditText etCost, etNote;
    private TextView tvDate;
    private Button btnSave, btnDelete;
    private RecyclerView rvBeneficiaries;

    private FirebaseFirestore db;
    private GroupMemberAdapter adapter;
    private final List<GroupMember> memberList = new ArrayList<>();
    private final List<String> currencyCodeList = new ArrayList<>();
    private boolean isEditMode;

    private Calendar selectedDate = Calendar.getInstance();

    public EditTransactionFragment() {}

    public static EditTransactionFragment newInstance(Transaction transaction, String groupId) {
        EditTransactionFragment fragment = new EditTransactionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRANSACTION, transaction);
        args.putString("groupId", groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transaction = getArguments().getParcelable(ARG_TRANSACTION);
            groupId = getArguments().getString("groupId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_transaction, container, false);

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();
        selectedDate = Calendar.getInstance();

        payerSpinner = view.findViewById(R.id.payerSpinner);
        currencySpinner = view.findViewById(R.id.currencySpinner);
        rvBeneficiaries = view.findViewById(R.id.rv_beneficiaries);
        etCost = view.findViewById(R.id.et_cost);
        etNote = view.findViewById(R.id.et_note);
        tvDate = view.findViewById(R.id.tv_date);
        btnSave = view.findViewById(R.id.btn_save);
        btnDelete = view.findViewById(R.id.btn_delete);

        adapter = new GroupMemberAdapter(memberList,false);
        rvBeneficiaries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBeneficiaries.setAdapter(adapter);

        if (getArguments() != null) {
            transaction = getArguments().getParcelable(ARG_TRANSACTION);
            groupId = getArguments().getString("groupId");
        }

        loadCurrencyFromCSV();
        loadGroupMembers(groupId);
//        if (transaction != null) {
//            fillTransactionData(transaction);
//        }

        if (transaction != null) {
            etCost.setText(String.valueOf(transaction.getAmount()));
            etNote.setText(transaction.getNote());
            tvDate.setText(transaction.getDate());
        } else {
            // 新增模式，自動填入今天日期
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
            tvDate.setText(today);
        }
        tvDate.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year1, month1, dayOfMonth) -> {
                selectedDate.set(year1, month1, dayOfMonth);
                String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                tvDate.setText(dateString);
            }, year, month, day);

            datePickerDialog.show();
        });

        btnSave.setOnClickListener(v -> onSaveButtonClick());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("確定刪除？")
                    .setMessage("刪除後將無法復原，是否確定？")
                    .setPositiveButton("刪除", (dialog, which) -> onDeleteButtonClick())
                    .setNegativeButton("取消", null)
                    .show();
        });

        return view;
    }
    private void onSaveButtonClick() {
        String amountStr = etCost.getText().toString();
        String description = etNote.getText().toString();
        String date = tvDate.getText().toString();

        if (amountStr.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "請填寫所有欄位", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String payer = payerSpinner.getSelectedItem().toString();
        String currency = currencySpinner.getSelectedItem().toString();

        List<String> selectedBeneficiaries = new ArrayList<>();
        for (GroupMember member : memberList) {
            if (member.isSelected()) {
                selectedBeneficiaries.add(member.getName());
            }
        }

        if (transaction == null) {
            transaction = new Transaction(); // 建立新交易
        }

        transaction.setAmount(amount);
        transaction.setNote(description);
        transaction.setDate(date);
        transaction.setPayer(payer);
        transaction.setCurrency(currency);
        transaction.setBeneficiaries(selectedBeneficiaries);

        db.collection("groups").document(groupId)
                .collection("transactions")
                .document(transaction.getId() != null ? transaction.getId() : db.collection("transactions").document().getId())
                .set(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "儲存成功", Toast.LENGTH_SHORT).show();
                    requireParentFragment().getChildFragmentManager().popBackStack();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "儲存失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onDeleteButtonClick() {
        if (transaction != null && transaction.getId() != null) {
            db.collection("groups").document(groupId)
                    .collection("transactions").document(transaction.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "已刪除交易", Toast.LENGTH_SHORT).show();
                        requireParentFragment().getChildFragmentManager().popBackStack(); // 回到 TransactionDetailFragment
                        requireParentFragment().getChildFragmentManager().popBackStack(); // 再回到 TransactionHistoryFragment

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "刪除失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
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

        for (GroupMember member : memberList) {
            if (t.getBeneficiaries() != null && t.getBeneficiaries().contains(member.getName())) {
                member.setSelected(true);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private int getMemberIndex(String name) {
        for (int i = 0; i < payerSpinner.getCount(); i++) {
            if (payerSpinner.getItemAtPosition(i).toString().equals(name)) {
                return i;
            }
        }
        return -1;
    }

}
