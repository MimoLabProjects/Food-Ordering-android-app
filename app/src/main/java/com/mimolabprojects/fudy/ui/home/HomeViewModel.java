package com.mimolabprojects.fudy.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mimolabprojects.fudy.Callback.IBestDealsCallBackListener;
import com.mimolabprojects.fudy.Callback.IPopulaCallBackListener;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Model.BestDealsModel;
import com.mimolabprojects.fudy.Model.PopularCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IPopulaCallBackListener, IBestDealsCallBackListener {

    private MutableLiveData<List<PopularCategoryModel>> popularList;
    private MutableLiveData<List<BestDealsModel>> bestDealList;
    private MutableLiveData<String> messageError;
    private IPopulaCallBackListener popularCallBackListener;
    private IBestDealsCallBackListener bestDealsCallBackListener;

    public HomeViewModel() {

        popularCallBackListener = this;
        bestDealsCallBackListener = this;

    }


    // BEST DEALS LIST DEALS LOAD

    public MutableLiveData<List<BestDealsModel>> getBestDealList() {

        if (bestDealList == null){

            bestDealList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadbestDealList();

        }

        return bestDealList;
    }

    private void loadbestDealList() {

        List<BestDealsModel> templist = new ArrayList<>();
        DatabaseReference bestdealsRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEAL_REF);
        bestdealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot itemsnapshot:dataSnapshot.getChildren())
                {
                    BestDealsModel bmodel = itemsnapshot.getValue(BestDealsModel.class);
                    templist.add(bmodel);
                }
                bestDealsCallBackListener.onBestDealLoadSucess(templist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                bestDealsCallBackListener.onBestDealLoadFailed(databaseError.getMessage());

            }
        });
    }


    //----------------------------------------------------------------------------------------------
    // POPULAR LIST DEALS LOAD

    public MutableLiveData<List<PopularCategoryModel>> getPopularList() {
        if (popularList == null)
        {
            popularList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadpopularlist();
        }
        return popularList;
    }

    private void loadpopularlist() {
        List<PopularCategoryModel> templist = new ArrayList<>();
        DatabaseReference popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_CATEGORY_REF);
        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot itemsnapshot:dataSnapshot.getChildren())
                {
                    PopularCategoryModel model = itemsnapshot.getValue(PopularCategoryModel.class);
                    templist.add(model);
                }
                popularCallBackListener.onPopularLoadSuccess(templist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                popularCallBackListener.onPopularLoadFailed(databaseError.getMessage());

            }
        });
    }



    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels) {
        popularList.setValue(popularCategoryModels);
    }

    @Override
    public void onPopularLoadFailed(String message) {

        messageError.setValue(message);
    }


    //bEAST DEALS LOAD SUCCESS AND FAILS

    @Override
    public void onBestDealLoadSucess(List<BestDealsModel> bestDealsModels) {
        bestDealList.setValue(bestDealsModels);
    }

    @Override
    public void onBestDealLoadFailed(String message) {

        messageError.setValue(message);

    }
}