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
import com.Tom.foodshare.Activity.Popup.EditFoodList;
import com.Tom.foodshare.Adapter.FoodListAdapter;
import com.Tom.foodshare.Class.FoodItem;
import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShoppingList extends Fragment {
    private RecyclerView mRecyclerView;
    public static FoodListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button addBtn;
    public static ArrayList<FoodItem> shoppingList;
    private FirebaseFirestore db;
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        addBtn = view.findViewById(R.id.add_food_item);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Retrieve the User object from arguments
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Login.LOGIN_USER_KEY);
        }

        shoppingList = new ArrayList<>();

        loadData();

        mRecyclerView = view.findViewById(R.id.food_stock_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new FoodListAdapter(shoppingList); // Use the separate shoppingList here

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemActionListener(new FoodListAdapter.OnItemActionListener() {
            @Override
            public void onEditClick(int position) {
                FoodItem item = shoppingList.get(position);
                openEditDialog(item);
            }

            @Override
            public void onDeleteClick(int position) {
                FoodItem item = shoppingList.get(position);
                deleteItemFromShoppingList(item, position);
            }

            @Override
            public void onMoveClick(int position) {
                FoodItem item = shoppingList.get(position);
                moveItemToFoodStock(item, position);
            }
        });

        return view;
    }

    private void loadData() {
        shoppingList.clear();
        db.collection("ShoppingList")
                .whereEqualTo("user.email", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                FoodItem item = document.toObject(FoodItem.class);
                                shoppingList.add(item); // Add each item to the shoppingList
                            }
                            mAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load shopping list.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openEditDialog(FoodItem item) {
        Intent intent = new Intent(getContext(), EditFoodList.class);
        intent.putExtra("foodItem", item);
        intent.putExtra(Login.LOGIN_USER_KEY, user);
        intent.putExtra("listType", "ShoppingList");
        startActivity(intent);
    }

    private void deleteItemFromShoppingList(FoodItem item, int position) {
        String documentId = item.getId();

        if (documentId != null && !documentId.isEmpty()) {
            db.collection("FoodItems").document(documentId).delete()
                    .addOnSuccessListener(aVoid -> {
                        shoppingList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Item deleted successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete item.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Error: Item ID is null or empty.", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveItemToFoodStock(FoodItem item, int position) {
        db.collection("FoodItems").document(item.getId()).set(item)
                .addOnSuccessListener(aVoid -> {
                    // delete it from ShoppingList
                    db.collection("ShoppingList").document(item.getId()).delete()
                            .addOnSuccessListener(aVoid2 -> {
                                shoppingList.remove(position);  // local list
                                mAdapter.notifyItemRemoved(position);  // Notify the adapter about item removal
                                Toast.makeText(getContext(), "Item moved to Food Stock.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreError", "Failed to delete item from ShoppingList after moving to FoodStock", e);
                                Toast.makeText(getContext(), "Failed to delete item from ShoppingList.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to add item to FoodStock", e);
                    Toast.makeText(getContext(), "Failed to move item to Food Stock.", Toast.LENGTH_SHORT).show();
                });
    }
}
