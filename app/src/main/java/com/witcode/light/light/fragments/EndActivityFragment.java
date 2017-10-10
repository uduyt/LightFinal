package com.witcode.light.light.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.witcode.light.light.R;
import com.witcode.light.light.Services.ActivityService;
import com.witcode.light.light.Services.EndService;
import com.witcode.light.light.Services.EndServiceBinder;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.EndActivityTask;
import com.witcode.light.light.domain.ActivityObject;
import com.witcode.light.light.domain.MapPoint;

import java.util.ArrayList;
import java.util.Locale;


public class EndActivityFragment extends Fragment {
    private static Toolbar myToolbar;
    private View myView;
    private TextView tvDistance, tvTime, tvLights, tvSpeed, tvEndText, tvWaitingValid, tvValid;
    private ActivityObject ActObject;
    private EndActivityTask endTask;
    private String EndText = "";
    private String mActionType;
    private boolean finished = true;
    private ServiceConnection endServiceConnection;
    private EndService mService;

    public static EndActivityFragment getInstance(ActivityObject activityObject, String endText) {
        EndActivityFragment fragment = new EndActivityFragment();
        fragment.setActObject(activityObject);
        fragment.setEndText(endText);
        return fragment;
    }

    public EndActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_end_activity, container, false);

        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        myToolbar.setTitle("Has terminado la actividad");
        myToolbar.setTitleTextColor(Color.parseColor("#ffffff"));

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        tvDistance = (TextView) myView.findViewById(R.id.tv_distance);
        tvTime = (TextView) myView.findViewById(R.id.tv_time);
        tvLights = (TextView) myView.findViewById(R.id.tv_lights);
        tvSpeed = (TextView) myView.findViewById(R.id.tv_speed);
        tvEndText = (TextView) myView.findViewById(R.id.tv_end_message);
        tvWaitingValid = (TextView) myView.findViewById(R.id.tv_waiting_valid);
        tvValid = (TextView) myView.findViewById(R.id.tv_valid_lights);

        tvDistance.setText("...");
        tvTime.setText("...");
        tvLights.setText("...");
        tvSpeed.setText("...");
        tvEndText.setText("Espere por favor...");

        Log.v("mytag", "EndActivityFragment: starting fragment");
        startService();
        return myView;
    }


    public void startService() {

        if (getActivity() != null) {


            endServiceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className, IBinder service) {

                    Log.v("mytag", "EndActivityFragment: onServiceConnected");
                    mService = ((EndService.LocalBinder) service).getService();
                    mService.setData(ActObject,this,(MainActivity) getActivity(), new EndServiceBinder() {
                        @Override
                        public void OnServiceEnded(String lights) {
                            Log.v("mytag", "EndActivityFragment: service ended");
                            tvLights.setText(lights);
                            tvWaitingValid.setVisibility(View.GONE);
                            tvValid.setVisibility(View.VISIBLE);
                            tvValid.setText("¡Has ganado " + lights + " lights!");
                        }
                    });

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("mytag", "EndActivityFragment: setting initial data");
                            String Distance = String.valueOf(Math.round(ActObject.getDistance() * 100.0) / 100.0) + "m";
                            String Speed = String.format(Locale.ENGLISH, "%.2f", ((float)ActObject.getDistance() / 1000) / ((float) ActObject.getSeconds() / 3600)) + "km/h";

                            tvDistance.setText(Distance);
                            tvTime.setText(ActObject.getTimeString());
                            tvSpeed.setText(Speed);
                            tvEndText.setText(EndText);
                            tvWaitingValid.setVisibility(View.VISIBLE);
                        }
                    });
                }


                public void onServiceDisconnected(ComponentName className) {
                    mService = null;
                }
            };

            if (ActObject.isTooFast()) {
                String Distance = String.valueOf(Math.round(ActObject.getDistance() * 100.0) / 100.0) + "m";
                String Speed = String.format(Locale.ENGLISH, "%.2f", ((float)ActObject.getDistance() / 1000) / ((float) ActObject.getSeconds() / 3600)) + "km/h";

                tvDistance.setText(Distance);
                tvTime.setText(ActObject.getTimeString());
                tvSpeed.setText(Speed);
                tvEndText.setText(EndText);
                tvWaitingValid.setVisibility(View.GONE);
                tvLights.setText("0");
            } else {
                Log.v("mytag", "EndActivityFragment: binding fragment");
                getActivity().bindService(new Intent(getContext(),
                        EndService.class), endServiceConnection, Context.BIND_AUTO_CREATE);
            }

        }
    }

    public ActivityObject getActObject() {
        return ActObject;
    }

    public void setActObject(ActivityObject actObject) {
        ActObject = actObject;
    }

    public String getEndText() {
        return EndText;
    }

    public void setEndText(String endText) {
        EndText = endText;
    }

    @Override
    public void onStop() {
        if(mService!=null){
            mService.setFragmentAlive(false);
        }

        super.onStop();
    }
}