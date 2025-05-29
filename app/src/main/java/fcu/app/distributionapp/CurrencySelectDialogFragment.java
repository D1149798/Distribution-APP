package fcu.app.distributionapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CurrencySelectDialogFragment extends DialogFragment {

    public interface CurrencySelectedListener {
        void onCurrencySelected(String currencyCode);
    }

    private CurrencySelectedListener listener;
    private Spinner currencySpinner;
    private final List<String> displayCurrencyList = new ArrayList<>();
    private final List<String> currencyCodeList = new ArrayList<>();

    public CurrencySelectDialogFragment(CurrencySelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_currency_select_dialog, null);
        dialog.setContentView(view);

        currencySpinner = view.findViewById(R.id.spinner_currency);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        loadCurrencyList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, displayCurrencyList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        btnConfirm.setOnClickListener(v -> {
            int selectedIndex = currencySpinner.getSelectedItemPosition();
            if (selectedIndex >= 0 && selectedIndex < currencyCodeList.size()) {
                String selectedCode = currencyCodeList.get(selectedIndex);
                if (listener != null) {
                    listener.onCurrencySelected(selectedCode);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "請選擇有效的貨幣", Toast.LENGTH_SHORT).show();
            }
        });

        return dialog;
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
}
