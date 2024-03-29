package com.example.loginregistration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loginregistration.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class MainActivity extends AppCompatActivity {

    String userId;
    DatabaseReference mDatabase;
    CircleImageView profile_picture;
    TextView profile_name;
    Button logout_btn;

    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseAuth auth;
    ShimmerLayout shimmerProfileImage, shimmerProfileName;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        handler = new Handler();

        profile_picture = findViewById(R.id.profile_picture);
        profile_name = findViewById(R.id.profile_name);
        logout_btn = findViewById(R.id.logout_btn);

        shimmerProfileImage = findViewById(R.id.shimmer_profile_image);
        shimmerProfileName = findViewById(R.id.shimmer_profile_name);

        shimmerProfileImage.startShimmerAnimation();
        shimmerProfileName.startShimmerAnimation();

        logout_btn.setOnClickListener(view -> {
            auth.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Bundle extra = getIntent().getExtras();

        if (extra != null) {
            userId = extra.getString("userId");
            assert userId != null;
            mDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User comeUser = dataSnapshot.getValue(User.class);
                    if (comeUser == null) {
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finish();
                    }
                    assert comeUser != null;
                    profile_name.setText(comeUser.getName());

                    // set image here
                    storageReference.child(comeUser.getImageUrl()).getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Picasso.get().load(uri).rotate(0).into(profile_picture);
                                handler.post(stopShimmerAnimation);
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    private Runnable stopShimmerAnimation = new Runnable() {
        @Override
        public void run() {
            shimmerProfileImage.stopShimmerAnimation();
            shimmerProfileName.stopShimmerAnimation();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(stopShimmerAnimation);
    }
}
