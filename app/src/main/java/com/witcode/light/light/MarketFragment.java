package com.witcode.light.light;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import backend.GetMarketItems;
import backend.GetRankings;
import backend.OnTaskCompletedListener;


public class MarketFragment extends Fragment{
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MarketItemAdapter mAdapter;
    private List<MarketItem> mMarkets;
    private View myView;

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_market, container, false);

        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);

        myToolbar.setTitle("Market");
        myToolbar.setTitleTextColor(Color.parseColor("#ffffff"));

        DrawerLayout mDrawerLayout = ((MainActivity) getActivity()).getDrawerLayout();

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(), mDrawerLayout, myToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            public void onDrawerOpened(View drawerView) {

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);


        recyclerView=(RecyclerView) myView.findViewById(R.id.rv_market);

        new GetMarketItems(new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode) {
                Log.d("tagg", result);
                try {
                    JSONArray jsonMarkets= new JSONArray(result);
                    JSONObject jsonMarket;
                    mMarkets=new ArrayList<MarketItem>();
                    MarketItem marketItem;
                    for(int i=0;i<jsonMarkets.length();i++) {
                        jsonMarket = jsonMarkets.getJSONObject(i);
                        marketItem = new MarketItem();
                        marketItem.setId(jsonMarket.getString("id"));
                        marketItem.setName(jsonMarket.getString("name"));
                        marketItem.setInfo(jsonMarket.getString("info"));
                        marketItem.setLights(jsonMarket.getString("lights"));
                        marketItem.setDiscount(jsonMarket.getString("discount"));
                        marketItem.setType(jsonMarket.getString("categoria"));
                        mMarkets.add(marketItem);
                    }

                    mAdapter = new MarketItemAdapter(mMarkets, getActivity());
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(mAdapter);


                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).execute();

        return myView;
    }
}
