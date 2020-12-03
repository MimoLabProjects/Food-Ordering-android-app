package com.mimolabprojects.fudy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.mimolabprojects.fudy.Adapter.MyBestDealsAdapter;
import com.mimolabprojects.fudy.Adapter.MyPopularCategoryAdapter;
import com.mimolabprojects.fudy.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_popular)
    RecyclerView recycler_popular;
    @BindView(R.id.viewpager)
    LoopingViewPager viewpager;

    LayoutAnimationController layoutAnimationController;

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        unbinder = ButterKnife.bind(this,root);
        init();
        homeViewModel.getPopularList().observe(this, popularCategoryModels -> {

            //Creating Popular List Adapter

            MyPopularCategoryAdapter adapter = new MyPopularCategoryAdapter(getContext(),popularCategoryModels);
            recycler_popular.setAdapter(adapter);
            recycler_popular.setLayoutAnimation(layoutAnimationController);

        });

        //--------------------------- BEST DEALS MODEL-------------------------------------------------------------------
        homeViewModel.getBestDealList().observe(this, bestDealsModels -> {

            MyBestDealsAdapter badapter = new MyBestDealsAdapter(getContext(),bestDealsModels, true);
            viewpager.setAdapter(badapter);

        });

        return root;
    }

    private void init() {

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        recycler_popular.setHasFixedSize(true);
        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));
    }

    //Best Deals
    @Override
    public void onResume() {
        super.onResume();
        viewpager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        viewpager.pauseAutoScroll();
        super.onPause();
    }
}