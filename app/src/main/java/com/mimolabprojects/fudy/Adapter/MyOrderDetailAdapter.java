package com.mimolabprojects.fudy.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mimolabprojects.fudy.Database.CartItem;
import com.mimolabprojects.fudy.Model.AddonModel;
import com.mimolabprojects.fudy.Model.SizeModel;
import com.mimolabprojects.fudy.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.MyViewholder> {


    private Context context;
    private List<CartItem> cartItemList;
    Gson gson;

    public MyOrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewholder(LayoutInflater.from(context).inflate(R.layout.layout_order_detail_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewholder holder, int position) {

        Glide.with(context).load(cartItemList.get(position).getFoodImage())
                .into(holder.detail_food_img);
        holder.detail_food_name.setText(new StringBuilder("").append(cartItemList.get(position).getFoodName()));
        holder.detail_food_quantity.setText(new StringBuilder("Quantity: ").append(cartItemList.get(position).getFoodQuantity()));

        SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getFoodSize(),
                new TypeToken<SizeModel>(){}.getType());
        if (sizeModel !=null)
            holder.detail_food_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
        if (!cartItemList.get(position).getFoodAddon().equals("Default"))
        {
            List<AddonModel> addonModels = gson.fromJson(cartItemList.get(position).getFoodAddon(),
                    new TypeToken<List<AddonModel>>(){}.getType());
            StringBuilder addonString = new StringBuilder();

            if (addonModels !=null){
                for (AddonModel addonModel: addonModels)
                    addonString.append(addonModel.getName()).append(",");
                addonString.delete(addonString.length()-1, addonString.length());
                holder.detail_food_addon.setText(new StringBuilder("Addon: ").append(addonString));

            }
        }
        else {
            holder.detail_food_addon.setText(new StringBuilder("AddOn: Default"));
        }
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class MyViewholder extends RecyclerView.ViewHolder{

        @BindView(R.id.detail_food_img)
        ImageView detail_food_img;

        @BindView(R.id.detail_food_name)
        TextView detail_food_name;
        @BindView(R.id.detail_food_quantity)
        TextView detail_food_quantity;
        @BindView(R.id.detail_food_addon)
        TextView detail_food_addon;
        @BindView(R.id.detail_food_size)
        TextView detail_food_size;


        Unbinder unbinder;
        public MyViewholder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
