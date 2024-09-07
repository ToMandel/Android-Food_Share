package com.Tom.foodshare.Activity.Popup;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.Tom.foodshare.Activity.Login;
import com.Tom.foodshare.Class.FoodItem;
import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.Fragment.FoodStock;
import com.Tom.foodshare.Fragment.ShoppingList;
import com.Tom.foodshare.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditFoodList extends Activity {
    private int width;
    private int height;
    private FoodItem foodItem;
    private TextView food_nameTV, foodUnitTV;
    private NumberPicker numberPicker;
    private Button updateBtn;
    private User user;
    private FirebaseFirestore db;
    private String listType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        setContentView(R.layout.activity_edit_food_list);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        Intent intent = getIntent();
        this.user = (User) intent.getExtras().getSerializable(Login.LOGIN_USER_KEY);
        foodItem = (FoodItem) intent.getExtras().getSerializable("foodItem");
        listType = intent.getStringExtra("listType");

        numberPicker = findViewById(R.id.numberPicker);
        food_nameTV = findViewById(R.id.food_name);
        food_nameTV.setText(foodItem.getDescription());
        foodUnitTV = findViewById(R.id.unitPop);
        updateBtn = findViewById(R.id.update);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(500);
        numberPicker.setValue(foodItem.getAmount());
        foodUnitTV.setText(foodItem.getUnit());

        updateBtn.setOnClickListener(v -> {
            int newAmount = numberPicker.getValue();
            if (newAmount == 0) {
                deleteItem();
            } else {
                // Update the item with the new amount
                updateItem(newAmount);
            }
        });
        setupPopupWindow();
    }

    private void deleteItem() {
        String collection = listType.equals("FoodItems") ? "FoodItems" : "ShoppingList";
        db.collection(collection).document(foodItem.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listType.equals("FoodItems")) {
                        FoodStock.foodList.remove(foodItem);
                        FoodStock.mAdapter.notifyDataSetChanged();
                    } else if (listType.equals("ShoppingList")) {
                        ShoppingList.shoppingList.remove(foodItem);
                        ShoppingList.mAdapter.notifyDataSetChanged();
                    }
                    finish();
                })
                .addOnFailureListener(e -> finish());
    }

    private void updateItem(int newAmount) {
        foodItem.setAmount(newAmount);
        String documentId = foodItem.getId();
        String collection = listType.equals("FoodItems") ? "FoodItems" : "ShoppingList";
        if (documentId != null && !documentId.isEmpty()) {
            db.collection(collection).document(documentId).set(foodItem)
                    .addOnSuccessListener(aVoid -> {
                        // Update local list and adapter
                        if (listType.equals("FoodItems")) {
                            int pos = FoodStock.foodList.indexOf(foodItem);
                            if (pos >= 0) {
                                FoodStock.foodList.set(pos, foodItem);
                                FoodStock.mAdapter.notifyDataSetChanged();
                            }
                        } else if (listType.equals("ShoppingList")) {
                            int pos = ShoppingList.shoppingList.indexOf(foodItem);
                            if (pos >= 0) {
                                ShoppingList.shoppingList.set(pos, foodItem);
                                ShoppingList.mAdapter.notifyDataSetChanged();
                            }
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> finish());
        }
    }

    private void setupPopupWindow() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        getWindow().setLayout((int) (width * .6), (int) (height * .2));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }
}
