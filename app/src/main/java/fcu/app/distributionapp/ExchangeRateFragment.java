package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import android.widget.*;
import androidx.annotation.NonNull;

import org.json.JSONObject;
import java.io.IOException;

import okhttp3.*;

public class ExchangeRateFragment extends Fragment {

    private EditText etAmount;
    private Spinner spinnerFrom, spinnerTo;
    private Button btnConvert, btnSwitch;
    private TextView tvResult;

    private final String[] currencies = {"USD", "EUR", "JPY", "TWD"};
    private final OkHttpClient client = new OkHttpClient();

    public ExchangeRateFragment() {}

    public static ExchangeRateFragment newInstance() {
        return new ExchangeRateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange_rate, container, false);

        etAmount = view.findViewById(R.id.etAmount);
        spinnerFrom = view.findViewById(R.id.spinnerFrom);
        spinnerTo = view.findViewById(R.id.spinnerTo);
        btnConvert = view.findViewById(R.id.btnConvert);
        tvResult = view.findViewById(R.id.tvResult);
        btnSwitch = view.findViewById(R.id.btnSwitch);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(3); // 預設 USD -> TWD

        btnConvert.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "請輸入金額", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String from = spinnerFrom.getSelectedItem().toString();
            String to = spinnerTo.getSelectedItem().toString();
            fetchExchangeRate(amount, from, to);
        });

        btnSwitch.setOnClickListener(v -> {
            int fromPosition = spinnerFrom.getSelectedItemPosition();
            int toPosition = spinnerTo.getSelectedItemPosition();
            spinnerFrom.setSelection(toPosition);
            spinnerTo.setSelection(fromPosition);
        });

        return view;
    }

    private void fetchExchangeRate(double amount, String base, String target) {
        String url = "https://api.apilayer.com/exchangerates_data/convert?from=" + base + "&to=" + target + "&amount=" + amount;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", "rKTh2VcZ8CUXhp7AzUaOJb5ZST1mUrtS") // 請替換成你自己的 API key
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        tvResult.setText("錯誤：" + e.getMessage()));
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        double result = obj.getDouble("result");

                        requireActivity().runOnUiThread(() ->
                                tvResult.setText(String.format(Locale.getDefault(), "%.2f %s = %.2f %s", amount, base, result, target)));
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> tvResult.setText("解析錯誤"));
                    }
                } else {
                    requireActivity().runOnUiThread(() -> tvResult.setText("API 呼叫失敗"));
                }
            }
        });
    }
}
