
package com.example.mychat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * @exclude
 * @hide
 */
import com.example.mychat.R;
import com.example.mychat.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextInputLayout textInputEmail, textInputName, textInputPassword, textInputConfirmPassword;
    private Button loginButton;
    private TextView toggleTextView;
    private boolean loginMode;
    private FirebaseDatabase database;
    private DatabaseReference usersDatabaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ImageView logoImage = findViewById(R.id.logoImageView);
        logoImage.setImageResource(R.drawable.logo);
        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");

        textInputEmail = findViewById(R.id.textInputEmail);
        textInputName = findViewById(R.id.textInputName);
        textInputPassword = findViewById(R.id.textInputPassword);
        textInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);
        loginButton = findViewById(R.id.loginSignInButton);
        toggleTextView = findViewById(R.id.toggleLoginTextView);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSignUpUser(textInputEmail.getEditText().getText().toString().trim(), textInputPassword.getEditText().getText().toString().trim());
            }
        });

        if(auth.getCurrentUser() != null){
            startActivity(new Intent(SignInActivity.this, DrawerActivity.class));
        }

    }
    private boolean validateEmail(){
        String emailInput = textInputEmail.getEditText().getText().toString().trim();

        if(emailInput.isEmpty()){
            textInputEmail.setError("Please input your email");
            return false;
        }
        else {
            textInputEmail.setError("");
            return true;
        }
    }
    private boolean validateName(){
        String nameInput = textInputName.getEditText().getText().toString().trim();
        if(nameInput.isEmpty()){
            if(loginMode)
                return true;
            else {
                textInputName.setError("Please input your login");
                return false;
            }
        }
        else if (nameInput.length() > 15){
            textInputName.setError("Login length have to be less than 15");
            return false;
        }
        else {
            textInputName.setError("");
            return true;
        }
    }
    private boolean validatePassword(){
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if(passwordInput.isEmpty()){
            textInputPassword.setError("Please input your password");
            return false;
        }
        else if (passwordInput.length() < 7){
            textInputPassword.setError("Password length have to be more than 6");
            return false;
        }
        else {
            textInputPassword.setError("");
            return true;
        }
    }
    private boolean validateConfirmPassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        String confirmPasswordInput = textInputConfirmPassword.getEditText().getText().toString().trim();
        if (!passwordInput.equals(confirmPasswordInput)){
            textInputConfirmPassword.setError("Passwords don't have to match");
            return false;
        }
        else {
            textInputConfirmPassword.setError("");
            return true;
        }

    }
    private void loginSignUpUser(String email, String password) {
        if(loginMode){
            if(!validateEmail() | !validatePassword())
               return;
            else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("auth", "signInWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    Intent intent = new Intent(SignInActivity.this, DrawerActivity.class);
                                    intent.putExtra("userName", textInputName.getEditText().getText().toString().trim());
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("auth", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }else {
            if(!validateEmail() | !validateName() | !validatePassword() | !validateConfirmPassword())
                return;
            else {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("auth", "createUserWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    createUser(user);
                                    Intent intent = new Intent(SignInActivity.this, DrawerActivity.class);
                                    intent.putExtra("userName", textInputName.getEditText().getText().toString().trim());
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("auth", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }
    private void createUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(textInputName.getEditText().getText().toString().trim());
        user.setOnlineStatus("online");
        user.setAvatarMockUpResource(" ");
        user.setLogin(" ");
        user.setAccountStatus("ok");
        usersDatabaseReference.push().setValue(user);
    }
    public void toggleLoginMode(View view) {
        if(loginMode){
            loginMode = false;
            loginButton.setText("Sign Up");
            toggleTextView.setText("Or, Log In");
            textInputConfirmPassword.setVisibility(View.VISIBLE);
            textInputName.setVisibility(View.VISIBLE);
        } else {
            loginMode = true;
            loginButton.setText("Log In");
            toggleTextView.setText("Or, Sign Up");
            textInputConfirmPassword.setVisibility(View.GONE);
            textInputName.setVisibility(View.GONE);

        }

    }
}