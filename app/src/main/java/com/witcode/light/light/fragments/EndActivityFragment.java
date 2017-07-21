package com.witcode.light.light.fragments;

import android.graphics.Color;
import android.os.Bundle;
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
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.AddActivityHistory;
import com.witcode.light.light.backend.EndActivityTask;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.OnTaskUpdateListener;
import com.witcode.light.light.backend.UpdateLights;
import com.witcode.light.light.domain.MapPoint;

import java.util.ArrayList;


public class EndActivityFragment extends Fragment {
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View myView;
    private TextView tvDistance, tvTime, tvLights, tvSpeed, tvEndText, tvWaitingValid, tvValid;
    private String mDistance, mTime, mLights, mSpeed, mEndText, mLine;
    private boolean mUrbanCercanias;
    private int mActivityType;
    private ArrayList<MapPoint> mUserRoutePoints;
    private EndActivityTask endTask;
    private String mActionType;

    public EndActivityFragment() {
        // Required empty public constructor
    }

    public void setData(String distance, String time, String speed, String lights, String endText, String line, int activityType, boolean urbanCercanias, ArrayList<MapPoint> userPoints) {
        mDistance = distance;
        mTime = time;
        mSpeed = speed;
        mLights = lights;
        mEndText = endText;
        mUserRoutePoints = userPoints;
        mLine = line;
        mActivityType = activityType;
        mUrbanCercanias = urbanCercanias;
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

        UpdateUI();
        return myView;
    }

    public String getmDistance() {
        return mDistance;
    }

    public void setmDistance(String mDistance) {
        this.mDistance = mDistance;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmLights() {
        return mLights;
    }

    public void setmLights(String mLights) {
        this.mLights = mLights;
    }

    public String getmSpeed() {
        return mSpeed;
    }

    public void setmSpeed(String mSpeed) {
        this.mSpeed = mSpeed;
    }

    public String getmEndText() {
        return mEndText;
    }

    public void setmEndText(String mEndText) {
        this.mEndText = mEndText;
    }

    public String getmLine() {
        return mLine;
    }

    public void setmLine(String mLine) {
        this.mLine = mLine;
    }

    public boolean ismUrbanCercanias() {
        return mUrbanCercanias;
    }

    public void setmUrbanCercanias(boolean mUrbanCercanias) {
        this.mUrbanCercanias = mUrbanCercanias;
    }

    public int getmActivityType() {
        return mActivityType;
    }

    public void setmActivityType(int mActivityType) {
        this.mActivityType = mActivityType;
    }

    public ArrayList<MapPoint> getmUserRoutePoints() {
        return mUserRoutePoints;
    }

    public void setmUserRoutePoints(ArrayList<MapPoint> mUserRoutePoints) {
        this.mUserRoutePoints = mUserRoutePoints;
    }

    public void UpdateUI() {

        if (getActivity() != null) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvWaitingValid.setVisibility(View.VISIBLE);
                    tvValid.setVisibility(View.GONE);

                    tvDistance.setText(mDistance);
                    tvTime.setText(mTime);
                    tvLights.setText(mLights);
                    tvSpeed.setText(mSpeed);
                    tvEndText.setText(mEndText);

                    if (!mDistance.equals("...")) {
                        endTask = new EndActivityTask(getActivity(), mActivityType, mLine, mUrbanCercanias, mUserRoutePoints, mLights, new OnTaskUpdateListener() {
                            @Override
                            public void OnUpdate(int resultCode) {
                                tvValid.setVisibility(View.VISIBLE);
                                tvWaitingValid.setVisibility(View.GONE);
                                tvValid.setText("¡Has ganado " + Math.round(Double.valueOf(mLights)) + " lights!");
                                Log.v("tagg", "end task on update");

                                switch (mActivityType) {
                                    case ActivityFragment.ACTIVITY_WALK:
                                        mActionType = UpdateLights.WALK;
                                        break;
                                    case ActivityFragment.ACTIVITY_BIKE:
                                        mActionType = UpdateLights.BIKE;
                                        break;

                                    case ActivityFragment.ACTIVITY_BUS:
                                        mActionType = UpdateLights.BUS;
                                        break;

                                    case ActivityFragment.ACTIVITY_RAILROAD:
                                        mActionType = UpdateLights.RAILROAD;
                                        break;

                                    case ActivityFragment.ACTIVITY_RECYCLE:
                                        mActionType = UpdateLights.RECYCLE;
                                        break;

                                    case ActivityFragment.ACTIVITY_CARSHARE:
                                        mActionType = UpdateLights.CAR_SHARE;
                                        break;

                                    default:
                                        mActionType = UpdateLights.OTHER;
                                        break;

                                }

                                new AddActivityHistory(getActivity(), mLights, mSpeed, mTime, mDistance, mActionType, new OnTaskCompletedListener() {
                                    @Override
                                    public void OnComplete(String result, int resultCode, int resultType) {

                                    }
                                }).execute();
                            }

                            @Override
                            public void OnError(int resultCode) {
                                tvWaitingValid.setVisibility(View.VISIBLE);
                                tvValid.setVisibility(View.GONE);
                                ((MainActivity) getActivity()).AddToRetryService(endTask);
                                Log.v("tagg", "end task on error");
                            }
                        });

                        endTask.runTask();

                    }

                }
            });


        }

    }
}
