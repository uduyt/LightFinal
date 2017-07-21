package com.witcode.light.light.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.DismissNotification;
import com.witcode.light.light.backend.GetNotifications;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.domain.Notification;
import com.witcode.light.light.domain.NotificationsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {
    private Toolbar myToolbar;
    private RecyclerView recyclerView;
    private Context mContext;
    private ArrayList<Notification> mNotifications;
    private NotificationsAdapter mAdapter;
    private View myView, vEndOfFeed;
    private int mLimit=0;
    private LinearLayoutManager mLayoutManager;
    private boolean mIsLoading=false;
    private OnTaskCompletedListener onCompleteListener;
    private ProgressBar pbNotifications;
    private NestedScrollView nsvNotifications;
    private ProfileFragment mFragment=this;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_profile, container, false);

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

        //TODO shared preferences to see if notification card has been dismissed, if so, it should not appear

        pbNotifications= (ProgressBar) myView.findViewById(R.id.pb_notifications);
        recyclerView = (RecyclerView) myView.findViewById(R.id.rv_notifications);
        vEndOfFeed=myView.findViewById(R.id.ll_end_of_feed);
        nsvNotifications=(NestedScrollView) myView.findViewById(R.id.nsv_notifications);
        mLayoutManager = new LinearLayoutManager(getActivity());


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



        fabLighter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).GoToFragment("start");
            }
        });
        return myView;
    }


}