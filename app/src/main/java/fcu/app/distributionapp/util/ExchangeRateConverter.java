package fcu.app.distributionapp.util;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExchangeRateConverter {

    private static final String API_KEY = "rKTh2VcZ8CUXhp7AzUaOJb5ZST1mUrtS"; // ✅ 請換成你自己的

    public static void fetchExchangeRate(String from, String to, ExchangeRateCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.apilayer.com/exchangerates_data/convert?from=" + from + "&to=" + to + "&amount=1";
        Log.d("ExchangeRate", "Fetching rate from " + from + " to " + to);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ExchangeRate", "API failed: " + e.getMessage());
                callback.onResult(0);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        double result = jsonObject.getDouble("result");
                        callback.onResult(result);
                    } catch (Exception e) {
                        Log.e("ExchangeRate", "Parsing error: " + e.getMessage());
                        callback.onResult(0);
                    }
                } else {
                    Log.e("ExchangeRate", "Response error: " + response.code() + " - " + response.body().string());

                    callback.onResult(0);
                }
            }
        });
    }

    public interface ExchangeRateCallback {
        void onResult(double rate);
    }
}
