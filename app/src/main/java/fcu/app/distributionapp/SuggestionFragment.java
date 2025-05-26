package fcu.app.distributionapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class SuggestionFragment extends Fragment {
    private EditText etSearch;
    private Button btnSearch;
    private WebView webView;
    private String firstSearchUrl = null;
    private boolean isApp = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestion, container, false);

        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);
        webView = view.findViewById(R.id.web_view);

        // 啟用 JavaScript（Google 搜尋需要）
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()); // 讓點擊連結不跳出瀏覽器

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                String url = "https://www.google.com/search?q=" + query;
                firstSearchUrl = url;
                webView.loadUrl(url);

                //清空搜尋框內容並隱藏搜尋框和按鈕
                etSearch.setText("");
                etSearch.setVisibility(View.GONE);
                btnSearch.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);

                isApp = false;
            }
        });
        return view;
    }

    public boolean canGoBack() {
        return (webView != null && webView.canGoBack()) || !isApp;
    }

    public void goBack() {
        if (webView != null && webView.canGoBack()) {
            // 若當前網址是搜尋首頁，就回到粉紅頁面
            webView.evaluateJavascript(
                    "(function() { return window.location.href; })();",
                    currentUrl -> {
                        // currentUrl包在引號內，要移除引號
                        currentUrl = currentUrl.replaceAll("^\"|\"$", "");

                        if (currentUrl.equals(firstSearchUrl)) {
                            // 回到粉紅頁面（隱藏 WebView）
                            etSearch.setVisibility(View.VISIBLE);
                            btnSearch.setVisibility(View.VISIBLE);
                            webView.setVisibility(View.GONE);
                            isApp = true;
                        } else {
                            webView.goBack();
                        }
                    }
            );
        } else if (!isApp) {
            // WebView 已無法 goBack，但仍非 App 狀態 → 回粉紅頁面
            etSearch.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            isApp = true;
        }
    }

}