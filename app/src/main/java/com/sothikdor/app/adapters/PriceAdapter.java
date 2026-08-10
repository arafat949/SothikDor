package com.sothikdor.app.adapters;

import com.sothikdor.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sothikdor.app.models.Price;
import com.sothikdor.app.utils.PriceFormatter;

import java.util.List;

public class PriceAdapter extends RecyclerView.Adapter<PriceAdapter.ViewHolder> {

    private final Context context;
    private final List<Price> priceList;
    private final OnPriceClickListener listener;
    private int lastAnimatedPosition = -1;

    public interface OnPriceClickListener {
        void onPriceClick(Price price);
    }

    public PriceAdapter(Context context, List<Price> priceList, OnPriceClickListener listener) {
        this.context = context;
        this.priceList = priceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_price_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Price price = priceList.get(position);

        holder.tvEmoji.setText(price.getProductEmoji() != null ? price.getProductEmoji() : "🛒");
        holder.tvProductName.setText(price.getProductName());
        holder.tvCategory.setText(price.getCategory() + " • প্রতি " + price.getUnit());
        if (holder.tvMarketName != null && price.getMarketName() != null) {
            holder.tvMarketName.setText("📍 " + price.getMarketName());
        }
        holder.tvMinPrice.setText(PriceFormatter.taka(price.getMinPrice()));
        holder.tvPriceRange.setText(PriceFormatter.takaRange(price.getMinPrice(), price.getMaxPrice()));

        // দামের পরিবর্তন দেখানো
        PriceFormatter.applyTrend(holder.tvTrend, price.getPriceTrend(),
                PriceFormatter.STABLE_STEADY);

        // Chart দেখার hint
        holder.tvChartHint.setText("📊 গ্রাফ দেখুন");

        // Card click → Chart Activity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPriceClick(price);
        });

        // Scroll Animation - নিচ থেকে উপরে আসবে
        if (position > lastAnimatedPosition) {
            holder.itemView.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.item_slide_up));
            lastAnimatedPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return priceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvProductName, tvCategory, tvMarketName, tvMinPrice, tvPriceRange, tvTrend, tvChartHint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvMarketName = itemView.findViewById(R.id.tvMarketName);
            tvMinPrice = itemView.findViewById(R.id.tvMinPrice);
            tvPriceRange = itemView.findViewById(R.id.tvPriceRange);
            tvTrend = itemView.findViewById(R.id.tvTrend);
            tvChartHint = itemView.findViewById(R.id.tvChartHint);
        }
    }
}
