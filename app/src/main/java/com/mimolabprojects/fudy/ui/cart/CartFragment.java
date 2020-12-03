package com.mimolabprojects.fudy.ui.cart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mimolabprojects.fudy.Adapter.MyCartAdapter;
import com.mimolabprojects.fudy.Callback.ILoadTimeFromFirebaseListener;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Common.MySwiperHlelper;
import com.mimolabprojects.fudy.Database.CartDataSource;
import com.mimolabprojects.fudy.Database.CartDatabase;
import com.mimolabprojects.fudy.Database.CartItem;
import com.mimolabprojects.fudy.Database.LocalCartDataSource;
import com.mimolabprojects.fudy.EventBus.CounterCartEvent;
import com.mimolabprojects.fudy.EventBus.HideFABCart;
import com.mimolabprojects.fudy.EventBus.MenuItemBack;
import com.mimolabprojects.fudy.EventBus.UpdateItemInCart;
import com.mimolabprojects.fudy.Model.FCMResponse;
import com.mimolabprojects.fudy.Model.FCMSendData;
import com.mimolabprojects.fudy.Model.OrderModel;
import com.mimolabprojects.fudy.R;
import com.mimolabprojects.fudy.Retrofit.IFCMService;
import com.mimolabprojects.fudy.Retrofit.RetrofitFCMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    private CartViewModel cartViewModel;
    private  MyCartAdapter adapter;
    private Unbinder unbinder;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    ILoadTimeFromFirebaseListener listener;
    IFCMService ifcmService;
  
    @BindView(R.id.recycler_carts)
    RecyclerView recycler_carts;
    @BindView(R.id.txt_total_prices)
    TextView txt_total_prices;
    @BindView(R.id.txt_empty_carts)
    TextView txt_empty_carts;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;




//=========================PLACING ORDER===================================
    @OnClick (R.id.btn_place_orders)
    void onPlaceOrderClick(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Almost there, just one more step!!");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_addresses = (EditText)view.findViewById(R.id.edt_addresses);
        EditText edt_comments = (EditText)view.findViewById(R.id.edt_comments);
        TextView txt_address = (TextView)view.findViewById(R.id.txt_address_details);
        RadioButton rdi_home = (RadioButton)view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_addres = (RadioButton)view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = (RadioButton)view.findViewById(R.id.rdi_ship_this_address);

        RadioButton rdi_cod = (RadioButton)view.findViewById(R.id.rdi_cod);
        RadioButton rdi_mpesa = (RadioButton)view.findViewById(R.id.rdi_mpesa);

        edt_addresses.setText(Common.currentUser.getAddress());//By default Home Address is Chosen

        //==========================EVENt
        rdi_home.setOnCheckedChangeListener((compoundButton, b) -> {

            if (b){
                edt_addresses.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.GONE);
            }
        });
        rdi_other_addres.setOnCheckedChangeListener((compoundButton, b) -> {

            if (b){
                edt_addresses.setText("");
                edt_addresses.setHint("Enter New Address");
                txt_address.setVisibility(View.GONE);
            }
        });
        rdi_ship_to_this.setOnCheckedChangeListener((compoundButton, b) -> {

            if (b){
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {

                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            txt_address.setVisibility(View.GONE);
                        })
                        .addOnCompleteListener(task -> {

                            String coordinates = new StringBuilder()
                                    .append(task.getResult().getLatitude())
                                    .append(" / ")
                                    .append(task.getResult().getLongitude()).toString();

            ////========================================GET LOCATION BY NAME============================================================================================
                            Single<String> singleAddress = Single.just(getAddressFromLongLat(
                                    task.getResult().getLatitude(),task.getResult().getLongitude()
                            ));

                            Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                                @Override
                                public void onSuccess(String s) {

                                    edt_addresses.setText(coordinates);
                                    txt_address.setText(s);
                                    txt_address.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError(Throwable e) {

                                    edt_addresses.setText(coordinates);
                                    txt_address.setText(e.getMessage());
                                    txt_address.setVisibility(View.VISIBLE);

                                }
                            });


                        });
            }
        });





        builder.setView(view);
        builder.setNegativeButton("No", (dialogInterface, i) -> {

            dialogInterface.dismiss();

        }).setPositiveButton("Continue", (dialogInterface, i) -> {

            //Toast.makeText(getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_SHORT).show();

            if(rdi_cod.isChecked())
            {
                paymentCOD(edt_addresses.getText().toString(), edt_comments.getText().toString());
            }
        });

        AlertDialog dialog = builder.create();
      /*  dialog.setOnDismissListener(dialogInterface -> {

        });*/
        dialog.show();

    }

//==============================IMPLEMENTING PAYMENT BY COD====================================================================

    private void paymentCOD(String adress, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {

            //Get Total of all items in Cart
            cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Double>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Double totalPrice) {

                            double finalPrice = totalPrice;
                            OrderModel order = new OrderModel();
                            order.setUserId(Common.currentUser.getUid());
                            order.setUserName(Common.currentUser.getName());
                            order.setUserPhone(Common.currentUser.getPhone());
                            order.setShippingAddress(adress);
                            order.setComments(comment);

                            if (currentLocation != null)
                            {
                                order.setLat(currentLocation.getLatitude());
                                order.setLang(currentLocation.getLongitude());
                            }
                            else {
                                order.setLat(-0.1f);
                                order.setLang(-0.1f);
                            }

                            order.setCartItemList(cartItems);
                            order.setTotalPayment(totalPrice);
                            order.setDiscount(0);
                            order.setFinalPayment(finalPrice);
                            order.setCod(true);
                            order.setTransactionId("Cash On Delivery");

                            ////=============SUBMITTiNG ORDER TO DB

                          syncLocalTimeWithGlobalTime(order);

                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }, throwable -> {

            Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }
    //======================SYNCHRONIZING LOCAL TIME WITH GLOBAL TIME================================================
    private void syncLocalTimeWithGlobalTime(OrderModel order) {

        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Offset time in between local time and sever time

                long offset = dataSnapshot.getValue(Long.class);
                long estimatedTimeServerTimeMs =System.currentTimeMillis()+offset;
                SimpleDateFormat sd = new SimpleDateFormat("MM dd,yyyy, HH:mm");
                Date resultDate = new Date(estimatedTimeServerTimeMs);
                Log.d("TEST_DATE",""+sd.format(resultDate));


                listener.onLoadTimeSuccess(order, estimatedTimeServerTimeMs);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onLoadTimeFailed(databaseError.getMessage());
            }
        });

    }
//======================SUBMITING ORDERS TO DB================================================

    private void writeOrderToFirebaseDb(OrderModel order) {

        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    cartDataSource.clearCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {

                                    Toast.makeText(getContext(), "Order Placed Successfully", Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                /*    Map<String, String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTIFICATION_TITLE,"New Order");
                                    notiData.put(Common.NOTIFICATION_CONTENT,"You have a new order from"+Common.currentUser.getPhone());

                                    FCMSendData sendData = new FCMSendData(Common.createTopicOrder(),notiData);

                                  compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        Toast.makeText(getContext(), "Order Placed Successfully", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {

                                    }));
*/

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getContext(), "[ORDER_ERROR !!]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                });
    }



    private String getAddressFromLongLat(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result="";

        try {
            List<Address>  addressList = geocoder.getFromLocation(latitude,longitude , 1);
            if (addressList !=null && addressList.size()>0)
            {
                Address address = addressList.get(0); // Always Get First Item
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            }
            else
                result = "Cannot Find Location";
        } catch (IOException e) {

            e.printStackTrace();
            result = e.getMessage();

        }

        return result;


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        listener = this;

        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(this, cartItems -> {
            if (cartItems == null || cartItems.isEmpty())
            {
                    recycler_carts.setVisibility(View.GONE);
                    group_place_holder.setVisibility(View.GONE);
                    txt_empty_carts.setVisibility(View.VISIBLE);
            }
            else {
                recycler_carts.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_carts.setVisibility(View.GONE);



                adapter = new MyCartAdapter(getContext(),cartItems);
                recycler_carts.setAdapter(adapter);



            }
        });

        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();
        return root;
    }

    private void initLocation() {
        
        builderLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest
        ,locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallBack() {

        locationCallback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void builderLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    private void initViews() {

        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        recycler_carts.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_carts.setLayoutManager(layoutManager);
        recycler_carts.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwiperHlelper mySwiperHlelper = new MySwiperHlelper(getContext(),recycler_carts,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buff) {

                buff.add(new MyButton(getContext(), "Delete Item", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                           CartItem cartItem = adapter.getItemPosition(pos);
                           cartDataSource.deleteCartItems(cartItem)
                                   .subscribeOn(Schedulers.io())
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .subscribe(new SingleObserver<Integer>() {
                                       @Override
                                       public void onSubscribe(Disposable d) {

                                       }

                                       @Override
                                       public void onSuccess(Integer integer) {
                                           adapter.notifyItemRemoved(pos);
                                           EventBus.getDefault().postSticky(new CounterCartEvent(true));//Updating FAB
                                           Toast.makeText(getContext(), "Item Deleted from CART Successfully", Toast.LENGTH_SHORT).show();

                                       }

                                       @Override
                                       public void onError(Throwable e) {

                                           Toast.makeText(getContext(), "[DELETE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                       }
                                   });
                        }));
            }
        };

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false); //hide Home Menu
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart)
        {
            cartDataSource.clearCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(), "CART Cleared Successfully", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();

        EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().postSticky(new CounterCartEvent(false));
        cartViewModel.OnStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient !=null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient !=null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event)
    {

        if (event.getCartItem() !=null)
        {
           //Save Current State of RecyclerView
           recyclerViewState = recycler_carts.getLayoutManager().onSaveInstanceState();
           cartDataSource.updateCartItems(event.getCartItem())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new SingleObserver<Integer>() {
                       @Override
                       public void onSubscribe(Disposable d) {

                       }

                       @Override
                       public void onSuccess(Integer integer) {

                           calculateTotalPrice();
                           recycler_carts.getLayoutManager().onRestoreInstanceState(recyclerViewState);// Fixes Error When refreshing Recycler View After Update

                       }

                       @Override
                       public void onError(Throwable e) {
                           Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                   });

       }
        }


    private void calculateTotalPrice() {
         cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new SingleObserver<Double>() {
                     @Override
                     public void onSubscribe(Disposable d) {

                     }

                     @Override
                     public void onSuccess(Double price) {
                        txt_total_prices.setText(new StringBuilder("Total: Ksh ")
                        .append(Common.formatPrice(price)));
                     }

                     @Override
                     public void onError(Throwable e) {

                         if (!e.getMessage().contains("Query returned empty result set"))
                             Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                     }
                 });
    }

    @Override
    public void onLoadTimeSuccess(OrderModel order, long estimateTimeinMs) {

        order.setCreateDate(estimateTimeinMs);
        order.setOrderStatus(0);
        writeOrderToFirebaseDb(order);
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}

