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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.witcode.light.light.R;
import com.witcode.light.light.Services.ActivityService;
import com.witcode.light.light.Services.ServiceBinder;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.CheckIfLineExists;
import com.witcode.light.light.backend.CheckIfLineExistsListener;
import com.witcode.light.light.backend.ExceptionHandler;
import com.witcode.light.light.backend.GetInitialActivityPoints;
import com.witcode.light.light.backend.GetNearRoutePoints;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.NetworkChangeReceiver;
import com.witcode.light.light.backend.OnConnectivityChangeListener;
import com.witcode.light.light.backend.OnInitialPointsCompleteListener;
import com.witcode.light.light.backend.OnLocationUpdateListener;
import com.witcode.light.light.backend.OnPointsCompleteListener;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.UpdateLights;
import com.witcode.light.light.backend.UpdateToken;
import com.witcode.light.light.backend.ValidateInterurbanBus;
import com.witcode.light.light.backend.ValidateUrbanBus;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.domain.ResizeAnimation;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;


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
    ;
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
    private int color;
    private OnPointsCompleteListener listener;
    private boolean stop = false;


    public static String LINE;
    public static boolean URBAN_CERCANIAS;

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

        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StopActivity();
            }
        });
        myView.findViewById(R.id.fab_walk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartWalk();
            }
        });

        myView.findViewById(R.id.fab_bike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartBike();
            }
        });

        myView.findViewById(R.id.fab_bus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartBus();
            }
        });

        myView.findViewById(R.id.fab_railroad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                StartRailroad();
            }
        });

        myView.findViewById(R.id.fab_carshare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                //StartCarshare();
            }
        });

        myView.findViewById(R.id.fab_recycle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityFragmentRunning = false;
                //StartRecycleSync();
            }
        });


        tvDistance = (TextView) myView.findViewById(R.id.tv_distance);
        tvSpeed = (TextView) myView.findViewById(R.id.tv_speed);
        tvLights = (TextView) myView.findViewById(R.id.tv_lights);
        tvTime = (TextView) myView.findViewById(R.id.tv_time);
        tvGPS = (TextView) myView.findViewById(R.id.tv_gps);

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


                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(40.434795, -3.731692)).zoom(14.0f).build();
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

                        if (ActivityService.IS_SERVICE_RUNNING) {
                            flNoActivity.setVisibility(View.GONE);

                            StartActivity(ActivityService.CURRENT_ACTIVITY);
                            mActivity.setServiceBinder(mServiceBinder);
                        } else {
                            fabStop.setAlpha(0f);
                            fabStop.setVisibility(View.GONE);
                        }
                    }

                }
            });
        }

        mServiceBinder = new ServiceBinder() {
            @Override
            public void OnUpdate(double distance, double speed, int seconds, double lights, ArrayList<MapPoint> userPoints) {

                mDistance = String.valueOf(Math.round(distance * 100.0) / 100.0) + "m";
                mSpeed = String.valueOf(Math.round(speed * 100.0) / 100.0) + "km/h";
                mLights = String.valueOf(Math.round(lights * 100.0) / 100.0);

                int minutes = (seconds) / 60;
                int hours = minutes / 60;

                String shours, sminutes, sseconds;

                shours = hours < 10 ? "0" + String.valueOf(hours) : String.valueOf(hours);
                sminutes = (minutes % 60) < 10 ? "0" + String.valueOf(minutes % 60) : String.valueOf(minutes % 60);
                sseconds = (seconds % 60) < 10 ? "0" + String.valueOf(seconds % 60) : String.valueOf(seconds % 60);

                mTime = shours + ":" + sminutes + ":" + sseconds;

                if(seconds%3==1){
                    mUserRoutePoints = userPoints;
                    UpdateUserRoute();
                }

                UpdateValues();

            }

            @Override
            public void OnGPSUpdate(String GPS) {
                GPSUpdate = GPS;
                UpdateValues();
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
                        mActivity.StartActivityService(ActivityFragment.ACTIVITY_WALK, mServiceBinder);


                        flNoActivity.animate()
                                .translationY(-flNoActivity.getHeight())
                                .alpha(0.0f)
                                .setStartDelay(300)
                                .setDuration(1000)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        StartActivity(ActivityFragment.ACTIVITY_WALK);
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
                        ((MainActivity) getActivity()).StartActivityService(ActivityFragment.ACTIVITY_BIKE, mServiceBinder);


                        StartActivity(ActivityFragment.ACTIVITY_BIKE);
                        flNoActivity.animate()
                                .translationY(-flNoActivity.getHeight())
                                .alpha(0.0f)
                                .setDuration(600)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        flNoActivity.setVisibility(View.GONE);
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
                            public void onComplete(boolean exists, boolean urban) {
                                progressDialog.dismiss();
                                if (exists) {
                                    ((MainActivity) getActivity()).StartActivityService(ActivityFragment.ACTIVITY_BUS, mServiceBinder);

                                    ActivityService.LINE = ((EditText) busConfirmDialog.findViewById(R.id.et_dialog_line)).getText().toString();
                                    ActivityService.URBAN = urban;
                                    URBAN_CERCANIAS = urban;
                                    LINE = ActivityService.LINE;

                                    StartActivity(ActivityFragment.ACTIVITY_BUS);
                                    flNoActivity.animate()
                                            .translationY(-flNoActivity.getHeight())
                                            .alpha(0.0f)
                                            .setDuration(600)
                                            .setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                    flNoActivity.setVisibility(View.GONE);
                                                }
                                            });
                                } else {
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
                        ((MainActivity) getActivity()).StartActivityService(ActivityFragment.ACTIVITY_RAILROAD, mServiceBinder);

                        LINE = mMetroAutoComplete.getText().toString();

                        if (LINE.contains("Linea")) {
                            ActivityService.CERCANIAS = false;
                            URBAN_CERCANIAS = false;
                            LINE = LINE.substring(6);
                            ActivityService.LINE = LINE;
                            Log.v("tagg", "line: " + LINE);
                        } else {
                            ActivityService.CERCANIAS = true;
                            URBAN_CERCANIAS = true;
                            LINE = LINE.substring(11);
                            ActivityService.LINE = LINE;
                            Log.v("tagg", "line: " + LINE);
                        }

                        StartActivity(ActivityFragment.ACTIVITY_RAILROAD);
                        flNoActivity.animate()
                                .translationY(-flNoActivity.getHeight())
                                .alpha(0.0f)
                                .setDuration(600)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        flNoActivity.setVisibility(View.GONE);
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


    public void StartActivity(final int type) {

        if (!activityFragmentRunning) {
            activityFragmentRunning = true;
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

            if (type == ACTIVITY_BUS || type == ACTIVITY_RAILROAD) {


                listener=new OnPointsCompleteListener() {
                    @Override
                    public void OnComplete(ArrayList<ArrayList<MapPoint>> points) {

                        if (points == null) {
                            points = new ArrayList<>();
                        }
                        mLineRoutePoints = points;

                        UpdateInitialTransportRoute();

                        try {
                            getActivity().unregisterReceiver(mrv);

                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        mrv = null;

                    }

                    @Override
                    public void OnError(String result, int resultCode, int resultType) {
                        if (mrv == null) {
                            mrv = new NetworkChangeReceiver(new OnConnectivityChangeListener() {
                                @Override
                                public void OnChange(int connectivity) {

                                    routeTask = (new GetNearRoutePoints(getActivity(), URBAN_CERCANIAS, type, LINE, listener));
                                    routeTask.execute();
                                }
                            });
                            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                            getActivity().registerReceiver(mrv, filter);
                        }

                    }
                };
                //Pedir polilinea de la ruta del transporte
                routeTask = (new GetNearRoutePoints(getActivity(), URBAN_CERCANIAS, type, LINE, listener));

                routeTask.execute();
            }


            new GetInitialActivityPoints(getActivity(), new OnInitialPointsCompleteListener() {
                @Override
                public void OnComplete(ArrayList<MapPoint> points) {
                    mInitialPoints = points;
                    UpdateInitialPoints();
                }

                @Override
                public void OnError(String result, int resultCode, int resultType) {
                    //todo gestionar sin conexion
                }
            }).execute();

        }

        if (stop)
            StopActivity();

    }

    public void StopActivity() {
        stop = false;
        activityFragmentRunning = false;
        mActivity.StopActivityService();
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

                        mActivity.GoToEndActivityFragment(mDistance, mTime, mSpeed, mLights, mEndText, LINE, ActivityService.CURRENT_ACTIVITY, URBAN_CERCANIAS, mUserRoutePoints);
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


        ClearInitialPoints();
        ClearInitialRoute();
        ClearUserRoute();

    }


    public void UpdateValues() {

        if (mActivity != null) {


            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //set textfields
                    tvDistance.setText(mDistance);
                    tvSpeed.setText(mSpeed);
                    tvLights.setText(mLights);
                    tvTime.setText(mTime);
                    tvGPS.setText(GPSUpdate);

                }
            });
        }
    }

    public void UpdateUserRoute() {

        if (mActivity != null) {


            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //user route

                    try {
                        Iterator<MapPoint> iter = mUserRoutePoints.iterator();
                        lastPoint=null;
                        mapPoint=null;
                        polyline=null;

                        while (iter.hasNext()) {
                            mapPoint = iter.next();
                            if (lastPoint != null) {
                                switch (mapPoint.getValidated()) {
                                    case MapPoint.VALIDATED:
                                        color = Color.parseColor("#00ff00");
                                        break;
                                    case MapPoint.NOT_VALIDATED:
                                    case MapPoint.BIG_JUMP:
                                        color = Color.parseColor("#ff0000");
                                        Log.v("tagg", "mappoint is: " + mapPoint.getValidated());
                                        break;
                                    default:
                                        color = Color.parseColor("#0000ff");
                                        break;
                                }


                                polyline = mGoogleMap.addPolyline(new PolylineOptions()
                                        .add(lastPoint.getLatLng())
                                        .add(mapPoint.getLatLng())
                                        .zIndex(1)
                                        .color(color));


                                userRoutePolylines.add(polyline);
                            }
                            lastPoint = mapPoint;
                        }
                    } catch (ConcurrentModificationException e) {
                        e.printStackTrace();
                        new ExceptionHandler(getActivity(), "concurrent_modification_exception: " + e.toString()).execute();
                    }
                }
            });
        }


    }

    public void UpdateInitialTransportRoute() {

        if (getActivity() != null) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //transport route
                    PolylineOptions pOptions;
                    int i = 0;
                    for (ArrayList<MapPoint> mps : mLineRoutePoints) {
                        pOptions = new PolylineOptions();
                        for (MapPoint mp : mps) {
                            pOptions.add(mp.getLatLng());
                            //Log.v("tagg","mp: " + mp.getLatLng().toString());
                        }
                        //Log.v("tagg", "tanda terminada");

                        pOptions.color(Color.parseColor("#000000"));
                        transportRoutePolylines.add(mGoogleMap.addPolyline(pOptions));

                    }
                }
            });
        }
    }

    public void UpdateInitialPoints() {

        if (getActivity() != null) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //initial points
                    Marker marker;
                    BitmapDescriptor icon;
                    for (MapPoint mapPoint : mInitialPoints) {
                        switch (mapPoint.getType()) {
                            case MapPoint.BUS_STOP:
                                //todo change icon
                                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_notifications);
                                break;

                            default:
                                icon = BitmapDescriptorFactory.fromResource(R.drawable.circle_background_primary);
                                break;
                        }


                        marker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(mapPoint.getLatLng())
                                .icon(icon));
                        initialMarkers.add(marker);
                    }


                }
            });
        }
    }

    public void ClearUserRoute() {

        if (getActivity() != null) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Polyline polyline : userRoutePolylines) {
                        polyline.remove();
                    }
                }
            });

            userRoutePolylines = new ArrayList<>();
        }
    }

    public void ClearInitialRoute() {

        if (getActivity() != null) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Polyline polyline : transportRoutePolylines) {
                        polyline.remove();
                    }
                    transportRoutePolylines = new ArrayList<>();

                }
            });
        }
    }

    public void ClearInitialPoints() {

        if (getActivity() != null) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Marker marker : initialMarkers) {
                        marker.remove();
                    }
                    initialMarkers = new ArrayList<>();
                }
            });
        }
    }

    public String getmEndText() {
        return mEndText;
    }

    public void setmEndText(String mEndText) {
        if (mEndText != null)
            this.mEndText = mEndText;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        mActivity = (MainActivity) getActivity();
        super.onAttach(context);
    }

    public MainActivity getmActivity() {
        return mActivity;
    }

    public void setmActivity(MainActivity mActivity) {
        this.mActivity = mActivity;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void onResume() {

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
        try {
            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

}
