package com.witcode.light.light;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.LocationRequest;

import java.util.Timer;
import java.util.TimerTask;

import backend.OnLocationUpdateListener;
import backend.OnTaskCompletedListener;
import backend.SyncContainer;
import backend.UpdateLights;
import backend.ValidateBus;


public class StartFragment extends Fragment {
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaterialDialog mDialog, mBusDialog, mRecycleDialog;
    private View myView;
    private Timer walkTimer;
    private int seconds = 0, minutes = 0, hours = 0;
    private double speed = 0;
    private double distance = 0;
    private TimerTask mWalkTimerTask;
    private static Handler mHandler;
    private Location oldLocation, newLocation;
    private double mLights = 0;
    private String locationState;
    private FrameLayout flRecycleDialogFirst, flRecycleDialogSecond, flRecycleDialogThird;
    private int currentActivity = -1;
    final static int ACTIVITY_WALK = 1;
    final static int ACTIVITY_BIKE = 2;
    private boolean recycleSynced=false;
    private int z = 0;

    private TextView tvTime, tvDistance, tvSpeed, tvGPS, tvLights;
    private MaterialDialog dialogWalk = null;

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_start, container, false);

        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);

        myToolbar.setTitle("Comienza una actividad");
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

        myView.findViewById(R.id.fab_walk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartWalk();
            }
        });

        myView.findViewById(R.id.fab_bike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartBike();
            }
        });

        myView.findViewById(R.id.fab_bus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartBus();
            }
        });

        myView.findViewById(R.id.fab_railroad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartRailroad();
            }
        });

        myView.findViewById(R.id.fab_carshare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartCarshare();
            }
        });

        myView.findViewById(R.id.fab_recycle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartRecycle();
            }
        });


        mDialog = new MaterialDialog.Builder(getActivity())
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
                        mDialog.dismiss();
                        ((MainActivity) getActivity()).GoToFragment("home");
                        ((MainActivity) getActivity()).StartWalkService(currentActivity);
                    }
                }).build();


        return myView;
    }

    public void StartWalk() {
        currentActivity = StartFragment.ACTIVITY_WALK;
        mDialog.show();


    }

    public void StartBike() {
        currentActivity = StartFragment.ACTIVITY_BIKE;
        mDialog.show();
    }

    public void StartBus() {

        mBusDialog = new MaterialDialog.Builder(getActivity())
                .title("Vamos a validar tu acción")
                .titleColor(Color.parseColor("#ffffff"))
                .positiveText("Validar")
                .negativeText("Cancelar")
                .backgroundColor(getActivity().getResources().getColor(R.color.PrimaryDark))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Snackbar.make(myView, "Se ha cancelado la acción", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .customView(R.layout.dialog_bus, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        mBusDialog.dismiss();
                        final MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                                .title("Por favor, espere")
                                .content("Estamos validando su acción")
                                .progress(true, 0)
                                .show();

                        ((MainActivity) getActivity()).RequestLocationOnce(new OnLocationUpdateListener() {
                            @Override
                            public void OnLocationLoad(Location location) {
                                Log.v("location_update", "loc_recieved");
                                new ValidateBus(getActivity(),((EditText) mBusDialog.findViewById(R.id.et_dialog_line)).getText().toString(), location, new OnTaskCompletedListener() {
                                    @Override
                                    public void OnComplete(String result, int resultCode, int resultType) {

                                        Log.v("location_update", "val_recieved");
                                        if (resultCode == ValidateBus.VALIDATED) {
                                            new UpdateLights(getActivity(),10, new OnTaskCompletedListener() {
                                                @Override
                                                public void OnComplete(String result, int resultCode, int resultType) {
                                                    Snackbar.make(myView, "La acción se ha realizado con éxito, has ganado 10 lights", Snackbar.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                    Log.v("location_update", "all_good_in_bus_validate");
                                                }
                                            }).execute();

                                        } else if (resultCode == ValidateBus.TOO_FAR) {
                                            Log.v("location_update", "too_far");
                                            Snackbar.make(myView, "La acción no se ha podido validar", Snackbar.LENGTH_SHORT).setAction("VOLVER A INTENTAR", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    StartBus();
                                                }
                                            }).show();
                                            progressDialog.dismiss();
                                        } else {
                                            Log.v("location_update", "strange error");
                                        }


                                    }
                                }).execute();
                            }

                            @Override
                            public void OnLocationTimeOut(Location location) {
                                Log.v("location_update", "loc_recieved_bad");
                                progressDialog.dismiss();
                                if (location == null) {
                                    Snackbar.make(myView, "No se ha podido conseguir una posición válida", Snackbar.LENGTH_SHORT).setAction("VOLVER A INTENTAR", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            StartBus();
                                        }
                                    }).show();
                                }
                            }
                        }, LocationRequest.PRIORITY_HIGH_ACCURACY);
                        Log.v("location_update", "loc_req");
                    }
                })
                .build();
        mBusDialog.show();

    }

    public void StartRailroad() {
        Snackbar.make(myView, "Esta acción estará disponible muy pronto", Snackbar.LENGTH_SHORT).show();
    }

    public void StartCarshare() {
        Snackbar.make(myView, "Esta acción estará disponible muy pronto", Snackbar.LENGTH_SHORT).show();
    }

    public void StartRecycle() {
        mRecycleDialog = new MaterialDialog.Builder(getActivity())

                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(R.color.colorPrimaryDark)
                //.backgroundColor(getActivity().getResources().getColor(R.color.PrimaryDark))
                .positiveText("CANCELAR")
                .positiveColorRes(R.color.colorPrimaryDark)
                .customView(R.layout.dialog_recycle, true)
                .build();

        View CustomView = mRecycleDialog.getCustomView();
        flRecycleDialogFirst = (FrameLayout) CustomView.findViewById(R.id.fl_first_screen);
        flRecycleDialogSecond = (FrameLayout) CustomView.findViewById(R.id.fl_second_screen);
        flRecycleDialogThird = (FrameLayout) CustomView.findViewById(R.id.fl_third_screen);
        ImageView ivSync = (ImageView) CustomView.findViewById(R.id.iv_dialog_sync);

        RotateAnimation anim = new RotateAnimation(360, 0,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1000);

        ivSync.startAnimation(anim);

        OnTaskCompletedListener mListener= new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {

                if(mRecycleDialog.isShowing()){
                    Log.d("container","result:  " + result);
                    switch (resultCode){
                        case SyncContainer.SYNCING:
                            (new SyncContainer(getActivity(),this)).execute();

                            break;
                        case SyncContainer.SYNCED:


                            if(!recycleSynced){
                                flRecycleDialogFirst.animate()
                                        .translationY(-flRecycleDialogFirst.getHeight())
                                        .alpha(0.0f)
                                        .setDuration(1000);

                                flRecycleDialogSecond.setTranslationY(flRecycleDialogSecond.getHeight());
                                flRecycleDialogSecond.animate()
                                        .translationY(0)
                                        .alpha(1.0f)
                                        .setStartDelay(200)
                                        .setDuration(800);

                            }
                            recycleSynced=true;
                            (new SyncContainer(getActivity(),this)).execute();
                            break;
                        case SyncContainer.SYNC_END:

                            if(recycleSynced){
                                flRecycleDialogSecond.animate()
                                        .translationY(-flRecycleDialogSecond.getHeight())
                                        .alpha(0.0f)
                                        .setDuration(1000);

                                flRecycleDialogThird.setTranslationY(flRecycleDialogThird.getHeight());
                                flRecycleDialogThird.animate()
                                        .translationY(0)
                                        .alpha(1.0f)
                                        .setStartDelay(300)
                                        .setDuration(900)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                mRecycleDialog.setActionButton(DialogAction.POSITIVE, "CERRAR");
                                            }
                                        });
                            }else {
                                flRecycleDialogFirst.animate()
                                        .translationY(-flRecycleDialogFirst.getHeight())
                                        .alpha(0.0f)
                                        .setDuration(1000);

                                flRecycleDialogThird.setTranslationY(flRecycleDialogThird.getHeight());
                                flRecycleDialogThird.animate()
                                        .translationY(0)
                                        .alpha(1.0f)
                                        .setStartDelay(300)
                                        .setDuration(900)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                mRecycleDialog.setActionButton(DialogAction.POSITIVE, "CERRAR");
                                            }
                                        });
                            }

                        case SyncContainer.ERROR:
                            break;
                        default:
                            break;
                    }
                }

            }
        };

        (new SyncContainer(getActivity(),mListener)).execute();

        mRecycleDialog.show();
        recycleSynced=false;
    }

    public void DialogStartWalk() {
        seconds = 0;
        mLights = 0;
        if (walkTimer != null)
            walkTimer.cancel();

        walkTimer = new Timer();
        walkTimer.scheduleAtFixedRate(new MyTimerTask(), 0, 1000);
    }

    public void DialogStopWalk() {
        seconds = 0;
        speed = 0;
        distance = 0;
        walkTimer.cancel();
    }

    public void UpdateWalkUI() {
        if (hours > 0) {
            tvTime.setText(hours + "h " + minutes % 60 + "min " + seconds % 60 + "s");
        } else if (minutes > 0) {
            tvTime.setText(minutes + "min " + seconds % 60 + "s");
        } else {
            tvTime.setText(seconds + "s");
        }
        tvSpeed.setText(String.format("%.2f", speed) + "km/h");
        tvDistance.setText(String.format("%.2f", distance) + "m");
        tvGPS.setText(locationState);
        tvLights.setText(String.format("%.2f", mLights));

    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Do stuff

            seconds++;
            minutes = (seconds) / 60;
            hours = minutes / 60;

            mHandler.obtainMessage(1).sendToTarget();


        }
    }

}
