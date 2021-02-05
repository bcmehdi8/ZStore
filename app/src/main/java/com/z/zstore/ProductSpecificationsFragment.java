package com.z.zstore;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;




/**
 * A simple {@link Fragment} subclass.
 */
public class ProductSpecificationsFragment extends Fragment {


    public ProductSpecificationsFragment() {
        // Required empty public constructor
    }

    private RecyclerView productSpecificationRecyclerView;
    public List<ProductSpecificationModel> productSpecificationModelList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_specifications, container, false);
        productSpecificationRecyclerView = view.findViewById(R.id.product_specification_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productSpecificationRecyclerView.setLayoutManager(linearLayoutManager);

       /* productSpecificationModelList.add(new ProductSpecificationModel("Type","Casual Shoes"));
        productSpecificationModelList.add(new ProductSpecificationModel("Materials","MD,Mesh Fabric"));
        productSpecificationModelList.add(new ProductSpecificationModel("Style","Comfortable,Fashion"));
        productSpecificationModelList.add(new ProductSpecificationModel("Outsole Material","MD"));
        productSpecificationModelList.add(new ProductSpecificationModel("Upper Material","Mesh Fabric"));
        productSpecificationModelList.add(new ProductSpecificationModel("Seasons","Summer"));*/

        ProductSpecificationAdapter productSpecificationAdapter = new ProductSpecificationAdapter(productSpecificationModelList);
        productSpecificationRecyclerView.setAdapter(productSpecificationAdapter);
        productSpecificationAdapter.notifyDataSetChanged();

        return view;
    }

}
