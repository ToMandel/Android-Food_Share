package com.Tom.foodshare.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Tom.foodshare.Class.FoodItem;
import com.Tom.foodshare.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder> {
    private ArrayList<FoodItem> foodList;
    private OnItemClickListener mlistener;
    private OnItemActionListener actionListener;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public interface OnItemActionListener {
        void onEditClick(int position);  // Edit action
        void onDeleteClick(int position);  // Delete action
        void onMoveClick(int position);  // Move action between lists
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mlistener = listener;
    }
    public void setOnItemActionListener(OnItemActionListener listener) {actionListener = listener;
    }
    public static class FoodListViewHolder extends RecyclerView.ViewHolder {
        public ImageView foodImage;
        public TextView foodDescription;
        public TextView amount;
        public TextView unit;
        public ImageView editIcon;
        public ImageView deleteIcon;
        public ImageView moveIcon;



        public FoodListViewHolder(@NonNull View itemView, final OnItemClickListener listener, final OnItemActionListener actionListener) {
            super(itemView);
            foodDescription = itemView.findViewById(R.id.foodDescriptionTV);
            amount = itemView.findViewById(R.id.amountTV);
            unit = itemView.findViewById(R.id.unitTV);
            foodImage = itemView.findViewById(R.id.food_image);
            editIcon = itemView.findViewById(R.id.edit_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
            moveIcon = itemView.findViewById(R.id.move_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            actionListener.onEditClick(position);
                        }
                    }
                }
            });

            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            actionListener.onDeleteClick(position);
                        }
                    }
                }
            });

            moveIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            actionListener.onMoveClick(position);
                        }
                    }
                }
            });
        }
    }

    public FoodListAdapter(ArrayList<FoodItem> foodList) {
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);
        FoodListViewHolder foodListViewHolder = new FoodListViewHolder(v, mlistener, actionListener);

        return foodListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FoodListViewHolder holder, int position) {
        FoodItem currentFoodItem = foodList.get(position);

        holder.foodDescription.setText(currentFoodItem.getDescription());
        holder.amount.setText(String.valueOf(currentFoodItem.getAmount()));
        holder.unit.setText(currentFoodItem.getUnit());

        String imageUrl = currentFoodItem.getUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(holder.foodImage);
        } else {
            holder.foodImage.setImageResource(R.drawable.grocery);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }
}
