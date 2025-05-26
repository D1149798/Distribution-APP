package fcu.app.distributionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import fcu.app.distributionapp.adapter.GroupMemberAdapter;
import fcu.app.distributionapp.model.GroupMember;
import fcu.app.distributionapp.model.Transaction;

public class TransactionDetailFragment extends Fragment {
    private static final String ARG_TRANSACTION_ID = "arg_transaction_id";

    private String transactionId;
    private String groupId;
    private Transaction transaction;

    private TextView tvNote, tvAmount, tvDate, tvPayer, tvCurrency;

    private RecyclerView rvBeneficiaries;
    private Button btnEdit;

    private FirebaseFirestore firestore;

    public static TransactionDetailFragment newInstance(String transactionId, String groupId) {
        TransactionDetailFragment fragment = new TransactionDetailFragment();
        Bundle args = new Bundle();
        args.putString("transaction_id", transactionId);
        args.putString("group_id", groupId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionId = getArguments().getString("transaction_id");
            groupId = getArguments().getString("group_id");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        // Initialize views
        tvNote = view.findViewById(R.id.tv_note);
        tvAmount = view.findViewById(R.id.tv_amount);
        tvDate = view.findViewById(R.id.tv_date);
        tvPayer = view.findViewById(R.id.tv_payer);
        tvCurrency = view.findViewById(R.id.tv_currency);
        rvBeneficiaries = view.findViewById(R.id.rv_beneficiaries);
        btnEdit = view.findViewById(R.id.btn_edit);

        // Set up RecyclerView layout
        rvBeneficiaries.setLayoutManager(new LinearLayoutManager(getContext()));

        btnEdit.setOnClickListener(v -> {
            if (transaction != null) {
                Fragment editFragment = EditTransactionFragment.newInstance(transaction,groupId);
                requireParentFragment().getChildFragmentManager().beginTransaction()
                        .replace(R.id.childFragmentContainer, editFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Transaction data not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        if (transactionId != null) {
            loadTransactionFromFirestore(transactionId);
        } else {
            Toast.makeText(getContext(), "No transaction ID provided", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadTransactionFromFirestore(String transactionId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("newGroups")
                .document(groupId)
                .collection("transactions")
                .document(transactionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        transaction = documentSnapshot.toObject(Transaction.class);
                        if (transaction != null) {
                            transaction.setId(documentSnapshot.getId());
                            displayTransactionData(transaction);
                        }
                    } else {
                        Toast.makeText(getContext(), "找不到交易資料", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "讀取交易失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());

    }


    private void displayTransactionData(Transaction transaction) {
        tvAmount.setText(String.valueOf(transaction.getAmount()));
        tvDate.setText(transaction.getDate());
        tvPayer.setText(transaction.getPayer());
        tvCurrency.setText(transaction.getCurrency());
        tvNote.setText(transaction.getNote());

        List<String> beneficiaryNames = transaction.getBeneficiaries();
        if (beneficiaryNames != null && !beneficiaryNames.isEmpty()) {
            List<GroupMember> beneficiaryMembers = new ArrayList<>();
            for (String name : beneficiaryNames) {
                beneficiaryMembers.add(new GroupMember(name));
            }
            GroupMemberAdapter adapter = new GroupMemberAdapter(beneficiaryMembers, true);
            rvBeneficiaries.setAdapter(adapter);

        }
    }
}
