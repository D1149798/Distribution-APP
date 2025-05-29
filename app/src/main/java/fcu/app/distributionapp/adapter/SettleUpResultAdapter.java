package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.distributionapp.R;

public class SettleUpResultAdapter extends RecyclerView.Adapter<SettleUpResultAdapter.ResultViewHolder> {

    private final List<String> resultList;

    public SettleUpResultAdapter(List<String> resultList) {
        this.resultList = resultList;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settle_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.tvResult.setText(resultList.get(position));
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    // ✅ 這就是你缺少的內部 ViewHolder 類別
    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvResult;

        ResultViewHolder(View itemView) {
            super(itemView);
            tvResult = itemView.findViewById(R.id.tv_SettleItem); // item_settle_result.xml 中的 TextView ID
        }
    }
}
