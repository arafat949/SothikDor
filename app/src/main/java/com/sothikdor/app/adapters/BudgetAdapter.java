package com.sothikdor.app.adapters;

import com.sothikdor.R;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sothikdor.app.models.BudgetItem;
import com.sothikdor.app.utils.PriceFormatter;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private final Context context;
    private final List<BudgetItem> items;
    private final OnQuantityChangedListener listener;

    public interface OnQuantityChangedListener {
        void onQuantityChanged();
    }

    public BudgetAdapter(Context context, List<BudgetItem> items, OnQuantityChangedListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_budget_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BudgetItem item = items.get(position);

        holder.tvEmoji.setText(item.getEmoji());
        holder.tvName.setText(item.getProductName());
        holder.tvUnitPrice.setText(PriceFormatter.takaPerUnit(item.getPricePerUnit(), item.getUnit()));

        // পরিমাণ দেখানো
        holder.etQuantity.setText(item.getQuantity() == 0 ? "" : String.valueOf((int) item.getQuantity()));

        // মোট খরচ দেখানো
        showSubtotal(holder, item);

        // + বাটন
        holder.btnIncrease.setOnClickListener(v -> {
            double newQty = item.getQuantity() + 1;
            item.setQuantity(newQty);
            holder.etQuantity.setText(String.valueOf((int) newQty));
            showSubtotal(holder, item);
            if (listener != null) listener.onQuantityChanged();
        });

        // - বাটন
        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 0) {
                double newQty = item.getQuantity() - 1;
                item.setQuantity(newQty);
                if (newQty == 0) {
                    holder.etQuantity.setText("");
                } else {
                    holder.etQuantity.setText(String.valueOf((int) newQty));
                }
                showSubtotal(holder, item);
                if (listener != null) listener.onQuantityChanged();
            }
        });

        // Manual input
        holder.etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = holder.etQuantity.getText().toString().trim();
                if (text.isEmpty()) {
                    item.setQuantity(0);
                } else {
                    try {
                        item.setQuantity(Double.parseDouble(text));
                    } catch (NumberFormatException e) {
                        item.setQuantity(0);
                    }
                }
                showSubtotal(holder, item);
                if (listener != null) listener.onQuantityChanged();
            }
        });
    }

    private void showSubtotal(ViewHolder holder, BudgetItem item) {
        if (item.getQuantity() > 0) {
            holder.tvSubtotal.setText("= " + PriceFormatter.takaRounded(item.getTotalCost()));
            holder.tvSubtotal.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubtotal.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvUnitPrice, tvSubtotal;
        EditText etQuantity;
        ImageButton btnIncrease, btnDecrease;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}
