package com.mimolabprojects.fudy.ui.menu;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mimolabprojects.fudy.Callback.ICategoryCallbackLstener;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Model.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class MenuViewModel extends ViewModel implements ICategoryCallbackLstener {

   private MutableLiveData<List<CategoryModel>> categoryListMtable;
   private MutableLiveData<String> messageError = new MutableLiveData<>();
   private ICategoryCallbackLstener categoryCallbackLstener;

    public MenuViewModel() {

        categoryCallbackLstener = this;

    }

    public MutableLiveData<List<CategoryModel>> getCategoryListMtable() {
        if (categoryListMtable == null){
            categoryListMtable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategories();
        }
        
        return categoryListMtable;
    }

    private void loadCategories() {

        List<CategoryModel> templist = new ArrayList<>();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot itemsnapshot:dataSnapshot.getChildren())
                {
                    CategoryModel categoryModel = itemsnapshot.getValue(CategoryModel.class);
                    categoryModel.setMenu_id(itemsnapshot.getKey());
                    templist.add(categoryModel);
                }

                categoryCallbackLstener.onCategoryLoadSucess(templist);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                categoryCallbackLstener.onCategoryLoadFailed(databaseError.getMessage());
            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSucess(List<CategoryModel> categoryModelList) {

        categoryListMtable.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {

        messageError.setValue(message);

    }
}