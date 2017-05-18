package com.witcode.light.light;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Profile;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    private android.support.v7.widget.Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Context mContext;
    private View myView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = getActivity();
        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);

        myToolbar.setTitle("Home");
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

        CircleImageView civ = (CircleImageView) myView.findViewById(R.id.civ_profile);
        Uri uri = Profile.getCurrentProfile().getProfilePictureUri(640, 640);
        Log.d("tagg", uri.toString());
        Picasso.with(mContext).load(uri).into(civ, new Callback() {
            @Override
            public void onSuccess() {
                ((TextView) myView.findViewById(R.id.tv_home_name)).setText(Profile.getCurrentProfile().getName());
            }

            @Override
            public void onError() {

            }
        });
        View fabLighter;
        fabLighter = myView.findViewById(R.id.fab_lighter);
        /*if (WalkService.IS_SERVICE_RUNNING)
            fabLighter.setVisibility(View.GONE);
        else
            fabLighter.setVisibility(View.VISIBLE);*/

        fabLighter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).GoToFragment("start");
            }
        });
        return myView;
    }
}
