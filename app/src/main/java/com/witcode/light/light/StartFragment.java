package com.witcode.light.light;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.LocationRequest;

import java.util.Timer;
import java.util.TimerTask;

import backend.OnLocationUpdateListener;
import backend.OnTaskCompletedListener;
import backend.SendPromotion;
import backend.UpdateLights;
import backend.ValidateBus;


public class StartFragment extends Fragment {
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaterialDialog mDialog, mBusDialog;
    private View myView;
    private Timer walkTimer;
    private int seconds = 0, minutes = 0, hours = 0;
    private double speed = 0;
    private double distance = 0;
    private TimerTask mWalkTimerTask;
    private static Handler mHandler;
    private Location oldLocation, newLocation;
    private double mLights=0;
    private String locationState;
    private String currentActivity="walk";
    private int z=0;

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
                        dialogWalk = new MaterialDialog.Builder(getActivity())
                                .title("Datos de la actividad")
                                .contentColor(Color.parseColor("#ffffff"))
                                .titleColor(Color.parseColor("#ffffff"))
                                .positiveText("Terminar")
                                .customView(R.layout.dialog_walk, true)
                                .backgroundColor(getActivity().getResources().getColor(R.color.PrimaryDark))
                                /*.negativeText("Pausar")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        DialogPauseWalk();
                                    }
                                })*/

                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialogWalk.dismiss();
                                        DialogStopWalk();
                                        mLights=Math.round(mLights);
                                        int l=(int) mLights;
                                        new UpdateLights(l, new OnTaskCompletedListener() {
                                            @Override
                                            public void OnComplete(String result, int resultCode) {
                                                Snackbar.make(myView, "Se ha terminado la acción, has ganado " + (int)mLights + " lights", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }).execute();
                                    }
                                })
                                .build();
                        tvTime = (TextView) dialogWalk.findViewById(R.id.tv_dialog_time);
                        tvDistance = (TextView) dialogWalk.findViewById(R.id.tv_dialog_distance);
                        tvSpeed = (TextView) dialogWalk.findViewById(R.id.tv_dialog_speed);
                        tvGPS = (TextView) dialogWalk.findViewById(R.id.tv_dialog_gps_signal);
                        tvLights = (TextView) dialogWalk.findViewById(R.id.tv_dialog_lights);
                        DialogStartWalk();


                        dialogWalk.setCanceledOnTouchOutside(false);
                        dialogWalk.show();
                    }
                }).build();

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (seconds % 3 == 1) {
                    if (oldLocation != null) {
                        ((MainActivity) getActivity()).RequestLocationOnce(new OnLocationUpdateListener() {
                            @Override
                            public void OnLocationLoad(Location location) {
                                Log.v("location_update", "locat came");
                                if (location.getTime() - oldLocation.getTime() > 4000) {
                                    if (location.getAccuracy() < 30) {
                                        if (location.distanceTo(oldLocation) >  (location.getAccuracy() + oldLocation.getAccuracy())) {
                                            Log.v("location_update", "adding values");

                                            speed = (location.distanceTo(oldLocation)/ (location.getTime() - oldLocation.getTime())) * 360;
                                            distance += location.distanceTo(oldLocation);
                                            oldLocation = location;
                                            if(currentActivity.equals("walk"))
                                                mLights+=(distance/100)*Math.sqrt(speed/6)*0.7;
                                            else
                                                mLights+=(distance/500)*Math.sqrt(speed/15)*0.7;

                                            if(currentActivity.equals("walk") & speed>16){
                                                dialogWalk.dismiss();
                                                Snackbar.make(myView, "Vas demasiado rápido para estar caminando...", Snackbar.LENGTH_SHORT).show();
                                                DialogStopWalk();
                                            }else if(currentActivity.equals("bike") & speed>60){
                                                dialogWalk.dismiss();
                                                Snackbar.make(myView, "Vas demasiado rápido para estar montando en bici...", Snackbar.LENGTH_SHORT).show();
                                                DialogStopWalk();
                                            }
                                        }
                                    }
                                }
                                Log.d("location_update", "location given with acc: " + location.getAccuracy());
                                if (location.getTime() - oldLocation.getTime() > 10000) {
                                    locationState = "La señal GPS es débil";
                                } else {
                                    locationState = "La señal GPS es buena";
                                }
                            }

                            @Override
                            public void OnLocationTimeOut(Location location) {
                                Log.v("location_update", "locat came bad");
                            }
                        }, LocationRequest.PRIORITY_HIGH_ACCURACY);
                    } else {
                        locationState = "Buscando señal GPS...";
                        ((MainActivity) getActivity()).RequestLocationOnce(new OnLocationUpdateListener() {
                            @Override
                            public void OnLocationLoad(Location location) {
                                Log.v("location_update", "locat came the first time");
                                if (location.getAccuracy() < 30)
                                    oldLocation = location;

                            }

                            @Override
                            public void OnLocationTimeOut(Location location) {
                                Log.v("location_update", "locat came bad the first time");
                            }
                        }, LocationRequest.PRIORITY_HIGH_ACCURACY);
                    }
                }
                UpdateWalkUI();

            }
        };
        return myView;
    }

    public void StartWalk() {
        currentActivity="walk";
        mDialog.show();


    }

    public void StartBike() {
        currentActivity="bike";
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
                                new ValidateBus(((EditText) mBusDialog.findViewById(R.id.et_dialog_line)).getText().toString(), location, new OnTaskCompletedListener() {
                                    @Override
                                    public void OnComplete(String result, int resultCode) {

                                        Log.v("location_update", "val_recieved");
                                        if (resultCode == ValidateBus.VALIDATED) {
                                            new UpdateLights(10, new OnTaskCompletedListener() {
                                                @Override
                                                public void OnComplete(String result, int resultCode) {
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
                                        }else{
                                            Log.v("location_update", "strange error");
                                        }


                                    }
                                }).execute();
                            }

                            @Override
                            public void OnLocationTimeOut(Location location) {
                                Log.v("location_update", "loc_recieved_bad");
                                progressDialog.dismiss();
                                if(location==null){
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
        Snackbar.make(myView, "Esta acción estará disponible muy pronto", Snackbar.LENGTH_SHORT).show();
    }


    public void DialogStartWalk() {
        seconds = 0;
        mLights=0;
        if(walkTimer!=null)
        walkTimer.cancel();

        walkTimer=new Timer();
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
            tvTime.setText(hours + "h " + minutes%60 + "min " + seconds%60 + "s");
        } else if (minutes > 0) {
            tvTime.setText(minutes + "min " + seconds%60 + "s");
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
