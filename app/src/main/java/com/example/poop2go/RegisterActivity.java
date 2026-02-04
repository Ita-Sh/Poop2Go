package com.example.poop2go;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etDob;
    private Button btnRegister;
    private CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Initialize views
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etDob = findViewById(R.id.et_dob);
        btnRegister = findViewById(R.id.btn_register);
        cbRememberMe = findViewById(R.id.cb_remember_me);

        // Show clickable date picker dialog when DOB field is clicked
        etDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Set listener
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    // Show clickable date picker dialog
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDob.setText(date);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String dob = etDob.getText().toString().trim();

        // Check if all fields are filled
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase authentication
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Connecting");
            pd.setMessage("Creating User...");
            pd.show();
            FBRef.refAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pd.dismiss();
                            if (task.isSuccessful()) {
                                Log.i("Firebase", "createUserWithEmail:success");
                                FirebaseUser user = FBRef.refAuth.getCurrentUser();
                                Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
                                startActivity(intent);
                            } else {
                                Exception exp = task.getException();
                                if (exp instanceof FirebaseAuthInvalidUserException) {
                                    Toast.makeText(RegisterActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                                } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                    Toast.makeText(RegisterActivity.this, "Password too weak", Toast.LENGTH_SHORT).show();
                                } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                                } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(RegisterActivity.this, "General authentication failure", Toast.LENGTH_SHORT).show();
                                } else if (exp instanceof FirebaseNetworkException) {
                                    Toast.makeText(RegisterActivity.this, "Network error. Please check your network", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        // Add to RT database
    }
}