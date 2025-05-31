package fcu.app.distributionapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import fcu.app.distributionapp.util.ExchangeRateConverter;

public class ExchangeRateFragment extends Fragment {

    private EditText etAmount;
    private AutoCompleteTextView autoCompleteFrom, autoCompleteTo;
    private Button btnConvert, btnSwitch;
    private TextView tvResult, tvRealTimeRate, tvLastUpdate;

    private final List<String> displayCurrencyList = new ArrayList<>();
    private final List<String> currencyCodeList = new ArrayList<>();

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

            convertAmount(amount, fromCode, toCode);
        });

        btnSwitch.setOnClickListener(v -> {
            CharSequence from = autoCompleteFrom.getText();
            CharSequence to = autoCompleteTo.getText();
            autoCompleteFrom.setText(to);
            autoCompleteTo.setText(from);
        });

        return view;
    }

    private void convertAmount(double amount, String from, String to) {
        Context context = requireContext();
        ExchangeRateConverter.fetchExchangeRate(context, from, to, rate -> {
            requireActivity().runOnUiThread(() -> {
                if (rate == 0) {
                    tvResult.setText("取得匯率失敗");
                    return;
                }

                double result = amount * rate;
                tvResult.setText(String.format(Locale.getDefault(), "%.2f %s = %.2f %s", amount, from, result, to));
                tvRealTimeRate.setText(String.format(Locale.getDefault(), "即時匯率：1 %s = %.4f %s", from, rate, to));

                // 顯示最後更新時間（從快取取得 timestamp）
                SharedPreferences prefs = context.getSharedPreferences("exchange_rate_cache", Context.MODE_PRIVATE);
                String key = from + "_" + to + "_timestamp";
                long timestamp = prefs.getLong(key, 0);
                if (timestamp > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String updateTime = sdf.format(new Date(timestamp));
                    tvLastUpdate.setText("最後更新時間：" + updateTime);
                } else {
                    tvLastUpdate.setText("尚未更新匯率");
                }
            });
        });
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
}
