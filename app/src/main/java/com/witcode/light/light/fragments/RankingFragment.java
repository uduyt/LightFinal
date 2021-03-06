package com.witcode.light.light.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.R;
import com.witcode.light.light.domain.RankingAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.witcode.light.light.backend.GetRankings;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.OnTaskCompletedListener;


public class RankingFragment extends Fragment{
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private RankingAdapter mAdapter;
    private ProgressBar progressBar;
    private View myView;
    private List<Bundle> mRankings;

    public RankingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_ranking, container, false);

        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);

        myToolbar.setTitle("Ranking");
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

        recyclerView=(RecyclerView) myView.findViewById(R.id.rv_ranking);

        new GetRankings(getActivity(),new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {

                if(resultCode== MyServerClass.SUCCESSFUL){
                    try {
                        JSONArray jsonRankings= new JSONArray(result);
                        JSONObject jsonRanking;
                        mRankings=new ArrayList<Bundle>();
                        Bundle ranking;
                        for(int i=0;i<jsonRankings.length();i++) {
                            jsonRanking = jsonRankings.getJSONObject(i);
                            ranking = new Bundle();
                            ranking.putString("uid", jsonRanking.getString("facebook_id"));
                            ranking.putString("name", jsonRanking.getString("name"));
                            ranking.putString("lights", jsonRanking.getString("lights"));
                            mRankings.add(ranking);
                        }

                        mAdapter = new RankingAdapter(mRankings, getActivity());
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(mAdapter);


                        mAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    myView.findViewById(R.id.ll_market_not_connected).setVisibility(View.GONE);
                    myView.findViewById(R.id.rv_ranking).setVisibility(View.VISIBLE);
                }else if(resultCode== MyServerClass.NOT_CONNECTED){
                    myView.findViewById(R.id.ll_market_not_connected).setVisibility(View.VISIBLE);
                    myView.findViewById(R.id.rv_ranking).setVisibility(View.GONE);
                }


            }
        }).execute();

        return myView;
    }
}
