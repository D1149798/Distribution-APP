package fcu.app.distributionapp.util;

import android.content.Context;
import android.content.SharedPreferences;
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

    private static final String PREFS_NAME = "exchange_rate_cache";
    private static final String API_KEY = "ac5917a34ee33953c3c134fc5ef002b0"; // 請換成你的 API Key
    private static final long CACHE_VALID_DAYS = 3;
    private static final String BASE_URL = "https://api.exchangerate.host/convert";

    public interface ExchangeRateCallback {
        void onResult(double rate);
    }

    public static void fetchExchangeRate(Context context, String from, String to, ExchangeRateCallback callback) {
        if (from.equalsIgnoreCase(to)) {
            Log.d("ExchangeRate", "Same currency, rate = 1.0");
            callback.onResult(1.0);
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String keyRate = from + "_" + to + "_rate";
        String keyTimestamp = from + "_" + to + "_timestamp";

        long now = System.currentTimeMillis();
        long cachedTimestamp = prefs.getLong(keyTimestamp, 0);
        long daysDiff = (now - cachedTimestamp) / (1000 * 60 * 60 * 24);
        double cachedRate = Double.longBitsToDouble(prefs.getLong(keyRate, Double.doubleToLongBits(0)));

        if (daysDiff < CACHE_VALID_DAYS && cachedRate > 0) {
            Log.d("ExchangeRate", "Use cached rate: " + cachedRate + " for " + from + "->" + to);
            callback.onResult(cachedRate);
            return;
        }

        String url = BASE_URL
                + "?from=" + from
                + "&to=" + to
                + "&amount=1"
                + "&access_key=" + API_KEY;

        Log.d("ExchangeRate", "Fetching rate from " + from + " to " + to);
        Log.d("ExchangeRate", "Request URL: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
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
                        Log.d("ExchangeRate", "Response JSON: " + json);

                        JSONObject jsonObject = new JSONObject(json);
                        boolean success = jsonObject.optBoolean("success", false);
                        if (!success) {
                            Log.e("ExchangeRate", "API error: " + json);
                            callback.onResult(0);
                            return;
                        }

                        double result = jsonObject.getDouble("result");

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong(keyRate, Double.doubleToLongBits(result));
                        editor.putLong(keyTimestamp, System.currentTimeMillis());
                        editor.apply();

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
}
