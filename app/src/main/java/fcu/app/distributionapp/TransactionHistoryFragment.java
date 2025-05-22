package fcu.app.distributionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import fcu.app.distributionapp.adapter.TransactionAdapter;
import fcu.app.distributionapp.model.Transaction;

public class TransactionHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button settleButton;
    private List<Transaction> transactions = new ArrayList<>();
    private TransactionAdapter adapter;
//    private String groupId;
    private String groupId = "jY6sXapfNGoRJttUaBjD";
    public TransactionHistoryFragment() {
        // Required empty public constructor
    }

    // 外部呼叫設定 groupId（在加入此 fragment 前調用）
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        recyclerView = view.findViewById(R.id.rv_transaction_list);
        settleButton = view.findViewById(R.id.btn_settle);

//        adapter = new TransactionAdapter(transactions, transaction -> {
//            // 點擊某筆紀錄 → 跳到詳細頁
//            Fragment detailFragment = TransactionDetailFragment.newInstance(transaction);
//            requireActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.childFragmentContainer, detailFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });

        adapter = new TransactionAdapter(transactions, transaction -> {
            // 點擊某筆紀錄 → 跳到詳細頁
            TransactionDetailFragment detailFragment = TransactionDetailFragment.newInstance(transaction.getId(), groupId);
            requireParentFragment().getChildFragmentManager().beginTransaction()
                    .replace(R.id.childFragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit();

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (groupId != null) {
            loadTransactions();
        } else {
            Toast.makeText(getContext(), "未提供群組 ID", Toast.LENGTH_SHORT).show();
        }

        settleButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "尚未實作結算功能", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void loadTransactions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("groups")
                .document(groupId)
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    transactions.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) {
                            t.setId(doc.getId());
                            transactions.add(t);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "載入交易失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
