package com.mimolabprojects.fudy.ui.foodlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Model.FoodModel;

import java.util.List;

public class FoodListViewModel extends ViewModel {

    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;

    public FoodListViewModel() {

    }

    public MutableLiveData<List<FoodModel>> getmutableLiveDataFoodList() {

        if (mutableLiveDataFoodList == null){

            mutableLiveDataFoodList = new MutableLiveData<>();
            mutableLiveDataFoodList.setValue(Common.categorySelected.getFoods());
        }
        return mutableLiveDataFoodList;
    }
}