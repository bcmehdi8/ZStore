package com.z.zstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private Button login_btn;
    SignInButton gl_btn;
    private EditText Pass_input, Email_input;
    private CheckBox checkBoxRemember;
    private ProgressDialog loadingBar;
    private String parentDbName = "Users";
    int RC_SIGN_IN = 0;
    private FirebaseAuth firebaseAuth;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";
    public static boolean disableCloseBtn = false;
    private ImageButton closeBtn;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_btn = (Button) findViewById(R.id.lgn_btn);
        gl_btn = findViewById(R.id.googlex);
        Pass_input = (EditText) findViewById(R.id.Pass_input);
        Email_input = (EditText) findViewById(R.id.Email_input);
        checkBoxRemember = (CheckBox) findViewById(R.id.checkBox);
        closeBtn = (ImageButton) findViewById(R.id.sign_in_close_btn);
        loadingBar = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        Paper.init(this);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainIntent();
            }
        });

        if(disableCloseBtn){
            closeBtn.setVisibility(View.GONE);
        }else{
            closeBtn.setVisibility(View.VISIBLE);
        }

        //Google Signin Method
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        gl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.googlex:
                        signIn();
                        break;

                }
            }
        });
    Email_input.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkInput();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });
    Pass_input.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkInput();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });
        login_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                checkEmailAndPassword();
            }
        });

    }

    private void checkEmailAndPassword() {
        if(Email_input.getText().toString().matches(emailPattern)){
            if(Pass_input.length()>=0){
                loadingBar.setTitle("Login Account");
                loadingBar.setMessage("Please Wait, While we are checking your information");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                firebaseAuth.signInWithEmailAndPassword(Email_input.getText().toString(),Pass_input.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    loadingBar.dismiss();
                                    mainIntent();
                                }else{
                                    loadingBar.dismiss();
                                    String error = task.getException().getMessage();
                                    Toast.makeText(LoginActivity.this,error,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }else{
                Toast.makeText(LoginActivity.this,"Incorrect email or password",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(LoginActivity.this,"Incorrect email or password",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkInput() {
        if(!TextUtils.isEmpty(Email_input.getText())){
            if(!TextUtils.isEmpty(Pass_input.getText())){
                login_btn.setEnabled(true);
            }else{
                login_btn.setEnabled(false);
            }
        }else{
            login_btn.setEnabled(false);
        }
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
        }
    }
    private void mainIntent(){
        if(disableCloseBtn){
            disableCloseBtn = false;
        }else {
            Intent mainIntent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(mainIntent);
        }
        finish();
    }
}
