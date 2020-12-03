package com.mimolabprojects.fudy.ui.vieworders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mimolabprojects.fudy.Adapter.MyOrdersAdapter;
import com.mimolabprojects.fudy.Callback.ILoadOrderCallBacklistener;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Common.MySwiperHlelper;
import com.mimolabprojects.fudy.Database.CartItem;
import com.mimolabprojects.fudy.EventBus.CounterCartEvent;
import com.mimolabprojects.fudy.Model.OrderModel;
import com.mimolabprojects.fudy.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallBacklistener {

    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    private Unbinder unbinder;

    AlertDialog dialog;

    private ILoadOrderCallBacklistener listener;



    private ViewOrdersViewModel viewOrdersViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewOrdersViewModel =
                ViewModelProviders.of(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_orders, container, false);

        unbinder = ButterKnife.bind(this, root);
        
        
        initViews(root);
        loadOrdersFromFirebase();

        viewOrdersViewModel.getMutableLiveDataOrderlist().observe(this , orderModelList -> {
            MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(), orderModelList);
            recycler_orders.setAdapter(adapter);
        });
        return root;
    }

    private void loadOrdersFromFirebase() {

        List<OrderModel> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot orderSnapshot: dataSnapshot.getChildren())
                        {
                            OrderModel order = orderSnapshot.getValue(OrderModel.class);
                            order.setOrderNumber(orderSnapshot.getKey());
                            orderList.add(order);
                        }
                        listener.onOrderLoadSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void initViews(View root) {

        listener = this;

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwiperHlelper mySwiperHlelper = new MySwiperHlelper(getContext(),recycler_orders,250) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buff) {

                buff.add(new MyButton(getContext(), "Cancel Order", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);
                            if (orderModel.getOrderStatus() == 0)
                            {
                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                builder.setTitle("Cancel Orders")
                                        .setMessage("Do you really want to cancel this order?")
                                        .setNegativeButton("No", (dialogInterface, i) -> {
                                            dialogInterface.dismiss();
                                        })
                                        .setPositiveButton("Yes", (dialogInterface, i) -> {

                                            Map<String, Object> update_data = new HashMap<>();
                                            update_data.put("orderStatus", -1);//cancel Order
                                            FirebaseDatabase.getInstance()
                                                    .getReference(Common.ORDER_REF)
                                                    .child(orderModel.getOrderNumber())
                                                    .updateChildren(update_data)
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnSuccessListener(aVoid -> {
                                                        orderModel.setOrderStatus(-1); //Local Update
                                                        ((MyOrdersAdapter)recycler_orders.getAdapter()).setItemAtPosition(pos,orderModel);
                                                        recycler_orders.getAdapter().notifyItemChanged(pos);
                                                        Toast.makeText(getContext(), "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                                androidx.appcompat.app.AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            else {
                                Toast.makeText(getContext(), new StringBuilder("Your Order Was changed to ")
                                        .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                        .append(" so you can't cancel it"), Toast.LENGTH_SHORT).show();
                            }
                        }));
            }
        };
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderList) {

        dialog.dismiss();
        viewOrdersViewModel.setMutableLiveDataOrderlist(orderList);

    }

    @Override
    public void onOrderLoadFailed(String message) {

        dialog.dismiss();
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();

    }
}