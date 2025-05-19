package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.distributionapp.R;
import fcu.app.distributionapp.model.Transaction;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private final List<Transaction> transactions;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, OnItemClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPayer, tvAmount, tvNote, tvDate;

        public ViewHolder(View view) {
            super(view);
            tvPayer = view.findViewById(R.id.tv_payer);
            tvAmount = view.findViewById(R.id.tv_amount);
            tvNote = view.findViewById(R.id.tv_note);
            tvDate = view.findViewById(R.id.tv_date);
        }

        public void bind(Transaction transaction, OnItemClickListener listener) {
            tvPayer.setText("付款者：" + transaction.getPayer());
            tvAmount.setText(transaction.getAmount() + " " + transaction.getCurrency());
            tvNote.setText("備註：" + transaction.getNote());
            tvDate.setText("日期：" + transaction.getDate());

            itemView.setOnClickListener(v -> listener.onItemClick(transaction));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(transactions.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
