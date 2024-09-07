package com.Tom.foodshare.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.Tom.foodshare.Class.Picture;
import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Camera extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    public boolean isPicExists = false;
    private Bitmap photo;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private DatabaseReference dbPicture;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    public User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            this.user = (User) intent.getExtras().getSerializable(Login.LOGIN_USER_KEY);
        }

        if (this.user == null) {
            Toast.makeText(this, "User data is missing. Please try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        this.dbPicture = FirebaseDatabase.getInstance().getReference("Picture");
        this.storageReference = FirebaseStorage.getInstance().getReference();

        Button savePic = findViewById(R.id.button_save);
        savePic.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (isPicExists) {
                    uploadImageToFirebase(photo);
                } else {
                    Toast.makeText(getBaseContext(), "Please take a picture", Toast.LENGTH_LONG).show();
                }
            }
        });

        this.imageView = findViewById(R.id.imageView1);
        Button photoButton = findViewById(R.id.button_take_picture);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Camera.this, ManageFood.class);
                intent.putExtra(Login.LOGIN_USER_KEY, user); // Pass user data back if needed
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
    }
    private void uploadImageToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        if (user != null) {
            String path = "images/" + user.getEmail() + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageReference.child(path);

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveImageMetadataToDatabase(uri.toString());
                    Toast.makeText(Camera.this, "Image saved successfully", Toast.LENGTH_LONG).show();
                    finish();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(Camera.this, "Error uploading image", Toast.LENGTH_LONG).show();
            });
        } else {
            Toast.makeText(this, "Failed to upload image: User data is missing.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveImageMetadataToDatabase(String imageUrl) {
        Date dt = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = df.format(dt);

        int picHashCode = this.hashCode();
        Picture picture = new Picture(imageUrl, formattedDate, user, picHashCode);

        dbPicture.push().setValue(picture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            this.photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            this.isPicExists = true;
        }
    }
}
