package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

//lily@gmail.com
//123456
public class AccountsFragment extends Fragment {
    private View selectedMenu = null;

    public AccountsFragment() {
        // Required empty public constructor
    }

    public static AccountsFragment newInstance(String param1, String param2) {
        AccountsFragment fragment = new AccountsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private List<TextView> allLabels = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        // 預設載入 ExpenseFragment
        loadChildFragment(new ExpenseFragment());

        TextView labelChat = view.findViewById(R.id.label_chat);
        TextView labelTransaction = view.findViewById(R.id.label_transaction);
        TextView labelAdd = view.findViewById(R.id.label_add);
        TextView labelRate = view.findViewById(R.id.label_rate);

        // 加入所有文字到 list
        allLabels.add(labelChat);
        allLabels.add(labelTransaction);
        allLabels.add(labelAdd);
        allLabels.add(labelRate);

        // 設定按鈕切換事件
        view.findViewById(R.id.btn_chat).setOnClickListener(v ->{
            loadChildFragment(new ExpenseFragment());
            highlightSelected(view.findViewById(R.id.btn_chat), view.findViewById(R.id.label_chat));
        });

        view.findViewById(R.id.btn_transaction).setOnClickListener(v ->{
            loadChildFragment(new TransactionHistoryFragment());
            highlightSelected(view.findViewById(R.id.btn_transaction), view.findViewById(R.id.label_transaction));
        });

        view.findViewById(R.id.btn_add).setOnClickListener(v ->{
            loadChildFragment(new AddTransactionFragment());
            highlightSelected(view.findViewById(R.id.btn_add), view.findViewById(R.id.label_add));
        });


        view.findViewById(R.id.btn_rate).setOnClickListener(v ->{
            loadChildFragment(new ExchangeRateFragment());
            highlightSelected(view.findViewById(R.id.btn_rate), view.findViewById(R.id.label_rate));
        });

        // 預設選中聊天室
        loadChildFragment(new ExpenseFragment());
        highlightSelected(view.findViewById(R.id.btn_chat), view.findViewById(R.id.label_chat));




        return view;
    }
    private void highlightSelected(View menuView, TextView labelToShow) {
        if (selectedMenu != null) {
            selectedMenu.setSelected(false);
        }
        menuView.setSelected(true);
        selectedMenu = menuView;

        // 隱藏所有文字
        for (TextView label : allLabels) {
            label.setVisibility(View.GONE);
        }
        // 顯示目前選中的
        labelToShow.setVisibility(View.VISIBLE);
    }
    private void loadChildFragment(Fragment fragment) {
        Log.d("AccountsFragment", "Loading fragment: " + fragment.getClass().getSimpleName());
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.childFragmentContainer, fragment)
                .commit();
    }

}