package com.z.zstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.z.zstore.HomeActivity.showCart;

public class ProductDetailsActivity extends AppCompatActivity {

    public static boolean running_wishlist_query = false;
    public static boolean running_rating_query = false;
    public static boolean running_cart_query = false;

    private ViewPager productImagesViewPager;
    private TextView productTitle;
    private TextView avergeRatingMiniView;
    private TextView totalRatingMiniView;
    private TextView productPrice;
    private TextView cuttedPrice;
    private ImageView codIndicator;
    private TextView tvcodIndicator;
    private TabLayout viewpagerIndicator;

    //////////// Rating Layout
        public static int initialRating;
        public static LinearLayout rateNowContainer;
        private TextView totalRatings;
        private LinearLayout ratingsNoContainer;
        private LinearLayout ratingsProgressBarContainer;
        private TextView avergeRating;
        private TextView totalRatingsFigure;
    //////////// Rating Layout


    //Product Description
    private ConstraintLayout productDetailsTabsContainer;
    private ViewPager productDetailsViewpager;
    private TabLayout productDetailsTabLayout;
    private String productDescription;
    private String productOtherDetails;
    //public static int tabPosition = -1;
    private List<ProductSpecificationModel> productSpecificationModelList = new ArrayList<>();
    //Product Description

    public static String ProductID;

    private Button buyNowBtn;
    private LinearLayout addToCartBtn;

    public static boolean ALREADY_ADDED_TO_WISHLIST = false;
    public static boolean ALREADY_ADDED_TO_CART = false;
    public static FloatingActionButton addToWishListBtn;

    private FirebaseFirestore firebaseFirestore;
    private Dialog signInDialog;
    private Dialog loadingDialog;
    private FirebaseUser currentUserr;
    private DocumentSnapshot documentSnapshot;
    public static MenuItem cartItem;
    private TextView badgeCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productImagesViewPager = findViewById(R.id.product_images_viewpager);
        viewpagerIndicator = findViewById(R.id.viewpager_indicator);
        addToWishListBtn = findViewById(R.id.add_to_wishlist_btn);
        productDetailsViewpager = findViewById(R.id.product_details_viewpager);
        productDetailsTabLayout = findViewById(R.id.product_details_tabLayout);
        buyNowBtn = findViewById(R.id.buy_now_btn);
        productTitle = findViewById(R.id.product_title);
        avergeRatingMiniView = findViewById(R.id.tv_product_rating_miniview);
        totalRatingMiniView = findViewById(R.id.total_ratings_miniview);
        totalRatingsFigure = findViewById(R.id.total_ratings_figure);
        productPrice = findViewById(R.id.product_price);
        cuttedPrice = findViewById(R.id.cutted_price);
        codIndicator = findViewById(R.id.cod_indicator);
        tvcodIndicator = findViewById(R.id.tv_cod_indicator);
        productDetailsTabsContainer = findViewById(R.id.product_details_tabs_container);
        totalRatings = findViewById(R.id.total_ratings);
        ratingsNoContainer = findViewById(R.id.ratings_numbers_container);
        ratingsProgressBarContainer = findViewById(R.id.ratings_progressbar_container);
        avergeRating = findViewById(R.id.average_rating);
        addToCartBtn = findViewById(R.id.add_to_cart_btn);
        ProductID = getIntent().getStringExtra("PRODUCT_ID");

        initialRating = -1;

        //Loading Dialog
        loadingDialog = new Dialog(ProductDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_proccess_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        //Loading Dialog

        firebaseFirestore = FirebaseFirestore.getInstance();

        final List<String> productImages = new ArrayList<>();

        Toast.makeText(ProductDetailsActivity.this,ProductID,Toast.LENGTH_SHORT).show();
        firebaseFirestore.collection("CATEGORIES")
                .document("HOME")
                .collection("PRD").document(ProductID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                     documentSnapshot = task.getResult(); //{

                        for (long x = 1; x < (long) documentSnapshot.get("no_of_product_images") + 1; x++) {
                            productImages.add(documentSnapshot.get("product_image_" + x).toString());
                        }
                        ProductImagesAdapter productImagesAdapter = new ProductImagesAdapter(productImages);
                        productImagesViewPager.setAdapter(productImagesAdapter);

                        productTitle.setText(documentSnapshot.get("product_title").toString());
                        avergeRatingMiniView.setText(documentSnapshot.get("averge_rating").toString());
                        totalRatingMiniView.setText("(" + (long) documentSnapshot.get("total_rating") + ") Notes");
                        productPrice.setText("$" + documentSnapshot.get("product_price").toString());
                        cuttedPrice.setText("$" + documentSnapshot.get("cutted_price").toString());
                        if ((boolean) documentSnapshot.get("COD")) {
                            codIndicator.setVisibility(View.VISIBLE);
                            tvcodIndicator.setVisibility(View.VISIBLE);
                        } else {
                            codIndicator.setVisibility(View.INVISIBLE);
                            tvcodIndicator.setVisibility(View.INVISIBLE);
                        }
                        if ((boolean) documentSnapshot.get("use_tab_layout")) {
                            //productDetailsTabsContainer.setVisibility(View.VISIBLE);
                            productDescription = documentSnapshot.get("product_description").toString();
                            productOtherDetails = documentSnapshot.get("product_other_details").toString();

                            for (long x = 1; x < (long) documentSnapshot.get("total_spec_titles") + 1; x++) {
                                    productSpecificationModelList.add(new ProductSpecificationModel(0, documentSnapshot.get("spec_title_" + x).toString()));
                                for (long y = 1; y < (long) documentSnapshot.get("spec_title_" + x + "_total_fields") + 1; y++) {
                                    productSpecificationModelList.add(new ProductSpecificationModel(1, documentSnapshot.get("spec_title_" + x + "_field_" + y + "_name").toString(), documentSnapshot.get("spec_title_" + x + "_field_" + y + "_value").toString()));
                                }
                            }
                        } else {
                            productDetailsTabsContainer.setVisibility(View.GONE);
                        }



                        totalRatings.setText((long) documentSnapshot.get("total_rating") + " Ratings");
                        for (int x = 0; x < 5; x++) {
                            TextView rating = (TextView) ratingsNoContainer.getChildAt(x);
                            rating.setText(String.valueOf((long) documentSnapshot.get((5 - x) + "_star")));

                            ProgressBar progressBar = (ProgressBar) ratingsProgressBarContainer.getChildAt(x);
                            int maxProgress = Integer.parseInt(String.valueOf((long) documentSnapshot.get("total_rating")));
                            progressBar.setMax(maxProgress);
                            progressBar.setProgress(Integer.parseInt(String.valueOf((long) documentSnapshot.get((5 - x) + "_star"))));
                        }
                        totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_rating")));
                        avergeRating.setText(documentSnapshot.get("averge_rating").toString());
                        productDetailsViewpager.setAdapter(new ProductDetailsAdapter(getSupportFragmentManager(), productDetailsTabLayout.getTabCount(), productDescription, productOtherDetails, productSpecificationModelList));

                        if(currentUserr != null){
                            if(DBqueries.myRating.size() == 0){
                                DBqueries.loadRatingList(ProductDetailsActivity.this);
                            }
                            if(DBqueries.cartList.size() == 0){
                                DBqueries.loadCartList(ProductDetailsActivity.this,loadingDialog,false,badgeCount);
                            }else{
                                loadingDialog.dismiss();
                            }
                            if(DBqueries.wishList.size() == 0){
                                DBqueries.loadWishList(ProductDetailsActivity.this,loadingDialog,false);
                            }else{
                                loadingDialog.dismiss();
                            }
                        }else{
                            loadingDialog.dismiss();
                        }

                        if(DBqueries.myRatedIds.contains(ProductID)){
                            int index = DBqueries.myRatedIds.indexOf(ProductID);
                            initialRating = Integer.parseInt(String.valueOf(DBqueries.myRating.get(index)))-1;
                            setRating(initialRating);
                        }
                       if(DBqueries.cartList.contains(ProductID)){
                            ALREADY_ADDED_TO_CART = true;
                       }else{
                           ALREADY_ADDED_TO_CART = false;
                       }
                        if(DBqueries.wishList.contains(ProductID)){
                            ALREADY_ADDED_TO_WISHLIST = true;
                            addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.WishColor));
                        }else{
                            addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#A5A4A4")));
                            ALREADY_ADDED_TO_WISHLIST = false;
                        }
                //    }
                }else{
                    loadingDialog.dismiss();
                    String error = task.getException().getMessage();
                    Toast.makeText(ProductDetailsActivity.this,error,Toast.LENGTH_SHORT).show();
                }
            }
        });



        viewpagerIndicator.setupWithViewPager(productImagesViewPager,true);

        addToWishListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUserr == null){
                    signInDialog.show();
                }else{
                    if(!running_wishlist_query){
                        running_wishlist_query = true;
                if(ALREADY_ADDED_TO_WISHLIST){
                    int index = DBqueries.wishList.indexOf(ProductID);
                    DBqueries.removeFromWishList(index,ProductDetailsActivity.this);
                    addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#A5A4A4")));
                }else{
                    addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.WishColor));
                    Map<String,Object> addProduct = new HashMap<>();

                                Map<String,Object> updateListSize = new HashMap<>();
                                updateListSize.put("product_ID_"+String.valueOf(DBqueries.wishList.size()),ProductID);
                                updateListSize.put("list_size", (long) (DBqueries.wishList.size()+1));

                                firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_WISHLIST")
                                        .update(updateListSize).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            if(DBqueries.wishlistModelList.size() !=0){
                                                DBqueries.wishlistModelList.add(new WishlistModel(ProductID,documentSnapshot.get("product_image_1").toString(),
                                                        documentSnapshot.get("product_title").toString(),
                                                        documentSnapshot.get("averge_rating").toString(),
                                                        (long) documentSnapshot.get("total_rating"),
                                                        documentSnapshot.get("product_price").toString(),
                                                        documentSnapshot.get("cutted_price").toString(),
                                                        (boolean) documentSnapshot.get("COD")));
                                            }
                                            ALREADY_ADDED_TO_WISHLIST = true;
                                            addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.WishColor));
                                            DBqueries.wishList.add(ProductID);
                                            Toast.makeText(ProductDetailsActivity.this,"ADDED TO WISHLIST!",Toast.LENGTH_SHORT).show();


                            }else{
                               addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#A5A4A4")));
                               String error = task.getException().getMessage();
                               Toast.makeText(ProductDetailsActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                              running_wishlist_query = false;
                        }
                    });
                }}
            }
            }
        });



        //////////// Rating Layout
        rateNowContainer = findViewById(R.id.rate_now_container);
        for(int x=0;x<rateNowContainer.getChildCount();x++){
            final int starPosition = x;
            rateNowContainer.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentUserr == null){
                        signInDialog.show();
                    }else {
                         if(starPosition != initialRating){
                        if (!running_rating_query) {
                            running_rating_query = true;
                            setRating(starPosition);
                            Map<String, Object> updateRating = new HashMap<>();
                            if (DBqueries.myRatedIds.contains(ProductID)) {

                                TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                updateRating.put(initialRating + 1 + "_star", Long.parseLong(oldRating.getText().toString()) - 1);
                                updateRating.put(starPosition + 1 + "_star", Long.parseLong(finalRating.getText().toString()) + 1);

                                updateRating.put("averge_rating", calculateAvergeRating((long) starPosition - initialRating, true));

                            } else {
                                updateRating.put(starPosition + 1 + "_star", (long) documentSnapshot.get(starPosition + 1 + "_star") + 1);
                                updateRating.put("averge_rating", calculateAvergeRating(starPosition + 1, false));
                                updateRating.put("total_rating", (long) documentSnapshot.get("total_rating") + 1);
                            }

                            firebaseFirestore.collection("CATEGORIES")
                                    .document("HOME").collection("PRD").document(ProductID)
                                    .update(updateRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Map<String, Object> myRating = new HashMap<>();
                                        if (DBqueries.myRatedIds.contains(ProductID)) {
                                            myRating.put("rating_" + DBqueries.myRatedIds.indexOf(ProductID), (long) starPosition + 1);
                                        } else {
                                            myRating.put("list_size", (long) DBqueries.myRatedIds.size() + 1);
                                            myRating.put("product_ID_" + DBqueries.myRatedIds.size(), ProductID);
                                            myRating.put("rating_" + DBqueries.myRatedIds.size(), (long) starPosition + 1);
                                        }

                                        firebaseFirestore.collection("USERS").document(currentUserr.getUid()).collection("USER_DATA").document("MY_RATINGS")
                                                .update(myRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    if (DBqueries.myRatedIds.contains(ProductID)) {

                                                        DBqueries.myRating.set(DBqueries.myRatedIds.indexOf(ProductID), (long) starPosition + 1);

                                                        TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                                        TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                                        oldRating.setText(String.valueOf(Integer.parseInt(oldRating.getText().toString()) - 1));
                                                        finalRating.setText(String.valueOf(Integer.parseInt(finalRating.getText().toString()) + 1));
                                                    } else {
                                                        DBqueries.myRatedIds.add(ProductID);
                                                        DBqueries.myRating.add((long) starPosition + 1);

                                                        TextView rating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);
                                                        rating.setText(String.valueOf(Integer.parseInt(rating.getText().toString()) + 1));

                                                        totalRatingMiniView.setText("(" + ((long) documentSnapshot.get("total_rating") + 1) + ") Ratings");
                                                        totalRatings.setText((long) documentSnapshot.get("total_rating") + 1 + " Ratings");
                                                        totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_rating") + 1));

                                                        Toast.makeText(ProductDetailsActivity.this, "Thank you! for rating", Toast.LENGTH_SHORT).show();
                                                    }

                                                    for (int x = 0; x < 5; x++) {
                                                        TextView ratingfigures = (TextView) ratingsNoContainer.getChildAt(x);

                                                        ProgressBar progressBar = (ProgressBar) ratingsProgressBarContainer.getChildAt(x);
                                                        int maxProgress = Integer.parseInt(totalRatingsFigure.getText().toString());
                                                        progressBar.setMax(maxProgress);
                                                        progressBar.setProgress(Integer.parseInt(ratingfigures.getText().toString()));
                                                    }
                                                    initialRating = starPosition;
                                                    avergeRating.setText(calculateAvergeRating(0, true));
                                                    avergeRatingMiniView.setText(calculateAvergeRating(0, true));

                                                    if (DBqueries.wishList.contains(ProductID) && DBqueries.wishlistModelList.size() != 0) {
                                                        int index = DBqueries.wishList.indexOf(ProductID);
                                                        DBqueries.wishlistModelList.get(index).setRating(avergeRating.getText().toString());
                                                        DBqueries.wishlistModelList.get(index).setTotalRatings(Long.parseLong(totalRatingsFigure.getText().toString()));
                                                    }
                                                } else {
                                                    setRating(initialRating);
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                                }
                                                running_rating_query = false;
                                            }
                                        });

                                    } else {
                                        running_rating_query = false;
                                        setRating(initialRating);
                                        String error = task.getException().getMessage();
                                        Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                    }
                    }
                }
            });
        }
        //////////// Rating Layout




        buyNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUserr == null){
                    signInDialog.show();
                }else{
                    Intent deliveryIntent = new Intent(ProductDetailsActivity.this,DeliveryActivity.class);
                    startActivity(deliveryIntent);
                }
            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUserr == null){
                    signInDialog.show();
                }else{
                    if(!running_cart_query){
                        running_cart_query = true;
                        if(ALREADY_ADDED_TO_CART){
                           // running_cart_query = false;
                            Toast.makeText(ProductDetailsActivity.this,"Already add to cart!",Toast.LENGTH_SHORT).show();
                        }else{
                            Map<String,Object> addProduct = new HashMap<>();
                            addProduct.put("product_ID_"+String.valueOf(DBqueries.cartList.size()),ProductID);
                            addProduct.put("list_size", (long) (DBqueries.cartList.size()+1));

                            firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                                    .update(addProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                                    if(DBqueries.cartItemModelList.size() !=0){
                                                        DBqueries.cartItemModelList.add(new CartItemModel(CartItemModel.CART_ITEM,ProductID,documentSnapshot.get("product_image_1").toString(),
                                                                documentSnapshot.get("product_title").toString(),
                                                                documentSnapshot.get("product_price").toString(),
                                                                documentSnapshot.get("cutted_price").toString(),
                                                                (long) 1,
                                                                (long) 1));
                                                    }
                                                    ALREADY_ADDED_TO_CART = true;
                                                    DBqueries.cartList.add(ProductID);
                                                    Toast.makeText(ProductDetailsActivity.this,"Added to Cart Successfully!",Toast.LENGTH_SHORT).show();
                                                    invalidateOptionsMenu();
                                                    running_cart_query = false;
                                    }else{
                                        running_cart_query = false;
                                        String error = task.getException().getMessage();
                                        Toast.makeText(ProductDetailsActivity.this,error,Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }}
                }
            }
        });
        productDetailsViewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(productDetailsTabLayout));
        productDetailsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                productDetailsViewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //sign dialog
        signInDialog = new Dialog(ProductDetailsActivity.this);
        signInDialog.setContentView(R.layout.sign_in_dialog);
        signInDialog.setCancelable(true);
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        Button dialogSignInBtn = signInDialog.findViewById(R.id.signin_btn);
        Button dialogSignUpBtn = signInDialog.findViewById(R.id.signup_btn);
        final Intent registerIntent = new Intent(ProductDetailsActivity.this,RegisterActivity.class);
        final Intent LoginIntent = new Intent(ProductDetailsActivity.this,LoginActivity.class);

        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.disableCloseBtn = true;
                RegisterActivity.disableCloseBtn = true;
                signInDialog.dismiss();
                startActivity(LoginIntent);

            }
        });

        dialogSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.disableCloseBtn = true;
                RegisterActivity.disableCloseBtn = true;
                signInDialog.dismiss();
                startActivity(registerIntent);
            }
        });

        //sign dialog

    }
    @Override
    protected void onStart() {
        super.onStart();
        currentUserr = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUserr!=null){
            if(DBqueries.myRating.size() == 0){
                DBqueries.loadRatingList(ProductDetailsActivity.this);
            }

            if(DBqueries.wishList.size() == 0){
                DBqueries.loadWishList(ProductDetailsActivity.this,loadingDialog,false);
            }else{
                loadingDialog.dismiss();
            }
        }else{
            loadingDialog.dismiss();
        }

        if(DBqueries.myRatedIds.contains(ProductID)){
            int index = DBqueries.myRatedIds.indexOf(ProductID);
            initialRating = Integer.parseInt(String.valueOf(DBqueries.myRating.get(index))) -1;
            setRating(initialRating);
        }
        if(DBqueries.cartList.contains(ProductID)){
            ALREADY_ADDED_TO_CART = true;
        }else{
            ALREADY_ADDED_TO_CART = false;
        }
        if(DBqueries.wishList.contains(ProductID)){
            ALREADY_ADDED_TO_WISHLIST = true;
            addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.WishColor));
        }else{
            addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#A5A4A4")));
            ALREADY_ADDED_TO_WISHLIST = false;
        }
        invalidateOptionsMenu();
    }

    public static void setRating(int starPosition){
            for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
                ImageView starBtn = (ImageView) rateNowContainer.getChildAt(x);
                starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#D3D3D3")));
                if (x <= starPosition) {
                    starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FDBC1A")));
                }
            }
    }

    private String calculateAvergeRating(long currentUserRating, boolean update){
        Double totalStars = Double.valueOf(0);
        for(int x=1;x<6;x++){
           TextView ratingNo = (TextView) ratingsNoContainer.getChildAt(5 - x);
           totalStars = totalStars + (Long.parseLong(ratingNo.getText().toString())*x);
        }
        totalStars = totalStars + currentUserRating;
        if(update){
            return String.valueOf(totalStars / Long.parseLong(totalRatingsFigure.getText().toString())).substring(0,3);

        }else {
            return String.valueOf(totalStars / (Long.parseLong(totalRatingsFigure.getText().toString()) +1)).substring(0,3);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_and_cart_icon, menu);

        cartItem = menu.findItem(R.id.cart);

            cartItem.setActionView(R.layout.badge_layout);
            ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
            badgeIcon.setImageResource(R.drawable.ic_shopping_cart_white);
             badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);
        if(currentUserr != null){
            if(DBqueries.cartList.size() == 0){
                DBqueries.loadCartList(ProductDetailsActivity.this,loadingDialog,false, badgeCount);
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
                        Intent cartIntent = new Intent(ProductDetailsActivity.this,HomeActivity.class);
                        showCart = true;
                        startActivity(cartIntent);
                    }
                }
            });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }else if(id == R.id.search){
            return true;
        }else if(id == R.id.cart){
            if(currentUserr == null) {
                signInDialog.show();
            }else{
                Intent cartIntent = new Intent(ProductDetailsActivity.this,HomeActivity.class);
                showCart = true;
                startActivity(cartIntent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
