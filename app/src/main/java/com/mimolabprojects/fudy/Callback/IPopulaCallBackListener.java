package com.mimolabprojects.fudy.Callback;

import com.mimolabprojects.fudy.Model.PopularCategoryModel;

import java.util.List;

public interface IPopulaCallBackListener {

    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);
    void onPopularLoadFailed(String message);
}
