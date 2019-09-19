package com.example.loginregistration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText email_edt_text, password_edt_text;
    TextView new_registration_text;
    Button login_btn;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Login...");

        email_edt_text = findViewById(R.id.email_edt_text);
        password_edt_text = findViewById(R.id.password_edt_text);
        new_registration_text = findViewById(R.id.new_registration_text);
        login_btn = findViewById(R.id.login_btn);

        new_registration_text.setOnClickListener(view -> startActivity(new Intent(Login.this, Register.class)));

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent comeUser = new Intent(Login.this, MainActivity.class);
            comeUser.putExtra("userId", auth.getCurrentUser().getUid());
            startActivity(comeUser);
            finish();
        }

        login_btn.setOnClickListener(view -> loginUser());

    }

    public void loginUser() {

        String email = email_edt_text.getText().toString().trim();
        final String password = password_edt_text.getText().toString();

        if (TextUtils.isEmpty(email)) {
            email_edt_text.setError("Enter Email Here");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            password_edt_text.setError("Enter Password");
            return;
        }

        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Intent comeUser = new Intent(Login.this, MainActivity.class);
                        comeUser.putExtra("userId", task.getResult().getUser().getUid());
                        startActivity(comeUser);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login Failed. Something wrong", Toast.LENGTH_LONG).show();
                    }
                });

    }
}
