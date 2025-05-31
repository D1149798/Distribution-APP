package fcu.app.distributionapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import fcu.app.distributionapp.adapter.SettleUpResultAdapter;
import fcu.app.distributionapp.model.Transaction;
import fcu.app.distributionapp.util.ExchangeRateConverter;
import fcu.app.distributionapp.util.SettleCalculator;

public class SettleUpResultFragment extends Fragment {
    private static final String ARG_TRANSACTIONS = "transactions";
    private static final String ARG_TARGET_CURRENCY = "target_currency";
    private static final String ARG_GROUP_ID = "group_id";

    private ArrayList<Transaction> transactions;
    private String targetCurrency;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ImageView ivIcon;

    public static SettleUpResultFragment newInstance(ArrayList<Transaction> transactions, String currencyCode, String groupId) {
        SettleUpResultFragment fragment = new SettleUpResultFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_TRANSACTIONS, transactions);
        args.putString(ARG_TARGET_CURRENCY, currencyCode);
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settle_up_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerSettleResult);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        ivIcon = view.findViewById(R.id.ivTransferIcon);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ivIcon.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));

        if (getArguments() != null) {
            transactions = getArguments().getParcelableArrayList(ARG_TRANSACTIONS);
            targetCurrency = getArguments().getString(ARG_TARGET_CURRENCY);
            convertAllTransactions();
        }
    }

    private void convertAllTransactions() {
        Map<Transaction, Double> convertedMap = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(transactions.size());

        for (Transaction tx : transactions) {
            String fromCurrencyCode = tx.getCurrency().split(",")[0].trim(); // 正確只取 ISO 代碼
            ExchangeRateConverter.fetchExchangeRate(
                    requireContext(),fromCurrencyCode, targetCurrency,
                    rate -> {
                        double convertedAmount = tx.getAmount() * rate;
                        // ✅ 印出轉換 log
                        Log.d("CurrencyConvert", "Transaction: " + tx.getPayer() +
                                ", From: " + fromCurrencyCode +
                                ", To: " + targetCurrency +
                                ", Amount: " + tx.getAmount() +
                                ", Rate: " + rate +
                                ", Converted: " + convertedAmount);
                        synchronized (convertedMap) {
                            convertedMap.put(tx, convertedAmount);
                        }
                        latch.countDown();
                    }
            );
        }

        new Thread(() -> {
            try {
                latch.await();
                requireActivity().runOnUiThread(() -> calculateSettleInstructions(convertedMap));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void calculateSettleInstructions(Map<Transaction, Double> convertedMap) {
        Map<String, Double> balances = new HashMap<>();

        for (Map.Entry<Transaction, Double> entry : convertedMap.entrySet()) {
            Transaction tx = entry.getKey();
            double convertedAmount = entry.getValue();
            String payer = tx.getPayer();
            List<String> beneficiaries = tx.getBeneficiaries();
            double share = convertedAmount / beneficiaries.size();

            // Log 每筆交易各人負擔
            Log.d("TransactionSplit", "交易付款人: " + payer + " 總額: " + convertedAmount);
            for (String person : beneficiaries) {
                Log.d("TransactionSplit", "  受益人: " + person + " 負擔份額: " + share);
            }

            balances.put(payer, balances.getOrDefault(payer, 0.0) + convertedAmount);
            for (String person : beneficiaries) {
                balances.put(person, balances.getOrDefault(person, 0.0) - share);
            }
        }

        // 加這段印出每人餘額
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            Log.d("SettleBalance", entry.getKey() + " 的餘額: " + entry.getValue());
        }

        List<String> resultList = SettleCalculator.calculateInstructions(balances, targetCurrency);
        progressBar.setVisibility(View.GONE);

        if (resultList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setAdapter(new SettleUpResultAdapter(resultList));
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
