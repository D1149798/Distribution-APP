package fcu.app.distributionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.*;

public class ExchangeRateFragment extends Fragment {

    private EditText etAmount;
    private AutoCompleteTextView autoCompleteFrom, autoCompleteTo;
    private Button btnConvert, btnSwitch;
    private TextView tvResult, tvRealTimeRate, tvLastUpdate;

    private final List<String> displayCurrencyList = new ArrayList<>();
    private final List<String> currencyCodeList = new ArrayList<>();
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
        autoCompleteFrom = view.findViewById(R.id.autoCompleteFrom);
        autoCompleteTo = view.findViewById(R.id.autoCompleteTo);
        btnConvert = view.findViewById(R.id.btnConvert);
        btnSwitch = view.findViewById(R.id.btnSwitch);
        tvResult = view.findViewById(R.id.tvResult);
        tvRealTimeRate = view.findViewById(R.id.tvRealTimeRate);
        tvLastUpdate = view.findViewById(R.id.tvLastUpdate);

        loadCurrencyList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, displayCurrencyList);
        autoCompleteFrom.setAdapter(adapter);
        autoCompleteTo.setAdapter(adapter);
        autoCompleteFrom.setThreshold(1);
        autoCompleteTo.setThreshold(1);

        // 預設值
        autoCompleteFrom.setText(getDisplayCurrency("USD"));
        autoCompleteTo.setText(getDisplayCurrency("TWD"));

        btnConvert.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "請輸入金額", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String fromCode = getCurrencyCodeFromDisplay(autoCompleteFrom.getText().toString());
            String toCode = getCurrencyCodeFromDisplay(autoCompleteTo.getText().toString());

            if (fromCode == null || toCode == null) {
                Toast.makeText(getContext(), "請選擇有效的貨幣", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchExchangeRate(amount, fromCode, toCode);
        });

        btnSwitch.setOnClickListener(v -> {
            CharSequence from = autoCompleteFrom.getText();
            CharSequence to = autoCompleteTo.getText();
            autoCompleteFrom.setText(to);
            autoCompleteTo.setText(from);
        });

        return view;
    }

    private void loadCurrencyList() {
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
        }
    }

    private String getDisplayCurrency(String code) {
        for (String item : displayCurrencyList) {
            if (item.startsWith(code + " ")) {
                return item;
            }
        }
        return "";
    }

    private String getCurrencyCodeFromDisplay(String display) {
        if (display.contains(" - ")) {
            return display.split(" - ")[0].trim();
        }
        return null;
    }

    private void fetchExchangeRate(double amount, String base, String target) {
        String url = "https://api.apilayer.com/exchangerates_data/convert?from=" + base + "&to=" + target + "&amount=" + amount;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", "rKTh2VcZ8CUXhp7AzUaOJb5ZST1mUrtS") // ⚠️ 請改成你自己的 API key
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
                        double rate = obj.getJSONObject("info").getDouble("rate");
                        long timestamp = obj.getJSONObject("info").getLong("timestamp");

                        String formattedTime = formatTimestamp(timestamp);

                        requireActivity().runOnUiThread(() -> {
                            tvResult.setText(String.format(Locale.getDefault(), "%.2f %s = %.2f %s", amount, base, result, target));
                            tvRealTimeRate.setText(String.format("即時匯率：1 %s = %.4f %s", base, rate, target));
                            tvLastUpdate.setText("最後更新時間：" + formattedTime);
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> tvResult.setText("解析錯誤"));
                    }
                } else {
                    requireActivity().runOnUiThread(() -> tvResult.setText("API 呼叫失敗"));
                }
            }
        });
    }

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
}
