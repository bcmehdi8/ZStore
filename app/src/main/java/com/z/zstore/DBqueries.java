package com.z.zstore;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.z.zstore.HomeFragment.swipeRefreshLayout;


public class DBqueries {

    public static boolean addressesSelected = false;

    public static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public static List<CategoryModel> categoryModelList = new ArrayList<>();
    public static List<List<HomePageModel>> lists = new ArrayList<>();
    public static List<String> loadedCategoriesNames = new ArrayList<>();

    public static List<String> wishList = new ArrayList<>();
    public static List<WishlistModel> wishlistModelList = new ArrayList<>();

    public static List<String> myRatedIds = new ArrayList<>();
    public static List<Long> myRating = new ArrayList<>();

    public static List<String> cartList = new ArrayList<>();
    public static List<CartItemModel> cartItemModelList  = new ArrayList<>();

    public static List<HomePageModel> homePageModelList = new ArrayList<>();
    public static List<SliderModel> sliderModelList = new ArrayList<>();
    public static List<HorizontalProductScrollModel> horizontalProductScrollModelList= new ArrayList<>();
    public static List<HorizontalProductScrollModel> GridLayoutModelList = new ArrayList<>();
    public static List<WishlistModel> ViewAllProductList = new ArrayList<>();


    public static void loadCategory(final RecyclerView categoryRecyclerView, final Context context){
        firebaseFirestore.collection("CATEGORIES").orderBy("index").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                categoryModelList.add(new CategoryModel(documentSnapshot.get("icon").toString(), documentSnapshot.get("categoryName").toString()));
                            }
                            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryModelList);
                            categoryRecyclerView.setAdapter(categoryAdapter);
                            categoryAdapter.notifyDataSetChanged();
                        }else{
                            String error = task.getException().getMessage();
                            Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void loadFragmentData(final RecyclerView homePageRecyclerView, final Context context, final int index, String categoryName){
        categoryModelList.clear();
        firebaseFirestore.collection("CATEGORIES")
                .document(categoryName.toUpperCase())
                .collection("PRD").orderBy("view_type").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    List<SliderModel> sliderModelList = new ArrayList<>();
                    List<HorizontalProductScrollModel> horizontalProductScrollModelList= new ArrayList<>();
                    List<HorizontalProductScrollModel> GridLayoutModelList = new ArrayList<>();
                    List<WishlistModel> ViewAllProductList = new ArrayList<>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot d : task.getResult()) {
                                if ((long) d.get("view_type") == 1) {

                                    String Img = d.getData().get("banner_image").toString();
                                    sliderModelList.add(new SliderModel(Img));


                                }else if((long)d.get("view_type") == 2){
                                    String Img = d.getData().get("product_image_1").toString();
                                    String title = d.getData().get("product_title").toString();
                                    String subtitle = d.getData().get("product_subtitle").toString();
                                    String Price = d.getData().get("product_price").toString();
                                    String Id = d.getData().get("product_ID").toString();

                                    //   String Full_title = d.getData().get("product_full_title").toString();
                                    String Avg_rating = d.getData().get("averge_rating").toString();
                                    long total_rating = (long)d.getData().get("total_rating");
                                    String cutted_price = d.getData().get("cutted_price").toString();
                                    boolean cod = (boolean)d.getData().get("COD");


                                    horizontalProductScrollModelList.add(new HorizontalProductScrollModel(Id, Img,title, subtitle, Price));
                                    ViewAllProductList.add(new WishlistModel(Id,Img,title,Avg_rating,total_rating,Price,cutted_price,cod));
                                }
                                else if((long)d.get("view_type") == 3){
                                    String Img = d.getData().get("product_image_1").toString();
                                    String title = d.getData().get("product_title").toString();
                                    String subtitle = d.getData().get("product_subtitle").toString();
                                    String Price = d.getData().get("product_price").toString();
                                    String Id = d.getData().get("product_ID").toString();

                                    //     String Full_title = d.getData().get("product_full_title").toString();
                                    String Avg_rating = d.getData().get("averge_rating").toString();
                                    long total_rating = (long)d.getData().get("total_rating");
                                    String cutted_price =  "44"; //d.getData().get("cutted_price").toString();
                                    boolean cod = (boolean)d.getData().get("COD");


                                    GridLayoutModelList.add(new HorizontalProductScrollModel(Id, Img,title, subtitle, Price));

                                }
                            }
                            lists.get(index).add(new HomePageModel(0, sliderModelList));
                            lists.get(index).add(new HomePageModel(1,"#Tendance",horizontalProductScrollModelList,ViewAllProductList));
                            lists.get(index).add(new HomePageModel(2,"Offres du jour",GridLayoutModelList));

                            HomePageAdapter homePageAdapter= new HomePageAdapter(lists.get(index));
                            homePageRecyclerView.setAdapter(homePageAdapter);
                            homePageAdapter.notifyDataSetChanged();
                           swipeRefreshLayout.setRefreshing(false);
                        }else{
                            String error = task.getException().getMessage();
                            Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void loadWishList(final Context context, final Dialog dialog, final boolean loadProductData){
        wishList.clear();
        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_WISHLIST")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    for(long x=0;x<(long)task.getResult().get("list_size");x++){
                        wishList.add(task.getResult().get("product_ID_"+x).toString());
                        if(DBqueries.wishList.contains(ProductDetailsActivity.ProductID)){
                            ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST = true;
                            if(ProductDetailsActivity.addToWishListBtn != null){
                                ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.WishColor));
                            }
                        }else{
                            if(ProductDetailsActivity.addToWishListBtn != null){
                                ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#A5A4A4")));
                            }
                            ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST = false;
                        }
                        if(loadProductData) {
                            wishlistModelList.clear();
                            String productID = task.getResult().get("product_ID_"+x).toString();
                            firebaseFirestore.collection("CATEGORIES")
                                    .document("HOME")
                                    .collection("PRD").whereEqualTo("product_ID",productID)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()) {
                                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                            wishlistModelList.add(new WishlistModel(documentSnapshot.get("product_ID").toString(),documentSnapshot.get("product_image_1").toString(),
                                                    documentSnapshot.get("product_title").toString(),
                                                     documentSnapshot.get("averge_rating").toString(),
                                                    (long) documentSnapshot.get("total_rating"),
                                                    documentSnapshot.get("product_price").toString(),
                                                    documentSnapshot.get("cutted_price").toString(),
                                                    (boolean) documentSnapshot.get("COD")));
                                            MyWishlistFragment.wishlistAdapter.notifyDataSetChanged();
                                        }
                                    }else{
                                        String error = task.getException().getMessage();
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                }}
                            });
                        }
                    }
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }

    public static void removeFromWishList(final int index, final Context context){
        final String removedProductID = wishList.get(index);
        wishList.remove(index);
        Map<String,Object> updateWishlist = new HashMap<>();
        for(int x = 0;x<wishList.size();x++){
            updateWishlist.put("product_ID_"+x,wishList.get(x));
        }
        updateWishlist.put("list_size",(long)wishList.size());

        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_WISHLIST")
                .set(updateWishlist).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if(wishlistModelList.size() != 0){
                        wishlistModelList.remove(index);
                        MyWishlistFragment.wishlistAdapter.notifyDataSetChanged();
                    }
                    ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST = false;
                    Toast.makeText(context,"Removed successfully", Toast.LENGTH_SHORT).show();
                }else{
                    if(ProductDetailsActivity.addToWishListBtn != null) {
                        ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.WishColor));
                    }
                    wishList.add(index,removedProductID);
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
                ProductDetailsActivity.running_wishlist_query =false;
            }
        });
    }

    public static void loadRatingList(final Context context){
        if(!ProductDetailsActivity.running_rating_query) {
            ProductDetailsActivity.running_rating_query = true;
            myRatedIds.clear();
            myRating.clear();
            firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_RATINGS")
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (long x = 0; x < (long) task.getResult().get("list_size"); x++) {
                            myRatedIds.add(task.getResult().get("product_ID_" + x).toString());
                            myRating.add((long) task.getResult().get("rating_" + x));

                            if (task.getResult().get("product_ID_" + x).toString().equals(ProductDetailsActivity.ProductID)) {
                                ProductDetailsActivity.initialRating = Integer.parseInt(String.valueOf((long) task.getResult().get("rating_" + x))) - 1;
                                if(ProductDetailsActivity.rateNowContainer != null){
                                    ProductDetailsActivity.setRating(ProductDetailsActivity.initialRating);
                                }
                            }
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                    }
                    ProductDetailsActivity.running_rating_query = false;
                }
            });
        }
    }

    public static void loadCartList(final Context context, final Dialog dialog , final boolean loadProductData, final TextView badgeCount){
        cartList.clear();
        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    for(long x=0;x<(long)task.getResult().get("list_size");x++){
                        cartList.add(task.getResult().get("product_ID_"+x).toString());
                        if(DBqueries.cartList.contains(ProductDetailsActivity.ProductID)){
                            ProductDetailsActivity.ALREADY_ADDED_TO_CART = true;

                        }else{

                            ProductDetailsActivity.ALREADY_ADDED_TO_CART = false;
                        }
                        if(loadProductData) {
                            cartItemModelList.clear();
                            String productID = task.getResult().get("product_ID_"+x).toString();
                            firebaseFirestore.collection("CATEGORIES")
                                    .document("HOME")
                                    .collection("PRD").whereEqualTo("product_ID",productID)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()) {
                                        int index = 0;
                                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                            if(cartList.size() >= 2){
                                                index = cartList.size() - 2;
                                            }
                                            cartItemModelList.add(index,new CartItemModel(CartItemModel.CART_ITEM,documentSnapshot.get("product_ID").toString(),documentSnapshot.get("product_image_1").toString(),
                                                    documentSnapshot.get("product_title").toString(),
                                                    documentSnapshot.get("product_price").toString(),
                                                    documentSnapshot.get("cutted_price").toString(),
                                                    (long) 1,
                                                    (long) 0));

                                            if(cartList.size() == 1){
                                                cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));
                                            }
                                            if (cartList.size() == 0){
                                                cartItemModelList.clear();
                                            }

                                            MyCartFragment.cartAdapter.notifyDataSetChanged();
                                        }
                                    }else{
                                        String error = task.getException().getMessage();
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                    }}
                            });
                        }
                    }
                    if(cartList.size() != 0){
                        badgeCount.setVisibility(View.VISIBLE);
                    }else{
                        badgeCount.setVisibility(View.INVISIBLE);
                    }
                    if(DBqueries.cartList.size() < 99) {
                        badgeCount.setText(String.valueOf(DBqueries.cartList.size()));
                    }else{
                        badgeCount.setText("99+");
                    }
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }

    public static void removeFromCart(final int index,final Context context){
        final String removedProductID = cartList.get(index);
        cartList.remove(index);
        Map<String,Object> updateCartList = new HashMap<>();

        for(int x = 0;x<cartList.size();x++){
            updateCartList.put("product_ID_"+x,cartList.get(x));
        }
        updateCartList.put("list_size",(long)cartList.size());

        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                .set(updateCartList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if(cartItemModelList.size() != 0){
                        cartItemModelList.remove(index);
                        MyCartFragment.cartAdapter.notifyDataSetChanged();
                    }
                    if(cartList.size() == 0){
                        cartItemModelList.clear();
                    }
                    Toast.makeText(context,"Removed successfully", Toast.LENGTH_SHORT).show();
                }else{
                    cartList.add(index,removedProductID);
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
                ProductDetailsActivity.running_cart_query =false;
            }
        });
    }

    public static void loadAddresses(final Context context, final Dialog loadingDialog){
        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_ADDRESSES")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Intent deliveryIntent;
                   /* if(task.getResult().get("list_size") == 0){
                         deliveryIntent = new Intent(context,AddAddressActivity.class);
                    }else{
                         deliveryIntent = new Intent(context,AddAddressActivity.class);
                    }
                    context.startActivity(deliveryIntent);*/
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });

    }

    public static void clearData(){
        categoryModelList.clear();
        lists.clear();
        loadedCategoriesNames.clear();
        wishList.clear();
        wishlistModelList.clear();
        cartList.clear();
        cartItemModelList.clear();
    }
}
