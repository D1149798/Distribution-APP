package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.*;

public class ExchangeRateFragment extends Fragment {

    private EditText etAmount;
    private Spinner spinnerFrom, spinnerTo;
    private Button btnConvert,btnSwitch;
    private TextView tvResult;

    private LineChart lineChart;
    private final String[] currencies = {"USD", "EUR", "JPY", "TWD"};
    private final OkHttpClient client = new OkHttpClient();

    public ExchangeRateFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exchange_rate, container, false);

        etAmount = view.findViewById(R.id.etAmount);
        spinnerFrom = view.findViewById(R.id.spinnerFrom);
        spinnerTo = view.findViewById(R.id.spinnerTo);
        btnConvert = view.findViewById(R.id.btnConvert);
        tvResult = view.findViewById(R.id.tvResult);
        btnSwitch = view.findViewById(R.id.btnSwitch);
        lineChart = view.findViewById(R.id.lineChart);

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
            fetchHistoricalRates(from, to);
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
                .addHeader("apikey", "rKTh2VcZ8CUXhp7AzUaOJb5ZST1mUrtS")  // 替換為你自己的 key
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> tvResult.setText("錯誤：" + e.getMessage()));
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        double result = obj.getDouble("result");

                        requireActivity().runOnUiThread(() ->
                                tvResult.setText(String.format("%.2f %s = %.2f %s", amount, base, result, target)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> tvResult.setText("解析錯誤"));
                    }
                } else {
                    requireActivity().runOnUiThread(() -> tvResult.setText("API 呼叫失敗"));
                }
            }
        });
    }
    private void fetchCurrencySymbols() {
        String url = "https://api.exchangerate.host/symbols";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json).getJSONObject("symbols");

                        List<String> currencyList = new ArrayList<>();
                        Iterator<String> keys = obj.keys();
                        while (keys.hasNext()) {
                            currencyList.add(keys.next());
                        }
                        Collections.sort(currencyList); // 排序

                        requireActivity().runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, currencyList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerFrom.setAdapter(adapter);
                            spinnerTo.setAdapter(adapter);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void fetchHistoricalRates(String base, String target) {
        String url = "https://api.exchangerate.host/timeseries?start_date=2024-05-06&end_date=2024-05-13&base="
                + base + "&symbols=" + target;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject rates = new JSONObject(json).getJSONObject("rates");

                        List<Entry> entries = new ArrayList<>();
                        List<String> dates = new ArrayList<>();
                        int index = 0;

                        Iterator<String> keys = rates.keys();
                        List<String> sortedDates = new ArrayList<>();
                        while (keys.hasNext()) sortedDates.add(keys.next());
                        Collections.sort(sortedDates);

                        for (String date : sortedDates) {
                            double rate = rates.getJSONObject(date).getDouble(target);
                            entries.add(new Entry(index++, (float) rate));
                            dates.add(date);
                        }

                        LineDataSet dataSet = new LineDataSet(entries, base + " to " + target);
                        LineData lineData = new LineData(dataSet);

                        requireActivity().runOnUiThread(() -> {
                            lineChart.setData(lineData);
                            lineChart.invalidate(); // refresh
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }




}