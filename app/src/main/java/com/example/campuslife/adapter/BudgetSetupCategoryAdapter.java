package com.example.campuslife.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.preparation.UpsertBudgetCategoryRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class BudgetSetupCategoryAdapter extends RecyclerView.Adapter<BudgetSetupCategoryAdapter.ViewHolder> {

    private final List<UpsertBudgetCategoryRequest> items;
    private final Runnable onDataChanged;

    public BudgetSetupCategoryAdapter(List<UpsertBudgetCategoryRequest> items, Runnable onDataChanged) {
        this.items = items;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_input_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UpsertBudgetCategoryRequest item = items.get(holder.getAdapterPosition());

        // Remove old watchers to avoid recursive loops during binding
        if (holder.nameWatcher != null) {
            holder.etCategoryName.removeTextChangedListener(holder.nameWatcher);
        }
        if (holder.allocWatcher != null) {
            holder.etCategoryAllocated.removeTextChangedListener(holder.allocWatcher);
        }

        holder.etCategoryName.setText(item.name != null ? item.name : "");
        holder.etCategoryAllocated.setText(item.allocatedAmount != null ? item.allocatedAmount : "");

        holder.nameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    items.get(pos).name = s.toString();
                    onDataChanged.run();
                }
            }
        };
        holder.etCategoryName.addTextChangedListener(holder.nameWatcher);

        holder.allocWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    items.get(pos).allocatedAmount = s.toString();
                    onDataChanged.run();
                }
            }
        };
        holder.etCategoryAllocated.addTextChangedListener(holder.allocWatcher);

        holder.btnRemoveCategory.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                items.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, items.size());
                onDataChanged.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText etCategoryName, etCategoryAllocated;
        ImageButton btnRemoveCategory;
        TextWatcher nameWatcher, allocWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            etCategoryName = itemView.findViewById(R.id.etCategoryName);
            etCategoryAllocated = itemView.findViewById(R.id.etCategoryAllocated);
            btnRemoveCategory = itemView.findViewById(R.id.btnRemoveCategory);
        }
    }
}
