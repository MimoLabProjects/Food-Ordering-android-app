package com.mimolabprojects.fudy.Callback;

import com.mimolabprojects.fudy.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackLstener {

    void onCategoryLoadSucess (List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);
}
