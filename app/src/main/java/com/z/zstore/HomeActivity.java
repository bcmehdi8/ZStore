package com.z.zstore;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.z.zstore.ProductDetailsActivity.cartItem;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private long backPressedTime;
    private Toast backToast;
    GoogleSignInClient mGoogleSignInClient;
    private AppBarConfiguration mAppBarConfiguration;
    private FrameLayout frameLayout;
    private ImageView noInternetConnection;

    private static final int HOME_FRAGMEMT = 0;
    private static final int CART_FRAGMENT = 1;
    private static final int ORDERS_FRAGMENT = 2;
    private static final int WISHLIST_FRAGMENT = 3;
    private static final int ACCOUNT_FRAGMENT = 4;

    public static Boolean showCart = false;

    private static int currentFragment = -1;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Dialog signInDialog;
    public static DrawerLayout drawer;
    private FirebaseUser currentUserr;
    private TextView badgeCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Paper.init(this);
        //Google Signin Method
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);



        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);


       frameLayout = findViewById(R.id.main_frameLayout);

        if (showCart) {
            drawer.setDrawerLockMode(1);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            goToFragment("Cart", new MyCartFragment(), -2);
        } else {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            setFragment(new HomeFragment(), HOME_FRAGMEMT);
        }




        View headerView = navigationView.getHeaderView(0);
        ImageView profileImage = headerView.findViewById(R.id.ProfilePicture);
        TextView UserFullName = headerView.findViewById(R.id.ProfileName);
        TextView UserFullEmail = headerView.findViewById(R.id.ProfileEmail);


        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();

            UserFullName.setText(personName);
            UserFullEmail.setText(personEmail);
            Glide.with(this).load(String.valueOf(personPhoto)).into(profileImage);
        }


        signInDialog = new Dialog(HomeActivity.this);
        signInDialog.setContentView(R.layout.sign_in_dialog);
        signInDialog.setCancelable(true);
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        Button dialogSignInBtn = signInDialog.findViewById(R.id.signin_btn);
        Button dialogSignUpBtn = signInDialog.findViewById(R.id.signup_btn);
        final Intent registerIntent = new Intent(HomeActivity.this,RegisterActivity.class);
        final Intent LoginIntent = new Intent(HomeActivity.this,LoginActivity.class);

        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.disableCloseBtn = true;
                RegisterActivity.disableCloseBtn = true;
                signInDialog.dismiss();
                startActivity(registerIntent);

            }
        });

        dialogSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.disableCloseBtn = true;
                RegisterActivity.disableCloseBtn = true;
                signInDialog.dismiss();
                startActivity(LoginIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
         currentUserr = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUserr == null){
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setEnabled(false);
        }else{
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setEnabled(true);
        }
        //invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.cart_icon) {
            if(currentUserr == null){
                signInDialog.show();
            }else{
            goToFragment("Cart", new MyCartFragment(), CART_FRAGMENT);
            }
            return true;
        }else if(id==android.R.id.home){
                if(showCart){
                    showCart =false;
                    finish();
                    return true;
                }
            }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(currentFragment == HOME_FRAGMEMT){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getMenuInflater().inflate(R.menu.home, menu);


            cartItem = menu.findItem(R.id.cart_icon);

                cartItem.setActionView(R.layout.badge_layout);
                ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
                badgeIcon.setImageResource(R.drawable.ic_shopping_cart_white);
                badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);
                if(currentUserr != null){
                    if(DBqueries.cartList.size() == 0){
                        DBqueries.loadCartList(HomeActivity.this,new Dialog(HomeActivity.this),false, badgeCount);
                    }else{
                            badgeCount.setVisibility(View.VISIBLE);
                        }
                        if(DBqueries.cartList.size() < 99) {
                            badgeCount.setText(String.valueOf(DBqueries.cartList.size()));
                        }else{
                            badgeCount.setText("99+");
                        }

                }
                cartItem.getActionView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(currentUserr == null){
                            signInDialog.show();
                        }else{
                            goToFragment("Cart", new MyCartFragment(), CART_FRAGMENT);
                        }
                    }
                });

        }
        return true;
    }


    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HomeActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            if(currentFragment == HOME_FRAGMEMT){
                currentFragment = -1;
                super.onBackPressed();
            }else {
                if (showCart) {
                    showCart = false;
                    finish();
                } else {
                    invalidateOptionsMenu();
                    setFragment(new HomeFragment(), HOME_FRAGMEMT);
                    navigationView.getMenu().getItem(0).setChecked(true);
                }
            }
        }

    /*    if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();

     */
    }

    private void goToFragment(String title, Fragment fragment, int fragmentNo) {
        invalidateOptionsMenu();
        setFragment(fragment,fragmentNo);
        toolbar.setTitle(title);
        if(fragmentNo == CART_FRAGMENT){
            navigationView.getMenu().getItem(1).setChecked(true);
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(currentUserr !=null) {
            int id = menuItem.getItemId();
             if (id == R.id.nav_home) {
                invalidateOptionsMenu();
                toolbar.setTitle("Accueil");
                setFragment(new HomeFragment(), HOME_FRAGMEMT);
            }if (id == R.id.nav_cart) {
                goToFragment("Mon Panier", new MyCartFragment(), CART_FRAGMENT);
            } else if (id == R.id.nav_orders) {
                goToFragment("Mes commandes", new MyOrdersFragment(), ORDERS_FRAGMENT);
            } else if (id == R.id.nav_wishlist) {
                goToFragment("Mes Favoris", new MyWishlistFragment(), WISHLIST_FRAGMENT);
            }else if (id == R.id.nav_Account) {
                goToFragment("Mon Compte", new MyAccountFragment(), ACCOUNT_FRAGMENT);
            } else if (id == R.id.nav_logout) {
                signOut();
                FirebaseAuth.getInstance().signOut();
                DBqueries.clearData();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }else{

            drawer.closeDrawer(GravityCompat.START);
            signInDialog.show();
            return false;
        }
    }

    private void setFragment(Fragment fragment, int fragmentNo){
            currentFragment = fragmentNo;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            fragmentTransaction.replace(frameLayout.getId(), fragment);
            fragmentTransaction.commit();

    }


}
