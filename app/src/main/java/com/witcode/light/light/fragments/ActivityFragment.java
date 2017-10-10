package com.witcode.light.light.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.maps.android.PolyUtil;
import com.witcode.light.light.R;
import com.witcode.light.light.Services.ActivityService;
import com.witcode.light.light.Services.ServiceBinder;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.CheckIfLineExists;
import com.witcode.light.light.backend.CheckIfLineExistsListener;
import com.witcode.light.light.backend.ExceptionHandler;
import com.witcode.light.light.backend.GetCitiesInfo;
import com.witcode.light.light.backend.GetInitialActivityPoints;
import com.witcode.light.light.backend.GetNearRoutePoints;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.NetworkChangeReceiver;
import com.witcode.light.light.backend.OnCityInfoCompleted;
import com.witcode.light.light.backend.OnConnectivityChangeListener;
import com.witcode.light.light.backend.OnInitialPointsCompleteListener;
import com.witcode.light.light.backend.OnLocationUpdateListener;
import com.witcode.light.light.backend.OnPointsCompleteListener;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.UpdateLights;
import com.witcode.light.light.backend.UpdateToken;
import com.witcode.light.light.backend.ValidateInterurbanBus;
import com.witcode.light.light.backend.ValidateUrbanBus;
import com.witcode.light.light.domain.ActivityObject;
import com.witcode.light.light.domain.City;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.domain.MyClusterManager;
import com.witcode.light.light.domain.ResizeAnimation;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ActivityFragment extends Fragment {
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SupportMapFragment mSupportMapFragment;
    private View myView;
    private GoogleMap mGoogleMap;
    private MaterialDialog walkConfirmDialog, bikeConfirmDialog, busConfirmDialog, mMetroDialog;
    private FrameLayout flNoActivity, flActivity, flBottom, flMap;
    private ServiceBinder mServiceBinder;
    private ArrayList<Polyline> userRoutePolylines, transportRoutePolylines;
    private TextView tvDistance, tvSpeed, tvTime, tvLights, tvGPS;
    private FrameLayout fabStop;
    private ArrayList<Marker> initialMarkers;
    private String GPSUpdate = "";
    private String mDistance = "0m", mSpeed = "0km/h", mTime = "00:00:00", mLights = "0", mEndText = "HAS TERMINADO LA ACTIVIDAD";
    private Timer walkTimer;
    private ArrayList<MapPoint> mInitialPoints, mUserRoutePoints;
    private ArrayList<ArrayList<MapPoint>> mLineRoutePoints;
    private AutoCompleteTextView mMetroAutoComplete;
    private boolean activityFragmentRunning = false;
    private NetworkChangeReceiver mrv;
    private GetNearRoutePoints routeTask;
    private MainActivity mActivity;
    private MapPoint lastPoint = null;
    private MapPoint mapPoint;
    private Polyline polyline;
    private ArrayList<City> mCities=new ArrayList<>();
    public static City currentCity;
    private int color;
    private boolean stop = false;
    private MyClusterManager<MapPoint> mClusterManager;
    private ActivityObject mActObject = new ActivityObject();
    private View fabWalk,fabBike,fabBus,fabRailRoad,fabCarshare,fabRecycle;


    public static String LINE;
    public static boolean IS_URBAN;

    public final static int ACTIVITY_NONE = 0;
    public final static int ACTIVITY_WALK = 1;
    public final static int ACTIVITY_BIKE = 2;
    public final static int ACTIVITY_BUS = 3;
    public final static int ACTIVITY_RAILROAD = 4;
    public final static int ACTIVITY_CARSHARE = 5;
    public final static int ACTIVITY_RECYCLE = 6;


    public ActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_activity, container, false);


        //initiate arraylists
        mInitialPoints = new ArrayList<>();
        mUserRoutePoints = new ArrayList<>();
        mLineRoutePoints = new ArrayList<>();
        userRoutePolylines = new ArrayList<>();
        transportRoutePolylines = new ArrayList<>();
        initialMarkers = new ArrayList<>();

        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(myToolbar);

        myToolbar.setTitle("Comenzar actividad");
        myToolbar.setTitleTextColor(Color.parseColor("#ffffff"));

        DrawerLayout mDrawerLayout = mActivity.getDrawerLayout();

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

        flNoActivity = (FrameLayout) myView.findViewById(R.id.fl_no_activity);
        flActivity = (FrameLayout) myView.findViewById(R.id.fl_activity);
        flBottom = (FrameLayout) myView.findViewById(R.id.fl_bottom);
        flMap = (FrameLayout) myView.findViewById(R.id.map);
        fabStop = (FrameLayout) myView.findViewById(R.id.fl_fab_stop);

        fabWalk=myView.findViewById(R.id.fab_walk);
        fabBike=myView.findViewById(R.id.fab_bike);
        fabBus=myView.findViewById(R.id.fab_bus);
        fabRailRoad=myView.findViewById(R.id.fab_railroad);
        fabCarshare=myView.findViewById(R.id.fab_carshare);
        fabRecycle=myView.findViewById(R.id.fab_recycle);

        fabBus.setVisibility(View.GONE);
        fabRailRoad.setVisibility(View.GONE);

        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StopActivity();
            }
        });

        SetClickers();

        tvDistance = (TextView) myView.findViewById(R.id.tv_distance);
        tvSpeed = (TextView) myView.findViewById(R.id.tv_speed);
        tvLights = (TextView) myView.findViewById(R.id.tv_lights);
        tvTime = (TextView) myView.findViewById(R.id.tv_time);
        tvGPS = (TextView) myView.findViewById(R.id.tv_gps);

        InitiateMap();

        mServiceBinder = new ServiceBinder() {
            @Override
            public void OnNewMapPoint(double distance, double speed, final PolylineOptions polylineOptions) {

                mDistance = String.valueOf(Math.round(distance * 100.0) / 100.0) + "m";
                mSpeed = String.valueOf(Math.round(speed * 100.0) / 100.0) + "km/h";
                mLights=String.format(Locale.ENGLISH,"%.2f", ActivityService.ActivityObject.getLights());
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvDistance.setText(mDistance);
                            tvSpeed.setText(mSpeed);
                            Log.v("mytag", "adding polyline with options: " + polylineOptions.getPoints().toString() );
                            userRoutePolylines.add(mGoogleMap.addPolyline(polylineOptions));
                            tvLights.setText(mLights);
                        }
                    });
                }
            }

            @Override
            public void OnGPSUpdate(String GPS) {
                GPSUpdate = GPS;

                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvGPS.setText(GPSUpdate);
                        }
                    });
                }
            }

            @Override
            public void OnPolylineUpdate(final int index, final int color) {

                mLights=String.format(Locale.ENGLISH,"%.2f", ActivityService.ActivityObject.getLights());
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userRoutePolylines.get(index).setColor(color);
                            tvLights.setText(mLights);
                        }
                    });
                }
            }

            @Override
            public void OnInitialPointsArrived(final ArrayList<PolylineOptions> polylineOptions) {
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(PolylineOptions pOptions:polylineOptions){
                                mGoogleMap.addPolyline(pOptions);
                            }
                        }
                    });
                }
            }
        };


        return myView;
    }


    public void StartWalk() {
        walkConfirmDialog = new MaterialDialog.Builder(mActivity)
                .title("¿Quieres empezar la actividad?")
                .content("Pulsa comenzar para empezar la actividad")
                .positiveText("Comenzar")
                .contentColor(Color.parseColor("#ffffff"))
                .titleColor(Color.parseColor("#ffffff"))
                .backgroundColor(mActivity.getResources().getColor(R.color.PrimaryDark))
                .negativeText("Cancelar")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Snackbar.make(myView, "Se ha cancelado la acción", Snackbar.LENGTH_SHORT);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        walkConfirmDialog.dismiss();
                        mActObject.setType(ACTIVITY_WALK);
                        flNoActivity.animate()
                                .translationY(-flNoActivity.getHeight())
                                .alpha(0.0f)
                                .setStartDelay(300)
                                .setDuration(1000)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        StartActivity();
                                    }
                                });
                    }
                }).build();

        walkConfirmDialog.show();


    }

    public void StartBike() {
        bikeConfirmDialog = new MaterialDialog.Builder(getActivity())
                .title("¿Quieres empezar la actividad?")
                .content("Pulsa comenzar para empezar la actividad")
                .positiveText("Comenzar")
                .contentColor(Color.parseColor("#ffffff"))
                .titleColor(Color.parseColor("#ffffff"))
                .backgroundColor(getActivity().getResources().getColor(R.color.PrimaryDark))
                .negativeText("Cancelar")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Snackbar.make(myView, "Se ha cancelado la acción", Snackbar.LENGTH_SHORT);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        bikeConfirmDialog.dismiss();
                        mActObject.setType(ACTIVITY_BIKE);
                        flNoActivity.animate()
                                .translationY(-flNoActivity.getHeight())
                                .alpha(0.0f)
                                .setDuration(600)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        flNoActivity.setVisibility(View.GONE);
                                        StartActivity();
                                    }
                                });
                    }
                }).build();

        bikeConfirmDialog.show();


    }

    public void StartBus() {
        busConfirmDialog = new MaterialDialog.Builder(getActivity())
                .title("Autobús")
                .titleColor(Color.parseColor("#ffffff"))
                .positiveText("Comenzar")
                .negativeText("Cancelar")
                .backgroundColor(getActivity().getResources().getColor(R.color.PrimaryDark))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        busConfirmDialog.dismiss();
                        Snackbar.make(myView, "Se ha cancelado la acción", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .customView(R.layout.dialog_bus, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        busConfirmDialog.dismiss();
                        final MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                                .title("Por favor, espere")
                                .content("Estamos validando su acción")
                                .progress(true, 0)
                                .build();
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        (new CheckIfLineExists(getActivity(), ((EditText) busConfirmDialog.findViewById(R.id.et_dialog_line)).getText().toString(), new CheckIfLineExistsListener() {

                            @Override
                            public void onComplete(boolean exists, boolean isUrban) {
                                progressDialog.dismiss();
                                if (exists) {
                                    //the line exists
                                    mActObject.setType(ACTIVITY_BUS);

                                    mActObject.setLine(((EditText) busConfirmDialog.findViewById(R.id.et_dialog_line)).getText().toString());
                                    mActObject.setCercaniasOrUrban(isUrban);

                                    flNoActivity.animate()
                                            .translationY(-flNoActivity.getHeight())
                                            .alpha(0.0f)
                                            .setDuration(600)
                                            .setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                    flNoActivity.setVisibility(View.GONE);
                                                    StartActivity();
                                                }
                                            });
                                } else {
                                    //the line doesnt exist
                                    Snackbar.make(myView, "La línea especificada no ha sido encontrada...", Snackbar.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void OnError(String result, int resultCode, int resultType) {
                                progressDialog.dismiss();
                                if (resultCode == MyServerClass.NOT_CONNECTED) {
                                    Snackbar.make(myView, "No se ha podido conectar a internet...", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        })).execute();
                    }
                })
                .build();

        busConfirmDialog.setCanceledOnTouchOutside(false);


        busConfirmDialog.show();
    }

    public void StartRailroad() {
        mMetroDialog = new MaterialDialog.Builder(getActivity())
                .title("Metro o Cercanias")
                .titleColor(Color.parseColor("#ffffff"))
                .positiveText("Comenzar")
                .negativeText("Cancelar")
                .backgroundColor(getActivity().getResources().getColor(R.color.PrimaryDark))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mMetroDialog.dismiss();
                        Snackbar.make(myView, "Se ha cancelado la acción", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .customView(R.layout.dialog_metro, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        mMetroDialog.dismiss();
                        mActObject.setType(ACTIVITY_RAILROAD);
                        mActObject.setLine(mMetroAutoComplete.getText().toString());

                        if ( mActObject.getLine().contains("Linea")) {
                            mActObject.setCercaniasOrUrban(false);
                            mActObject.setLine(mActObject.getLine().substring(6));
                        } else {
                            mActObject.setCercaniasOrUrban(true);
                            mActObject.setLine(mActObject.getLine().substring(11));
                        }

                        flNoActivity.animate()
                                .translationY(-flNoActivity.getHeight())
                                .alpha(0.0f)
                                .setDuration(600)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        flNoActivity.setVisibility(View.GONE);
                                        StartActivity();
                                    }
                                });

                    }
                })
                .build();

        mMetroDialog.setCanceledOnTouchOutside(false);

        final ArrayAdapter cercaniasAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cercanias_array, android.R.layout.simple_list_item_1);


        mMetroAutoComplete = (AutoCompleteTextView) mMetroDialog.getCustomView().findViewById(R.id.actv_metro);

        mMetroAutoComplete.setAdapter(cercaniasAdapter);
        mMetroAutoComplete.setThreshold(1);

        mMetroDialog.show();
    }

    public void StartActivity() {
        mActObject.InitiateMillis();
        mActivity.StartActivityService(mActObject);
        mClusterManager.setDraw(false);
        mClusterManager.removeAll();
        ResumeActivity();
    }

    public void ResumeActivity() {
        mActivity.setServiceBinder(mServiceBinder);
        flActivity.setTranslationY(flActivity.getHeight());
        flActivity.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        //igual algo
                        Log.v("tagg", "animation ended");

                    }
                });

        ResizeAnimation anim = new ResizeAnimation(flBottom, flBottom.getHeight() + 50, flBottom.getHeight());
        anim.setDuration(800);
        flBottom.startAnimation(anim);

        anim = new ResizeAnimation(flMap, flMap.getHeight() - flBottom.getHeight() - 50, flMap.getHeight(), false);
        anim.setDuration(800);
        flMap.startAnimation(anim);

        fabStop.setVisibility(View.VISIBLE);
        fabStop.setTranslationY(flActivity.getHeight());
        fabStop.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });


        walkTimer = new Timer();
        walkTimer.scheduleAtFixedRate(new ActivityFragment.MyTimerTask(), 0, 1000);

        //Stuff for onResume only
    }

    public void StopActivity() {
        mActObject=new ActivityObject();
        activityFragmentRunning = false;
        walkTimer.cancel();

        flActivity.setTranslationY(flActivity.getHeight());
        flActivity.animate()
                .translationY(flActivity.getHeight())
                .alpha(0f)
                .setDuration(800);
        ResizeAnimation anim = new ResizeAnimation(flBottom, flBottom.getHeight() / 2 - 25, flBottom.getHeight(), false);
        anim.setDuration(800);
        flBottom.startAnimation(anim);

        anim = new ResizeAnimation(flMap, flBottom.getHeight() / 2 + 25, flMap.getHeight());
        anim.setDuration(800);
        flMap.startAnimation(anim);

        fabStop.animate()
                .translationY(flActivity.getHeight())
                .alpha(0f)
                .setDuration(800)

                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        fabStop.setVisibility(View.GONE);
                        ActivityObject activityObject= ActivityService.ActivityObject;
                        mActivity.GoToEndActivityFragment(activityObject,"HAS TERMINADO LA ACTIVIDAD");
                        mActivity.StopActivityService();
                    }
                });

        flNoActivity.setVisibility(View.VISIBLE);
        flNoActivity.setAlpha(0f);
        flNoActivity.setTranslationY(-flNoActivity.getHeight());
        flNoActivity.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });


    }

    @Override
    public void onAttach(Context context) {
        mActivity = (MainActivity) getActivity();
        super.onAttach(context);
    }

    public void setmActivity(MainActivity mActivity) {
        this.mActivity = mActivity;
    }

    private void SetClickers() {
        fabWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartWalk();
            }
        });

       fabBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartBike();
            }
        });

        fabBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartBus();
            }
        });

        fabRailRoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartRailroad();
            }
        });

       fabCarshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                //StartCarshare();
            }
        });

        fabRecycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                //StartRecycleSync();
            }
        });
    }

    private void InitiateMap() {
        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mSupportMapFragment).commit();
        }

        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (googleMap != null) {

                        mGoogleMap = googleMap;
                        googleMap.getUiSettings().setAllGesturesEnabled(true);


                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(28.1043563,-15.4138618)).zoom(14.0f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        googleMap.moveCamera(cameraUpdate);



                        try {

                            googleMap.setMyLocationEnabled(true);
                            googleMap.getUiSettings().setZoomGesturesEnabled(true);
                            googleMap.getUiSettings().setZoomControlsEnabled(true);
                            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                            googleMap.getUiSettings().setCompassEnabled(true);
                            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                                @Override
                                public boolean onMyLocationButtonClick() {

                                    return false;
                                }
                            });
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }


                        //Get cities polylines
                        (new GetCitiesInfo(getActivity(), new OnCityInfoCompleted() {
                            @Override
                            public void OnComplete(ArrayList<City> cities) {
                                mCities=cities;
                                for(City city:cities){
                                    mGoogleMap.addPolygon(city.getPolygonOptions());
                                }
                            }

                            @Override
                            public void OnError(String result, int resultCode, int resultType) {

                            }
                        })).execute();

                        if (ActivityService.ActivityObject.isRunning()) {
                            //is already running
                            flNoActivity.setVisibility(View.GONE);

                            mActObject = ActivityService.ActivityObject;
                            ResumeActivity();
                            userRoutePolylines=new ArrayList<>();
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mDistance = String.valueOf(Math.round(ActivityService.ActivityObject.getDistance() * 100.0) / 100.0) + "m";
                                    mSpeed = String.valueOf(Math.round(ActivityService.ActivityObject.getSpeed() * 100.0) / 100.0) + "km/h";
                                    mLights=String.format(Locale.ENGLISH,"%.2f", ActivityService.ActivityObject.getLights());

                                    tvDistance.setText(mDistance);
                                    tvSpeed.setText(mSpeed);
                                    tvLights.setText(mLights);
                                    for(PolylineOptions pOptions:ActivityService.ActivityObject.getUserRoutePolylines()){
                                        userRoutePolylines.add(mGoogleMap.addPolyline(pOptions));
                                    }

                                    for(PolylineOptions pOptions:ActivityService.ActivityObject.getInitialRoutePolylines()){
                                        mGoogleMap.addPolyline(pOptions);
                                    }

                                }
                            });

                        } else {
                            //no activity running
                            fabStop.setAlpha(0f);
                            fabStop.setVisibility(View.GONE);
                        }

                        mClusterManager = new MyClusterManager<>(!ActivityService.ActivityObject.isRunning(),getActivity(), mGoogleMap, new GoogleMap.OnCameraIdleListener() {
                            @Override
                            public void onCameraIdle() {
                                if(!ActivityService.ActivityObject.isRunning()){
                                    String title="Comenzar Actividad";
                                    currentCity=null;
                                    fabBus.setVisibility(View.GONE);
                                    fabRailRoad.setVisibility(View.GONE);

                                    for(City city:mCities){
                                        if(PolyUtil.containsLocation(mGoogleMap.getCameraPosition().target,city.getPolygonOptions().getPoints(),false)){
                                            currentCity=city;
                                            title=city.getName();
                                            fabBus.setVisibility(city.isBusEnabled()?View.VISIBLE:View.GONE);
                                            fabRailRoad.setVisibility(city.isRailroadEnabled()?View.VISIBLE:View.GONE);
                                        }
                                    }

                                    myToolbar.setTitle(title);
                                }

                            }
                        });

                        mGoogleMap.setOnCameraIdleListener(mClusterManager);
                        mGoogleMap.setOnMarkerClickListener(mClusterManager);
                    }

                }
            });
        }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Do stuff
            mTime = mActObject.getTimeString();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTime.setText(mTime);
                }
            });

            Log.v("tagg","time: " + mTime);

        }
    }

    @Override
    public void onResume() {
        mActObject=ActivityService.ActivityObject;
        try {
            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mActObject=new ActivityObject();
        try {
            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

}
