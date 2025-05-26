package fcu.app.distributionapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class CurrencySelectDialogFragment extends DialogFragment {

    public interface OnCurrencySelectedListener {
        void onCurrencySelected(String selectedCurrency);
    }
    private Spinner spinner;
    private OnCurrencySelectedListener listener;

    public CurrencySelectDialogFragment(OnCurrencySelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_currency_select_dialog, null);
        spinner = view.findViewById(R.id.spinner_currency);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"TWD", "USD", "JPY"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        return new AlertDialog.Builder(requireContext())
                .setTitle("選擇幣別")
                .setView(view)
                .setPositiveButton("確定", (dialog, which) -> {
                    String selectedCurrency = spinner.getSelectedItem().toString();
                    listener.onCurrencySelected(selectedCurrency);
                })
                .setNegativeButton("取消", null)
                .create();
    }
}