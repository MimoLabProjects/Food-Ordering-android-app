package com.mimolabprojects.fudy.ui.fooddetail;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Database.CartDataSource;
import com.mimolabprojects.fudy.Database.CartDatabase;
import com.mimolabprojects.fudy.Database.CartItem;
import com.mimolabprojects.fudy.Database.LocalCartDataSource;
import com.mimolabprojects.fudy.EventBus.CounterCartEvent;
import com.mimolabprojects.fudy.EventBus.MenuItemBack;
import com.mimolabprojects.fudy.Model.AddonModel;
import com.mimolabprojects.fudy.Model.CommentModel;
import com.mimolabprojects.fudy.Model.FoodModel;
import com.mimolabprojects.fudy.Model.SizeModel;
import com.mimolabprojects.fudy.R;
import com.mimolabprojects.fudy.ui.comments.CommentFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {

    private FoodDetailiewModel fooddetailViewModel;
    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<FoodModel> foodModelList;

    private Unbinder unbinder;
    private android.app.AlertDialog waitingDialog;
    private BottomSheetDialog addonBottomSheetDialog;


    //Inflatiating a View
    ChipGroup chip_group_addon;
    EditText edt_search;

    @BindView(R.id.img_food)
    ImageView img_food;
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.btnShowComment)
    Button btnShowComment;
    @BindView(R.id.rdi_group_size)
    RadioGroup rdi_group_size;

    //add On
    @BindView(R.id.img_add_on)
    ImageView img_add_on;
    @BindView(R.id.chip_group_user_selected_addon)
    ChipGroup chip_group_user_selected_addon;


    @OnClick(R.id.img_add_on)
    void onAddonClick(){
        if (Common.selectedFood.getAddon() !=null)
        {
            displayAddOnList(); // Show all addon options
            addonBottomSheetDialog.show();
        }
    }

    @OnClick(R.id.btnShowComment)
    void onShowCommentButtonClick(){

        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager(),"Comment Fragments");
    }

    @OnClick (R.id.btnCart)
    void onCartItemAdd(){



            CartItem cartItem = new CartItem();
            cartItem.setUid(Common.currentUser.getUid());
            cartItem.setUserPhone(Common.currentUser.getPhone());

            cartItem.setCategoryId(Common.categorySelected.getMenu_id());
            cartItem.setFoodId(Common.selectedFood.getId());
            cartItem.setFoodName(Common.selectedFood.getName());
            cartItem.setFoodImage(Common.selectedFood.getImage());
            cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
            cartItem.setFoodQuantity(Integer.valueOf(numberButton.getNumber()));
            cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),Common.selectedFood.getUserSelectedAddon()));//By default size and Addon is not chosen


            if (Common.selectedFood.getUserSelectedAddon() !=null)
                cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
            else
                cartItem.setFoodAddon("Default");


            if (Common.selectedFood.getUserSelectedSize() !=null)
                cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
            else
                cartItem.setFoodSize("Default");




        cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                Common.categorySelected.getMenu_id(),
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDb) {

                        if (cartItemFromDb.equals(cartItem))
                        {
                            cartItemFromDb.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDb.setFoodAddon(cartItem.getFoodAddon());
                            cartItemFromDb.setFoodSize(cartItem.getFoodSize());
                            cartItemFromDb.setFoodQuantity(cartItemFromDb.getFoodQuantity()
                                    + cartItem.getFoodQuantity());

                            cartDataSource.updateCartItems(cartItemFromDb)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            Toast.makeText(getContext(), "Updated cart Successfully", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                                        }
                                    });
                        }
                        else {

                            //If Item is not available in cart Insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(()->{
                                        Toast.makeText(getContext(), "Added to cart Successfully", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    },throwable -> {
                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();



                                    }));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (e.getMessage().contains("empty")){
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(()->{
                                        Toast.makeText(getContext(), "Added to cart Successfully", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    },throwable -> {
                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();



                                    }));
                        }else {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }



                    }
                });



    }



    private void displayAddOnList() {
        if  (Common.selectedFood.getAddon().size() > 0)
        {
            chip_group_addon.clearCheck();//clear check all views
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);
            for (AddonModel addonModel: Common.selectedFood.getAddon())
            {

                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_items, null);
                    chip.setText( new StringBuilder(addonModel.getName()).append("(+Ksh")
                            .append(addonModel.getPrice()).append(")"));
                    chip.setOnCheckedChangeListener((compoundButton, b) -> {

                        if (b)
                        {
                            if (Common.selectedFood.getUserSelectedAddon() == null)
                                Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                            Common.selectedFood.getUserSelectedAddon().add(addonModel);
                        }

                    });

                    chip_group_addon.addView(chip);

            }
        }
    }


    @OnClick(R.id.btn_rating)
    void onRatingButtonClick(){
        
        showDialogRating();
    }

    private void showDialogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder (getContext());
        builder.setTitle("Rating Food");
        builder.setMessage("Please Fill in the Information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null);

        RatingBar ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
        EditText edt_comment = (EditText) itemView.findViewById(R.id.edt_comment);

        builder.setView(itemView);

        builder.setNegativeButton(  "CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        builder.setPositiveButton("OK", (dialogInterface, i) -> {

            CommentModel commentModel = new CommentModel();
            commentModel.setName(Common.currentUser.getName());
            commentModel.setUid(Common.currentUser.getUid());
            commentModel.setComment(edt_comment.getText().toString());
            commentModel.setRatingValue(ratingBar.getRating());
            Map<String, Object> serverTimestamp = new HashMap<>();
            serverTimestamp.put("timeStamp", ServerValue.TIMESTAMP);
            commentModel.setCommentTimeStamp(serverTimestamp);


            fooddetailViewModel.setCommentModel(commentModel);

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fooddetailViewModel =
                ViewModelProviders.of(this).get(FoodDetailiewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);


        unbinder = ButterKnife.bind(this, root);
        initViews();

        fooddetailViewModel.getMutableLiveDataFood().observe(this, foodModel -> {
            
            displayInfo(foodModel);
        });
        fooddetailViewModel.getMutableLiveDataComment().observe(this, commentModel -> {

            submitRatingToFirebase(commentModel);
        });
        return root;
    }

    private void initViews() {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();


//================Add On ==============================================================================
        addonBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.Dialogstyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText) layout_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);


        addonBottomSheetDialog.setOnDismissListener(dialogInterface -> {

            displayUserSelectedAddon();
            calculateTotalPrice();

        });


    }

    private void displayUserSelectedAddon() {
        if (Common.selectedFood.getUserSelectedAddon() !=null &&
        Common.selectedFood.getUserSelectedAddon().size() > 0)
        {
            chip_group_user_selected_addon.removeAllViews();// Clear all views Already
            for (AddonModel addonModel: Common.selectedFood.getUserSelectedAddon())// adding the available views to the list
            {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+Ksh ")
                .append(addonModel.getPrice()).append(")"));

                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {

                    //Remooves the Addon when user clicks remove icon
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });

                chip_group_user_selected_addon.addView(chip);

            }
        }
        else
            chip_group_user_selected_addon.removeAllViews();



    }

    //================Submitting Rating To Database==============================================================================
    private void submitRatingToFirebase(CommentModel commentModel) {



        waitingDialog.show();

        //=========SUBMITTING COMMENTS TO DB COMMENTS TABLE

        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        //Updating values on DB after submission

                        addRatingToFood(commentModel.getRatingValue());
                    }

                    waitingDialog.dismiss();

                });

    }

    private void addRatingToFood(float ratingValue) {

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())// Select Category
                .child("foods")//Select from array of foods in particular category
                .child(Common.selectedFood.getKey())//INdex of the foood in the array

                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {
                            FoodModel foodModel = dataSnapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectedFood.getKey());

            //============================================Apply Rating ============================================

                           if (foodModel.getRatingValue() == null)
                               foodModel.setRatingValue(0d);
                           if (foodModel.getRatingCount()==null){
                               foodModel.setRatingCount(0l);
                           }

                            double sumRating = foodModel.getRatingValue()+ratingValue;
                            long ratingCount = foodModel.getRatingCount()+1;


                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("ratingValue", sumRating);
                            updateData.put("ratingCount", ratingCount);

          //=================================update the data in interface// show rating stars in the interface
                            foodModel.setRatingValue(sumRating);
                            foodModel.setRatingCount(ratingCount );


                            dataSnapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(task -> {

                                        waitingDialog.dismiss();
                                        if (task.isSuccessful()){
                                            Toast.makeText(getContext(), "Thank You For Rating Us", Toast.LENGTH_SHORT).show();
                                            Common.selectedFood = foodModel;
                                            fooddetailViewModel.setFoodModel(foodModel);//Refreshing the interface rating
                                        }
                                    });
                        }

                        else {
                            waitingDialog.dismiss();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        waitingDialog.dismiss();
                        Toast.makeText(getContext(), ""+databaseError, Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void displayInfo(FoodModel foodModel) {

        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));


        if (foodModel.getRatingValue()!=null)
            ratingBar.setRating(foodModel.getRatingValue().floatValue()/ foodModel.getRatingCount());

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectedFood.getName());


        //Change Size Model
        if (Common.selectedFood.getSize() !=null) {
            for (SizeModel sizeModel : Common.selectedFood.getSize()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setOnCheckedChangeListener((compoundButton, b) -> {

                    if (b)
                        Common.selectedFood.setUserSelectedSize(sizeModel);
                    calculateTotalPrice();//Calculate and Update the Total Price

                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTag(sizeModel.getPrice());

                rdi_group_size.addView(radioButton);
            }


            if (rdi_group_size.getChildCount() > 0)
            {
                //=================================================setting Default as 1 selected
                RadioButton radioButton = (RadioButton) rdi_group_size.getChildAt(0);
                radioButton.setChecked(true);
            }

        }
        calculateTotalPrice();
    }

    private void calculateTotalPrice (){

        double totalPrice = Double.parseDouble(Common.selectedFood.getPrice().toString()), displayPrice=0.0;

        //On Addon Calculate
        if (Common.selectedFood.getUserSelectedAddon() !=null && Common.selectedFood.getUserSelectedAddon().size()>0)
                for (AddonModel addonModel: Common.selectedFood.getUserSelectedAddon())
                    totalPrice += Double.parseDouble(addonModel.getPrice().toString());


        //On Size Calculate
        if (Common.selectedFood.getUserSelectedSize() !=null)
            totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());

        displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
        displayPrice = Math.round(displayPrice*100.0/100.0);

        food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ///////////////////////////////////////////////////////////////////
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        edt_search.addTextChangedListener(this);

        ///=======================ADD ALL VIEWS

        for (AddonModel addonModel: Common.selectedFood.getAddon())
        {

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_items, null);
                chip.setText( new StringBuilder(addonModel.getName()).append("(+Ksh")
                .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {

                    if (b)
                    {
                        if (Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }

                });

                chip_group_addon.addView(chip);
            }


    }

    @Override
    public void afterTextChanged(Editable editable) {
        ///////////////////////////////////////////////////////////////////
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}