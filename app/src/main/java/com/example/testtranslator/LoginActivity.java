package com.example.testtranslator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testtranslator.MainActivity;
import com.example.testtranslator.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    EditText editname,editpass;
    TextView tvSignUp;


    Button btnLogin;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editname = findViewById(R.id.editTextEmail);
        editpass = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.cirLoginButton);
        Intent intent = new Intent(this, MainActivity.class);
        mAuth= FirebaseAuth.getInstance();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SignIn();
            }
        });
        tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });
    }
    public void SignIn()
    {
        String email = String.valueOf(editname.getText());
        String password = String.valueOf(editpass.getText());
        if(email.isEmpty())
        {
            editname.setError("Not Empty Email!");
            editname.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            editname.setError("Invalid Email!");
            editname.requestFocus();
            return;
        }
        if(password.isEmpty())
        {
            editpass.setError("Not Empty Email!");
            editpass.requestFocus();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        } else
                        {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Confirm")
                                    .setMessage("Sign Failed!")
                                    .setPositiveButton("YES",null).setNegativeButton("NO",null).show();
                        }
                    }
                });
        editname.setText("");
        editpass.setText("");
    }
}
