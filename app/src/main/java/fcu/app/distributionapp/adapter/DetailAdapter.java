package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import fcu.app.distributionapp.R;

/*
public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder>{
    private List<DetailGroup> detailList;

    public DetailAdapter(List<DetailGroup> detailList) {
        this.detailList = detailList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPayer, tvReceiver, tvNote, tvAmount, tvDate;

        public ViewHolder(View view) {
            super(view);
            tvPayer = view.findViewById(R.id.text_payer);
            tvReceiver = view.findViewById(R.id.text_receiver);
            tvNote = view.findViewById(R.id.text_note);
            tvAmount = view.findViewById(R.id.text_amount);
            tvDate = view.findViewById(R.id.text_date);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DetailGroup detail = detailList.get(position);
        holder.tvPayer.setText(detail.payer);
        holder.tvReceiver.setText(detail.receiver);
        holder.tvNote.setText(detail.note);
        holder.tvAmount.setText(String.valueOf(detail.amount));
        holder.tvDate.setText(detail.date);
    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }
}
*/