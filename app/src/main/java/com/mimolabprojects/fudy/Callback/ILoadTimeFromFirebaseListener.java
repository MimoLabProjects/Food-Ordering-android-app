package com.mimolabprojects.fudy.Callback;

import com.mimolabprojects.fudy.Model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(OrderModel order, long estimateTimeinMs);
    void onLoadTimeFailed(String message);
}
