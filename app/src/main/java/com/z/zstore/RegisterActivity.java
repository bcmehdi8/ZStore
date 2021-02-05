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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button register_btn;
    SignInButton gl_btn;
    private EditText Name_input, Pass_input, Email_input;
    private ProgressDialog loadingBar;
    private String parentDbName = "Users";
    int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    public static boolean disableCloseBtn = false;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";
    private ImageButton closeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register_btn = (Button) findViewById(R.id.Register_btn);
        Name_input = (EditText) findViewById(R.id.Name_input);
        Pass_input = (EditText) findViewById(R.id.Pass_input);
        Email_input = (EditText) findViewById(R.id.Email_input);
        closeBtn = (ImageButton) findViewById(R.id.sign_up_close_btn);
        gl_btn = findViewById(R.id.googlex);
        loadingBar = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               checkEmailPass();
            }
        });
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

        //Google Sign in Method
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
        Name_input.addTextChangedListener(new TextWatcher() {
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
    }

    private void checkEmailPass() {
        if(Email_input.getText().toString().matches(emailPattern)){
            loadingBar.setTitle("Register Account");
            loadingBar.setMessage("Please Wait, While we are checking your information");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            firebaseAuth.createUserWithEmailAndPassword(Email_input.getText().toString(),Pass_input.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final Map<Object,String> userdata = new HashMap<>();
                            userdata.put("fullname",Name_input.getText().toString());
                            firebaseFirestore.collection("USERS").document(firebaseAuth.getUid())
                                    .set(userdata)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                CollectionReference userDataReference = firebaseFirestore.collection("USERS").document(firebaseAuth.getUid()).collection("USER_DATA");

                                                //MAPS
                                                Map<Object,Long> wishlistMap = new HashMap<>();
                                                wishlistMap.put("list_size",(long) 0);

                                                Map<Object,Long> ratingsMap = new HashMap<>();
                                                ratingsMap.put("list_size",(long) 0);

                                                Map<Object,Long> cartMap = new HashMap<>();
                                                cartMap.put("list_size",(long) 0);

                                                Map<Object,Long> addressesMap = new HashMap<>();
                                                addressesMap.put("list_size",(long) 0);

                                                //MAPS

                                                final List<String> documentNames = new ArrayList<>();
                                                documentNames.add("MY_WISHLIST");
                                                documentNames.add("MY_RATINGS");
                                                documentNames.add("MY_CART");
                                                documentNames.add("MY_ADDRESSES");

                                                List<Map<Object,Long>> documentFields = new ArrayList<>();
                                                documentFields.add(wishlistMap);
                                                documentFields.add(ratingsMap);
                                                documentFields.add(cartMap);
                                                documentFields.add(addressesMap);

                                                for(int x=0;x<documentNames.size();x++){
                                                    final int finalX = x;
                                                    userDataReference.document(documentNames.get(x))
                                                            .set(documentFields.get(x)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                if(finalX == documentNames.size() -1){
                                                                    mainIntent();
                                                                }
                                                            }else{
                                                                loadingBar.dismiss();
                                                                register_btn.setEnabled(true);
                                                                String error = task.getException().getMessage();
                                                                Toast.makeText(RegisterActivity.this,error,Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }else{
                                                String error = task.getException().getMessage();
                                                Toast.makeText(RegisterActivity.this,error,Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }else{
                            String error = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this,error,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
        }else{
            Email_input.setError("Invalid Email");
        }
    }

    private void checkInput() {
        if(!TextUtils.isEmpty(Email_input.getText())){
            if(!TextUtils.isEmpty(Name_input.getText())){
                if(!TextUtils.isEmpty(Pass_input.getText()) && Pass_input.length() >=8 ){
                    register_btn.setEnabled(true);
                }else{
                    register_btn.setEnabled(false);
                }
            }else{
                register_btn.setEnabled(false);
            }
        }else{
            register_btn.setEnabled(false);
        }
    }



    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

        Toast.makeText(RegisterActivity.this, "Logged in Successfully", Toast.LENGTH_LONG).show();
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
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);
           // this.finish();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
        }
    }
    private void mainIntent(){
        Intent mainIntent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(mainIntent);
        disableCloseBtn = false;
        finish();
    }
}
