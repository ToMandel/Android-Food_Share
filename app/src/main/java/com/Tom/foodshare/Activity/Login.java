package com.Tom.foodshare.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Tom.foodshare.Class.FoodItem;
import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    private EditText emailET, passwordET, firstNameET, lastNameET;
    public static EditText ageET;
    private TextView firstNameTV, lastNameTV, ageTV, emailTV, createAccountTV;
    private Button loginBtn;
    private Context context;
    public static final String LOGIN_USER_KEY = "login_user_key";

    private User user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isCreatingAccount = false;

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Toast.makeText(context, "You have successfully logged in.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, ManageFood.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(LOGIN_USER_KEY, user);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        context = this;

        // Initialize UI components
        initializeUI();

        createAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRegistrationUI();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = loginBtn.getText().toString();
                if (buttonText.equals("Login")) {
                    handleLogin();
                } else if (buttonText.equals("Sign Up")) {
                    handleRegistration();
                }
            }
        });
    }

    private void initializeUI() {
        emailTV = findViewById(R.id.emailTV);
        emailET = findViewById(R.id.email);
        passwordET = findViewById(R.id.password);
        firstNameET = findViewById(R.id.firstName);
        lastNameET = findViewById(R.id.lastName);
        ageET = findViewById(R.id.birthDayET);
        firstNameTV = findViewById(R.id.firstNameTV);
        lastNameTV = findViewById(R.id.lastNameTV);
        ageTV = findViewById(R.id.birthDayTV);
        createAccountTV = findViewById(R.id.creatAccountTV);
        loginBtn = findViewById(R.id.login);

        // Hide registration fields by default
        firstNameET.setVisibility(View.GONE);
        firstNameTV.setVisibility(View.GONE);
        lastNameET.setVisibility(View.GONE);
        lastNameTV.setVisibility(View.GONE);
        ageET.setVisibility(View.GONE);
        ageTV.setVisibility(View.GONE);
    }

    private void toggleRegistrationUI() {
        if (firstNameET.getVisibility() == View.GONE) {
            // Show registration fields
            firstNameET.setVisibility(View.VISIBLE);
            firstNameTV.setVisibility(View.VISIBLE);
            lastNameET.setVisibility(View.VISIBLE);
            lastNameTV.setVisibility(View.VISIBLE);
            ageET.setVisibility(View.VISIBLE);
            ageTV.setVisibility(View.VISIBLE);
            createAccountTV.setText("Back");
            loginBtn.setText("Sign Up");
        } else {
            // Hide registration fields
            firstNameET.setVisibility(View.GONE);
            firstNameTV.setVisibility(View.GONE);
            lastNameET.setVisibility(View.GONE);
            lastNameTV.setVisibility(View.GONE);
            ageET.setVisibility(View.GONE);
            ageTV.setVisibility(View.GONE);
            loginBtn.setText("Login");
            createAccountTV.setText("Don't have an account yet? Click here!");
        }
    }

    private void handleLogin() {
        if (loginUiCheck()) {
            Toast.makeText(context, "The email or password is incorrect", Toast.LENGTH_LONG).show();
        } else {
            // Authenticate with Firebase
            mAuth.signInWithEmailAndPassword(emailET.getText().toString().trim(), passwordET.getText().toString().trim())
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Fetch user details from Firestore
                                    db.collection("users").document(firebaseUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                                        user = documentSnapshot.toObject(User.class);
                                        updateUI(firebaseUser);
                                    }).addOnFailureListener(e -> {
                                        Log.w("Login", "Failed to fetch user details.", e);
                                        Toast.makeText(context, "Failed to load user details.", Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    Log.w("Login", "FirebaseUser is null after login.");
                                    Toast.makeText(context, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.w("Login", "signInWithEmail:failure", task.getException());
                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void handleRegistration() {
        if (signUiCheck()) {
            Toast.makeText(context, "Please fill out all required fields.", Toast.LENGTH_LONG).show();
        } else {
            prepareUserData();
            if (!isCreatingAccount) {
                isCreatingAccount = true;
                mAuth.createUserWithEmailAndPassword(user.getEmail(), passwordET.getText().toString().trim())
                        .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                isCreatingAccount = false;
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        // Save user details to Firestore
                                        db.collection("users").document(firebaseUser.getUid())
                                                .set(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, "User registered successfully.", Toast.LENGTH_SHORT).show();
                                                    addDefaultItemsToDB(user);
                                                    updateUI(firebaseUser);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("Login", "Failed to save user details.", e);
                                                    Toast.makeText(context, "Registration successful, but failed to save user details.", Toast.LENGTH_SHORT).show();
                                                    updateUI(firebaseUser);
                                                });
                                    }
                                } else {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(context, "This email is already registered. Please log in or use a different email.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(context, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
            }
        }
    }

    public boolean loginUiCheck() {
        boolean hasErrors = false;
        if (emailET.getText().toString().trim().isEmpty()) {
            emailET.setError("Please enter your email.");
            hasErrors = true;
        }
        if (passwordET.getText().toString().trim().isEmpty()) {
            passwordET.setError("Please enter your password.");
            hasErrors = true;
        }
        return hasErrors;
    }
    public boolean signUiCheck() {
        boolean hasErrors = false;
        if (emailET.getText().toString().trim().isEmpty()) {
            emailET.setError("Please enter your email.");
            hasErrors = true;
        }
        if (firstNameET.getText().toString().trim().isEmpty()) {
            firstNameET.setError("Please enter your first name.");
            hasErrors = true;
        }
        if (lastNameET.getText().toString().trim().isEmpty()) {
            lastNameET.setError("Please enter your last name.");
            hasErrors = true;
        }
        if (ageET.getText().toString().trim().isEmpty()) {
            ageET.setError("Please enter your age.");
            hasErrors = true;
        }
        if (passwordET.getText().toString().trim().isEmpty()) {
            passwordET.setError("Please enter your password.");
            hasErrors = true;
        }
        return hasErrors;
    }
    private void prepareUserData() {
        user = new User();
        user.setEmail(emailET.getText().toString().trim());
        user.setAge(ageET.getText().toString().trim());
        user.setFirstName(firstNameET.getText().toString().trim());
        user.setLastName(lastNameET.getText().toString().trim());
    }
    private void addDefaultItemsToDB(User user) {
        ArrayList<FoodItem> defaultItems = new ArrayList<>();
            defaultItems.add(new FoodItem("Butter", 200, "Gram", "https://dairyfarmersofcanada.ca/sites/default/files/product_butter_thumb.jpg", user, null));
            defaultItems.add(new FoodItem("Flour", 2, "Kilo", "https://www.apk-inform.com/uploads/Redakciya/2019/%D0%98%D1%8E%D0%BD%D1%8C/%D0%BC%D1%83%D0%BA%D0%B0.jpg", user, null));
            defaultItems.add(new FoodItem("Eggs", 12, "Units", "https://chriskresser.com/wp-content/uploads/iStock-172696992.jpg", user, null));
        for (FoodItem item : defaultItems) {
            String documentId = db.collection("ShoppingList").document().getId();
            item.setId(documentId);
            db.collection("ShoppingList").document(documentId).set(item)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Failed to add default item", e);
                        Toast.makeText(context, "Failed to add default items.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    }