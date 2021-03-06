package com.witcode.light.light.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.witcode.light.light.R;
import com.witcode.light.light.Services.ActivityService;
import com.witcode.light.light.backend.AddAnalytics;
import com.witcode.light.light.backend.EndActivityTask;
import com.witcode.light.light.backend.NetworkChangeReceiver;
import com.witcode.light.light.backend.UpdateToken;
import com.witcode.light.light.backend.UpdateUserData;
import com.witcode.light.light.domain.ActivityObject;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.domain.MarketItem;
import com.witcode.light.light.fragments.ActivityFragment;
import com.witcode.light.light.fragments.AdminFragment;
import com.witcode.light.light.fragments.EndActivityFragment;
import com.witcode.light.light.fragments.HomeFragment;
import com.witcode.light.light.fragments.MarketDetailFragment;
import com.witcode.light.light.fragments.MarketFragment;
import com.witcode.light.light.fragments.RankingFragment;

import com.witcode.light.light.backend.GetAdminLevel;
import com.witcode.light.light.backend.GetLights;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.OnLocationUpdateListener;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.Services.ServiceBinder;
import com.witcode.light.light.backend.UpdateLights;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private FragmentTransaction fragmentTransaction;
    private TextView tvLights;
    private int MY_PERMISSIONS_REQUEST_CODE = 1;
    public final static int HIGH_ACCURACY = 1;
    public final static int REQUEST_CHECK_SETTINGS = 2;
    private Context mContext = this;
    private int seconds, minutes, hours;
    private MaterialDialog mDialog, dialogWalk;
    private static Handler mHandler;
    private double mSpeed, mDistance, mLights;
    private View header;
    public boolean serviceBound = false;
    private String mCurrentFragment = "";
    private ServiceBinder mServiceBinder=null;
    private ActivityService mActivityService = null;
    private TextView tvTime, tvDistance, tvSpeed, tvGPS, tvDialogLights;
    private int i;
    private long mTime;
    private long timeIn;
    private ServiceConnection mActivityServiceConnection;

    public final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 4532;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                if (Profile.getCurrentProfile() != null & FirebaseAuth.getInstance().getCurrentUser() != null) {


                    new GetLights(mContext, GetLights.SUMMED_LIGHTS, new OnTaskCompletedListener() {
                        @Override
                        public void OnComplete(String result, int resultCode, int resultType) {
                            if (resultCode == GetLights.SUCCESSFUL) {
                                header.findViewById(R.id.ll_connected).setVisibility(View.VISIBLE);
                                header.findViewById(R.id.ll_not_connected).setVisibility(View.GONE);
                                tvLights.setText(result);
                            } else if (resultCode == MyServerClass.NOT_CONNECTED) {
                                header.findViewById(R.id.ll_connected).setVisibility(View.GONE);
                                header.findViewById(R.id.ll_not_connected).setVisibility(View.VISIBLE);
                            }
                        }
                    }).execute();

                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {


            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }

        header = navigationView.getHeaderView(0);
        tvLights = (TextView) header.findViewById(R.id.tv_nav_lights);

        CircleImageView civ = (CircleImageView) header.findViewById(R.id.civ_profile);
        Uri uri = Profile.getCurrentProfile().getProfilePictureUri(640, 640);
        Log.d("tagg", uri.toString());

        Picasso.with(this).load(uri).into(civ, new Callback() {
            @Override
            public void onSuccess() {
                ((TextView) header.findViewById(R.id.tv_nav_name)).setText(Profile.getCurrentProfile().getName());
            }

            @Override
            public void onError() {

            }
        });

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToFragment("home");
            }
        });


        //If admin then show new tab
        new GetAdminLevel(mContext, new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {
                if (resultCode == MyServerClass.SUCCESSFUL && result != null && !result.isEmpty() && Double.parseDouble(result) >= 90) {
                    navigationView.getMenu().findItem(R.id.nav_admin).setVisible(true);
                }
            }
        }).execute();


        if (getIntent() != null && getIntent().getAction() != null) {

            switch (getIntent().getAction()) {
                case "too_fast":
                    Log.v("tagg", "intent too_fast");
                    GotoStartActivityFragment();
                    break;
                case "start_activity":
                    GoToFragment("start");
                    break;
                case "ranking":
                    GoToFragment("ranking");
                    break;
                case "market":
                    GoToFragment("market");
                    break;
                case "started_activity":
                    GotoStartActivityFragment();
                    Log.v("tagg", "intent_started_activity");
                    break;
                default:
                    GoToFragment("home");
                    break;
            }
        } else {
            GoToFragment("start");
        }


        if (FirebaseInstanceId.getInstance().getToken() != null) {
            new UpdateToken(mContext, FirebaseInstanceId.getInstance().getToken(), new OnTaskCompletedListener() {
                @Override
                public void OnComplete(String result, int resultCode, int resultType) {

                }
            }).execute();
        }

        Log.v("mytag","starting activity fragment");
        GotoStartActivityFragment();


    }

    public DrawerLayout getDrawerLayout() {
        return drawer;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_start) {
            GotoStartActivityFragment();
        } else if (id == R.id.nav_market) {
            GoToFragment("market");
        } else if (id == R.id.nav_ranking) {
            GoToFragment("ranking");
        } else if (id == R.id.nav_admin) {
            GoToFragment("admin");
        } else if (id == R.id.nav_logout) {

            logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return true;
    }

    public void GoToFragment(String fragment, Bundle extras) {

        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
        switch (fragment) {

            case "home":
                HomeFragment mFragment = new HomeFragment();
                mFragment.setArguments(extras);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack("home");
                fragmentTransaction.replace(R.id.container, mFragment);
                fragmentTransaction.commit();
                mCurrentFragment = "home";
                break;
            case "ranking":
                RankingFragment mRankingFragment = new RankingFragment();
                mRankingFragment.setArguments(extras);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack("ranking");
                fragmentTransaction.replace(R.id.container, mRankingFragment);
                fragmentTransaction.commit();
                mCurrentFragment = "ranking";
                break;

            case "market":
                MarketFragment mMarketFragment = new MarketFragment();
                mMarketFragment.setArguments(extras);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack("market");
                fragmentTransaction.replace(R.id.container, mMarketFragment);
                fragmentTransaction.commit();
                mCurrentFragment = "market";
                break;

            case "admin":
                AdminFragment mAdminFragment = new AdminFragment();
                mAdminFragment.setArguments(extras);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack("admin");
                fragmentTransaction.replace(R.id.container, mAdminFragment);
                fragmentTransaction.commit();
                mCurrentFragment = "admin";
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    public void GoToFragment(String fragment) {
        GoToFragment(fragment, new Bundle());
    }

    public void GoToMarketDetail(MarketItem marketItem) {
        MarketDetailFragment mMarketFragment = MarketDetailFragment.getInstance(marketItem);
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack("market_detail");
        fragmentTransaction.replace(R.id.container, mMarketFragment);
        fragmentTransaction.commit();
        mCurrentFragment = "market_detail";

    }

    public void GotoStartActivityFragment() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Log.v("tagg", "gotostartactivityfragment");
            ActivityFragment mStartFragment = new ActivityFragment();
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack("start");
            fragmentTransaction.replace(R.id.container, mStartFragment);
            fragmentTransaction.commitAllowingStateLoss();
            mCurrentFragment = "activity";
            mStartFragment.setmActivity(this);
        }
    }


    public EndActivityFragment GoToEndActivityFragment(ActivityObject activityObject, String endText) {
        EndActivityFragment mFragment =EndActivityFragment.getInstance(activityObject, endText);
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack("end_activity");
        fragmentTransaction.replace(R.id.container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
        mCurrentFragment = "end_activity";

        return mFragment;
    }
    public void StartActivityService(final ActivityObject activityObject) {
        mActivityServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {

                mActivityService = ((ActivityService.LocalBinder) service).getService();
                mActivityService.mActivity = (MainActivity) mContext;
                ActivityService.ActivityObject = activityObject;
                if(mServiceBinder!=null){
                    mActivityService.setBinder(mServiceBinder);
                }

                serviceBound = true;
                Log.i("ForegroundService", "service_connected");
                ActivityService.ActivityObject.setRunning(true);
            }

            public void onServiceDisconnected(ComponentName className) {
                mActivityService = null;
            }
        };
        bindService(new Intent(MainActivity.this,
                ActivityService.class), mActivityServiceConnection, Context.BIND_AUTO_CREATE);
        Log.v("ForegroundService", "service bound");
    }

    public void setServiceBinder(ServiceBinder binder) {
        Log.v("mytag", "in setservicebinder method");
        if (mActivityService != null) {
            Log.v("mytag", "setting binder");
            mActivityService.setBinder(binder);
        }else{
            mServiceBinder=binder;
        }
    }

    public void StopActivityService() {
        if (mActivityService != null && ActivityService.ActivityObject.isRunning()) {
            try {
                unbindService(mActivityServiceConnection);
                ActivityService.ActivityObject.setRunning(false);
                Log.v("tagg", "service unbound");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.v("tagg", "caught unbind exception");
            }
        } else {
            Log.v("tagg", "service was null or cte was not true");
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        if (ActivityService.ActivityObject.isRunning()) {
            StopActivityService();
        }


        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v("tagg", "new intent: " + intent);
        if (intent != null && intent.getAction() != null) {
            if (!mCurrentFragment.equals("activity") && intent.getAction().equals("started_activity")) {
                GotoStartActivityFragment();
            } else if (intent.getAction().equals("action_stop")) {
                final EndActivityFragment mFragment = GoToEndActivityFragment(ActivityService.ActivityObject,"HAS TERMINADO LA ACTIVIDAD");
                mActivityService.setBinder(null);
                StopActivityService();

            } else if (intent.getAction().equals("too_fast")) {
                final EndActivityFragment mFragment = GoToEndActivityFragment(ActivityService.ActivityObject, "VAS DEMASIADO RÁPIDO");

                mActivityService.setBinder(null);
                StopActivityService();

            }

        }
        super.onNewIntent(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                    GotoStartActivityFragment();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void logOut(){
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }
    @Override
    protected void onResume() {

        (new UpdateUserData(this, new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {

            }
        })).execute();

        (new AddAnalytics(this, "activity_resume","null","null","null", new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {

            }
        })).execute();

        timeIn=System.currentTimeMillis();

        super.onResume();

    }

    @Override
    protected void onPause() {
        //Analytics: paso como valor el tiempo desde resume hasta pause
        (new AddAnalytics(this, "activity_pause",String.valueOf((System.currentTimeMillis()-timeIn)/1000),"null","null", new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {

            }
        })).execute();
        super.onPause();
    }
}