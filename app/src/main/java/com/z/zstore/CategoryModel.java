package com.z.zstore;

public class CategoryModel {

    private String CategoryIconLink;
    private String CategoryName;

    public CategoryModel(String categoryIconLink, String CategoryName){
        this.CategoryIconLink = categoryIconLink;
        this.CategoryName = CategoryName;
    }

    public String getCategoryIconLink() {
        return CategoryIconLink;
    }

    public void setCategoryIconLink(String categoryIconLink) {
        CategoryIconLink = categoryIconLink;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }
}
