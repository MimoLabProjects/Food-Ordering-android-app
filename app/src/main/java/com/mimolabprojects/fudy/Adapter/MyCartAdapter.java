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
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Database.CartItem;
import com.mimolabprojects.fudy.EventBus.UpdateItemInCart;
import com.mimolabprojects.fudy.Model.AddonModel;
import com.mimolabprojects.fudy.Model.SizeModel;
import com.mimolabprojects.fudy.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {

   Context context;
   private List<CartItem> cartItemList;
   Gson gson;

    public MyCartAdapter(Context context, List<CartItem> cartItemList) {

        this.context = context;
        this.cartItemList = cartItemList;
        this.gson = new Gson();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_cart_item,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(cartItemList.get(position).getFoodImage()).into(holder.img_carts);
        holder.txt_food_names.setText(new StringBuilder(cartItemList.get(position).getFoodName()));
        holder.txt_food_prices.setText(new StringBuilder("Ksh ").append(cartItemList.get(position)
                .getFoodPrice() + cartItemList.get(position).getFoodExtraPrice()));


//==========================Size Display on Cart Layout==============================================
        if (cartItemList.get(position).getFoodSize() !=null)
        {
            if (cartItemList.get(position).getFoodSize().equals("Default"))
                holder.txt_food_sizes.setText(new StringBuilder("Size: ").append("Default"));
            else {
                SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getFoodSize(), new TypeToken<SizeModel>()
                {}.getType());
                holder.txt_food_sizes.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
            }
        }
 //========================== Addon Display on Cart Layout==============================================
        if (cartItemList.get(position).getFoodAddon() !=null)
        {
            if (cartItemList.get(position).getFoodAddon().equals("Default"))
                holder.txt_food_addons.setText(new StringBuilder("AddOn: ").append("Default"));
            else {
                List<AddonModel> addonModels = gson.fromJson(cartItemList.get(position).getFoodAddon(), new TypeToken<AddonModel>()
                {}.getType());
                holder.txt_food_addons.setText(new StringBuilder("AddOn: ").append(Common.getListAddon(addonModels)));
            }
        }
        holder.number_button.setNumber(String.valueOf(cartItemList.get(position).getFoodQuantity()));

        //==========================EVENT ON ELG BUTTON CLICK==============================================

    holder.number_button.setOnValueChangeListener((view, oldValue, newValue) -> {

        cartItemList.get(position).setFoodQuantity(newValue);
        EventBus.getDefault().postSticky(new UpdateItemInCart(cartItemList.get(position)));
    });
    }

    @Override
    public int getItemCount() {

        return  cartItemList.size();
    }

    public CartItem getItemPosition(int pos) {

        return cartItemList.get(pos);
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder {

        private Unbinder unbinder;

        @BindView(R.id.img_carts)
        ImageView img_carts;
        @BindView(R.id.txt_food_prices)
        TextView txt_food_prices;
        @BindView(R.id.txt_food_names)
        TextView txt_food_names;
        @BindView(R.id.txt_food_sizes)
        TextView txt_food_sizes;
        @BindView(R.id.txt_food_addons)
        TextView txt_food_addons;
        @BindView(R.id.number_buttons)
        ElegantNumberButton number_button;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
