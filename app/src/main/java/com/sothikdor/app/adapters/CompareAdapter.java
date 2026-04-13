package com.sothikdor.app.adapters;

import com.sothikdor.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sothikdor.app.models.Price;

import java.util.List;

public class CompareAdapter extends RecyclerView.Adapter<CompareAdapter.ViewHolder> {

    private final Context context;
    private List<Price> priceList;
    private String productName;
    private double lowestPrice = Double.MAX_VALUE;

    public CompareAdapter(Context context, List<Price> priceList) {
        this.context = context;
        this.priceList = priceList;
    }

    public void updateData(List<Price> newPrices, String productName) {
        this.priceList = newPrices;
        this.productName = productName;
        lowestPrice = Double.MAX_VALUE;
        for (Price p : newPrices) {
            if (p.getMinPrice() < lowestPrice) lowestPrice = p.getMinPrice();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_compare_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Price price = priceList.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvMarketName.setText(price.getMarketName());
        holder.tvArea.setText(price.getMarketId() != null ? "ঢাকা" : "");
        holder.tvMinPrice.setText("৳" + (int) price.getMinPrice());
        holder.tvMaxPrice.setText("৳" + (int) price.getMaxPrice() + " পর্যন্ত");

        // সবচেয়ে সস্তা বাজার হাইলাইট করা
        boolean isCheapest = price.getMinPrice() == lowestPrice;
        if (isCheapest) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.green_pale));
            holder.tvMinPrice.setTextColor(ContextCompat.getColor(context, R.color.green_primary));
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText("✓ সেরা দাম");
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.tvMinPrice.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.tvBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return priceList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvMarketName, tvArea, tvMinPrice, tvMaxPrice, tvBadge;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvMarketName = itemView.findViewById(R.id.tvMarketName);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvMinPrice = itemView.findViewById(R.id.tvMinPrice);
            tvMaxPrice = itemView.findViewById(R.id.tvMaxPrice);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
