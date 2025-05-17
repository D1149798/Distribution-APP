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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestion, container, false);

        EditText etSearch = view.findViewById(R.id.et_search);
        Button btnSearch = view.findViewById(R.id.btn_search);
        webView = view.findViewById(R.id.web_view);

        // 啟用 JavaScript（Google 搜尋需要）
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()); // 讓點擊連結不跳出瀏覽器

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                String url = "https://www.google.com/search?q=" + query;
                webView.loadUrl(url);

                //清空搜尋框內容並隱藏搜尋框和按鈕
                etSearch.setText("");
                etSearch.setVisibility(View.GONE);
                btnSearch.setVisibility(View.GONE);
            }
        });
        return view;
    }
}