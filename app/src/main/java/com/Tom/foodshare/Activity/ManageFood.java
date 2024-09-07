package com.Tom.foodshare.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.Fragment.FoodStock;
import com.Tom.foodshare.Fragment.Scan;
import com.Tom.foodshare.Fragment.ShoppingList;
import com.Tom.foodshare.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ManageFood extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private User user;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        setContentView(R.layout.activity_manage_food);

        // Get user data from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Login.LOGIN_USER_KEY)) {
            this.user = (User) intent.getExtras().getSerializable(Login.LOGIN_USER_KEY);
            if (this.user == null) {
                Toast.makeText(this, "User data is missing. Please log in again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "User data is missing. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_Food_stock);

        // Initialize the first fragment
        Fragment newFragment = new FoodStock();
        bundle = new Bundle();
        bundle.putSerializable(Login.LOGIN_USER_KEY, user);
        newFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_bottom_container, newFragment).commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        View headerView = navigationView.getHeaderView(0);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_Food_stock);
        }

        TextView firstNameTextView = headerView.findViewById(R.id.nav_header_first_name);
        TextView lastNameTextView = headerView.findViewById(R.id.nav_header_last_name);

        if (user != null) {
            firstNameTextView.setText(user.getFirstName());
            lastNameTextView.setText(user.getLastName());
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    TextView toolbar_text = findViewById(R.id.toolbar_text);

                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.nav_Shopping_List) {
                        selectedFragment = new ShoppingList();
                        toolbar_text.setText("Shopping List");
                    } else if (itemId == R.id.nav_Scan) {
                        selectedFragment = new Scan();
                        toolbar_text.setText("Scanning Receipts");
                    } else if (itemId == R.id.nav_Food_stock) {
                        selectedFragment = new FoodStock();
                        toolbar_text.setText("Food Stock");
                    }

                    if (selectedFragment != null) {
                        bundle = new Bundle();
                        bundle.putSerializable(Login.LOGIN_USER_KEY, user);
                        selectedFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_bottom_container, selectedFragment).commit();
                        return true;
                    }
                    return false;
                }
            };


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        Fragment selectedFragment = null;
        TextView toolbar_text = findViewById(R.id.toolbar_text);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (itemId == R.id.nav_manage_food_stock) {
            selectedFragment = new FoodStock();
            toolbar_text.setText("Food Stock");
            bottomNav.setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_manage_shopping_list) {
            selectedFragment = new ShoppingList();
            toolbar_text.setText("Shopping List");
            bottomNav.setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_log_out) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
            return true;
        }

        if (selectedFragment != null) {
            bundle = new Bundle();
            bundle.putSerializable(Login.LOGIN_USER_KEY, user);
            selectedFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_bottom_container, selectedFragment).commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
