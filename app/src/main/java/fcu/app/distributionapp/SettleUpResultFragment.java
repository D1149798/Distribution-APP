package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fcu.app.distributionapp.model.GroupMember;
import fcu.app.distributionapp.model.Transaction;


public class SettleUpResultFragment extends Fragment {

    private static final String ARG_CURRENCY = "currency";
    private static final String ARG_TRANSACTIONS = "transactions";
    private static final String ARG_MEMBERS = "members";

    public SettleUpResultFragment() {
        // Required empty public constructor
    }

    public static SettleUpResultFragment newInstance(List<Transaction> transactions, List<GroupMember> members, String currency) {
        SettleUpResultFragment fragment = new SettleUpResultFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_TRANSACTIONS, new ArrayList<>(transactions));
//        args.putParcelableArrayList(ARG_MEMBERS, new ArrayList<>(members));
        args.putString(ARG_CURRENCY, currency);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settle_up_result, container, false);
    }
}