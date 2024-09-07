package com.Tom.foodshare.Activity.Popup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.Tom.foodshare.Activity.Login;
import com.Tom.foodshare.Class.FoodItem;
import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.Fragment.FoodStock;
import com.Tom.foodshare.R;
import com.squareup.picasso.Picasso;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddFoodList extends Activity implements AdapterView.OnItemSelectedListener {
    private NumberPicker addNumberPicker;
    private Button addBtn;
    private Spinner categorySpinner, unitsSpinner;
    private String currentCategory, currentUnit;
    private ImageView foodImage;
    private Context context;
    private FirebaseFirestore db;
    public User user;
    private String itemUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        setContentView(R.layout.activity_add_food_list);

        Intent intent = getIntent();
        if(intent != null){
            this.user = (User) intent.getExtras().getSerializable(Login.LOGIN_USER_KEY);
        }

        context = this;
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        categorySpinner = findViewById(R.id.category_spinner);
        unitsSpinner = findViewById(R.id.units_spinner);
        addNumberPicker = findViewById(R.id.add_numberPicker);
        addBtn = findViewById(R.id.add_food_item);
        foodImage = findViewById(R.id.food_image_add);

        addNumberPicker.setMinValue(0);
        addNumberPicker.setMaxValue(1000);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> unitsAdapter = ArrayAdapter.createFromResource(this, R.array.units, android.R.layout.simple_spinner_item);
        unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitsSpinner.setAdapter(unitsAdapter);
        unitsSpinner.setOnItemSelectedListener(this);

        addBtn.setOnClickListener(v -> {
            String documentId = db.collection("FoodItems").document().getId();  // Generate a Firestore document ID
            FoodItem foodItem = new FoodItem(currentCategory, addNumberPicker.getValue(), currentUnit, itemUrl, user, documentId);

            db.collection("FoodItems")
                    .document(documentId)
                    .set(foodItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Item Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to add item. Please try again.", Toast.LENGTH_SHORT).show();
                    });

            if (FoodStock.mAdapter != null) {
                FoodStock.foodList.add(foodItem);
                FoodStock.mAdapter.notifyDataSetChanged();
            }
        });
        setupPopupWindow();
    }

    private void setupPopupWindow() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .6), (int) (height * .5));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.category_spinner) {
            currentCategory = parent.getItemAtPosition(position).toString();
            switch (currentCategory) {
                case "Cream Cheese":
                    itemUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTQsbCMD1kRgx-N_CXU4z7rSk_5jAY3d42OnQ&s";
                    break;
                case "Milk":
                    itemUrl = "https://as1.ftcdn.net/v2/jpg/01/06/68/88/1000_F_106688812_rVoRFXazgIMEUJdvffG9p0XvP8Lntf0a.jpg";
                    break;
                case "Sugar":
                    itemUrl = "https://images.theconversation.com/files/307440/original/file-20191217-58292-nlmvmh.jpg?ixlib=rb-1.1.0&q=45&auto=format&w=926&fit=clip";
                    break;
                case "Pasta":
                    itemUrl = "https://previews.123rf.com/images/pavlok/pavlok1804/pavlok180400192/100084317-stack-of-raw-spaghetti-isolated-on-white-background.jpg";
                    break;
            }
            Picasso.get().load(itemUrl).fit().centerCrop().into(foodImage);
        } else if (parentId == R.id.units_spinner) {
            currentUnit = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
