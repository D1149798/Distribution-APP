package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddTransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTransactionFragment extends Fragment {


    public AddTransactionFragment() {
        // Required empty public constructor
    }

    public static AddTransactionFragment newInstance(String param1, String param2) {
        AddTransactionFragment fragment = new AddTransactionFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        Spinner groupSpinner = view.findViewById(R.id.groupSpinner);
        Spinner payerSpinner = view.findViewById(R.id.payerSpinner);
        Spinner categorySpinner = view.findViewById(R.id.categorySpinner);

        // 假資料：之後可改成從資料庫取得
        String[] groupList = {"旅遊小組", "家庭", "同事", "朋友"};
        String[] payerList = {"小明", "小華", "小美"};
        String[] categoryList = {"餐飲", "交通", "娛樂", "住宿", "雜支"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, groupList);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);

        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, payerList);
        payerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payerSpinner.setAdapter(payerAdapter);

        return view;
    }
}