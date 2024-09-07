package com.Tom.foodshare.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Tom.foodshare.Activity.Login;
import com.Tom.foodshare.Activity.Popup.AddFoodList;
import com.Tom.foodshare.Activity.Popup.EditFoodList;
import com.Tom.foodshare.Adapter.FoodListAdapter;
import com.Tom.foodshare.Class.FoodItem;
import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FoodStock extends Fragment {
    public static ArrayList<FoodItem> foodList;
    private RecyclerView mRecyclerView;
    public static FoodListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button addBtn;
    private FirebaseFirestore db;
    private User user;
    private Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_food_stock, container, false);

        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        if (getArguments() != null) {
            this.user = (User) getArguments().get(Login.LOGIN_USER_KEY);
        }

        addBtn = view.findViewById(R.id.add_food_item);

        foodList = new ArrayList<>();

        // Initialize the RecyclerView and Adapter
        mRecyclerView = view.findViewById(R.id.food_stock_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new FoodListAdapter(foodList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemActionListener(new FoodListAdapter.OnItemActionListener() {
            @Override
            public void onEditClick(int position) {
                FoodItem item = foodList.get(position);
                openEditDialog(item);
            }

            @Override
            public void onDeleteClick(int position) {
                FoodItem item = foodList.get(position);
                deleteItemFromFoodStock(item, position);
            }

            @Override
            public void onMoveClick(int position) {
                FoodItem item = foodList.get(position);
                moveItemToShoppingList(item, position);
            }
        });

        loadData();

        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddFoodList.class);
            bundle = new Bundle();
            bundle.putSerializable(Login.LOGIN_USER_KEY, user);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        foodList.clear();  // Clear the list to avoid duplicates

        db.collection("FoodItems")
                .whereEqualTo("user.email", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                FoodItem item = document.toObject(FoodItem.class);
                                if (!foodList.contains(item)) {
                                    foodList.add(item);
                                }
                            }
                            mAdapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
                        }
                    } else {
                        Log.w("TAG", "Failed to read value.", task.getException());
                        Toast.makeText(getContext(), "Failed to load food items.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openEditDialog(FoodItem item) {
        Intent intent = new Intent(getContext(), EditFoodList.class);
        intent.putExtra("foodItem", item);
        intent.putExtra(Login.LOGIN_USER_KEY, user);
        intent.putExtra("listType", "FoodItems");
        startActivity(intent);
    }

    // Method to delete an item from FoodStock
    private void deleteItemFromFoodStock(FoodItem item, int position) {
        String documentId = item.getId();  // Get the document ID

        if (documentId != null && !documentId.isEmpty()) {
            db.collection("FoodItems").document(documentId).delete()
                    .addOnSuccessListener(aVoid -> {
                        foodList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Item deleted successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete item.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Error: Item ID is null or empty.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to move an item from FoodStock to ShoppingList
    private void moveItemToShoppingList(FoodItem item, int position) {
        db.collection("ShoppingList").document(item.getId()).set(item)
                .addOnSuccessListener(aVoid -> {
                    // Once the item is successfully added to ShoppingList, delete it from FoodStock
                    db.collection("FoodItems").document(item.getId()).delete()
                            .addOnSuccessListener(aVoid2 -> {
                                foodList.remove(position);  // Remove the item from the local list
                                mAdapter.notifyItemRemoved(position);  // Notify the adapter about item removal
                                Toast.makeText(getContext(), "Item moved to Shopping List.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreError", "Failed to delete item from FoodStock after moving to ShoppingList", e);
                                Toast.makeText(getContext(), "Failed to delete item from FoodStock.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to add item to ShoppingList", e);
                    Toast.makeText(getContext(), "Failed to move item to Shopping List.", Toast.LENGTH_SHORT).show();
                });
    }

}
