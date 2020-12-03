package com.mimolabprojects.fudy.ui.vieworders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mimolabprojects.fudy.Model.OrderModel;

import java.util.List;

public class ViewOrdersViewModel extends ViewModel {

    private MutableLiveData<List<OrderModel>> mutableLiveDataOrderlist;

    public ViewOrdersViewModel() {

        mutableLiveDataOrderlist = new MutableLiveData<>();

    }

    public MutableLiveData<List<OrderModel>> getMutableLiveDataOrderlist() {
        return mutableLiveDataOrderlist;
    }

    public void setMutableLiveDataOrderlist(List<OrderModel> orderlist) {

        mutableLiveDataOrderlist.setValue(orderlist);

    }
}