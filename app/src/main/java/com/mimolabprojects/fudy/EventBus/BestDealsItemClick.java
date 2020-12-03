package com.mimolabprojects.fudy.EventBus;

import com.mimolabprojects.fudy.Model.BestDealsModel;

public class BestDealsItemClick {

    private BestDealsModel bestDealsModel;

    public BestDealsItemClick(BestDealsModel bestDealsModel) {
        this.bestDealsModel = bestDealsModel;
    }

    public BestDealsModel getBestDealsModel() {
        return bestDealsModel;
    }

    public void setBestDealsModel(BestDealsModel bestDealsModel) {
        this.bestDealsModel = bestDealsModel;
    }
}
