package com.mimolabprojects.fudy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.andremion.counterfab.CounterFab;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Database.CartDataSource;
import com.mimolabprojects.fudy.Database.CartDatabase;
import com.mimolabprojects.fudy.Database.LocalCartDataSource;
import com.mimolabprojects.fudy.EventBus.BestDealsItemClick;
import com.mimolabprojects.fudy.EventBus.CategoryClick;
import com.mimolabprojects.fudy.EventBus.CounterCartEvent;
import com.mimolabprojects.fudy.EventBus.FoodItemClick;
import com.mimolabprojects.fudy.EventBus.HideFABCart;
import com.mimolabprojects.fudy.EventBus.MenuItemBack;
import com.mimolabprojects.fudy.EventBus.PopularCategoryClick;
import com.mimolabprojects.fudy.Model.CategoryModel;
import com.mimolabprojects.fudy.Model.FoodModel;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Homectivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;

    private CartDataSource cartDataSource;

    android.app.AlertDialog dialog;

    int menuClickId = -1;

    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onResume() {
        super.onResume();
        counterCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homectivity);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,R.id.nav_view_orders,
                R.id.nav_sign_out, R.id.nav_cart, R.id.nav_food_list)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hello  ", Common.currentUser.getName(),txt_user);

        counterCartItem();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.homectivity, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId())
        {
            case R.id.nav_home:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_home);
                break;

            case R.id.nav_menu:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_menu);
                break;
                
            case R.id.nav_cart:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_view_orders:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_view_orders);
                break;

            case R.id.nav_sign_out:
                signOut();
                break;

        }
        menuClickId = item.getItemId();
        return true;
    }

    private void signOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logging Out")
                .setMessage("Do You really want to Logout?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(Homectivity.this, LoginRegisterActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }


    //Event Bus Receive


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    //Evenet for clicked category to see foods

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event){
        if (event.isSuccess()){

            navController.navigate(R.id.nav_food_list);
           // Toast.makeText(this, "Click"+event.getCategoryModel().getName(),Toast.LENGTH_SHORT).show();
        }
    }

    //// Food Item Clicked to see details

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClicked(FoodItemClick event){
        if (event.isSuccess()){

            navController.navigate(R.id.nav_food_detail);
            // Toast.makeText(this, "Click"+event.getCategoryModel().getName(),Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onHiddenFAB(HideFABCart event){
        if (event.isHidden()){

          fab.hide();
        }
        else
            fab.show();
    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event){
        if (event.isSuccess()){
            
            counterCartItem();
        }
    }
    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealsItemClicked(BestDealsItemClick event){
        if (event.getBestDealsModel()!=null){

            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealsModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists())
                            {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());


                                //==========================LOAD FOOD
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealsModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealsModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists())
                                                {
                                                    for (DataSnapshot itemsnapshot: dataSnapshot.getChildren())
                                                    {
                                                        Common.selectedFood = itemsnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemsnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }

                                                else {

                                                    Toast.makeText(Homectivity.this, "Item Does Not Exist", Toast.LENGTH_SHORT).show();

                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                dialog.dismiss();
                                                Toast.makeText(Homectivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();


                                            }
                                        });


                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(Homectivity.this, "Item Does Not Exist in Database", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            dialog.dismiss();
                            Toast.makeText(Homectivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClicked(PopularCategoryClick event){
        if (event.getPopularCategoryModel()!=null){

            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists())
                            {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());


                                //==========================LOAD FOOD
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists())
                                                {
                                                    for (DataSnapshot itemsnapshot: dataSnapshot.getChildren())
                                                    {
                                                        Common.selectedFood = itemsnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemsnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }

                                                else {

                                                    Toast.makeText(Homectivity.this, "Item Does Not Exist", Toast.LENGTH_SHORT).show();

                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                dialog.dismiss();
                                                Toast.makeText(Homectivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();


                                            }
                                        });


                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(Homectivity.this, "Item Does Not Exist in Database", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            dialog.dismiss();
                            Toast.makeText(Homectivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    private void counterCartItem(

    ) {



        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                        {

                            Toast.makeText(Homectivity.this, "[COUNT CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else
                            fab.setCount(0);
                    }
                });

    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack (MenuItemBack event)
    {
        menuClickId = -1;
        //navController.popBackStack(R.id.nav_home, true);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
    }
}
