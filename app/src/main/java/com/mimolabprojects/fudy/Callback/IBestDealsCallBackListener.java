package com.mimolabprojects.fudy.Callback;

import com.mimolabprojects.fudy.Model.BestDealsModel;

import java.util.List;

public interface IBestDealsCallBackListener {

    void onBestDealLoadSucess (List<BestDealsModel> bestDealsModels);
    void onBestDealLoadFailed(String message);
}
