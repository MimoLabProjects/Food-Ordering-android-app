package com.mimolabprojects.fudy.Callback;

import com.mimolabprojects.fudy.Model.OrderModel;

import java.util.List;

public interface ILoadOrderCallBacklistener {

    void onOrderLoadSuccess (List<OrderModel> orderList);
    void onOrderLoadFailed (String message);

}
