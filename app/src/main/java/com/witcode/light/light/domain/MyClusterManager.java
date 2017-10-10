package com.witcode.light.light.domain;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.witcode.light.light.R;
import com.witcode.light.light.backend.GetInitialStopMapPoints;
import com.witcode.light.light.backend.GetStopTimes;
import com.witcode.light.light.backend.OnStopsCompleted;
import com.witcode.light.light.backend.OnStopsTimesCompleted;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MyClusterManager<T extends ClusterItem> extends ClusterManager implements GoogleMap.OnCameraIdleListener {
    private GoogleMap mGoogleMap;
    private Activity mContext;
    private ArrayList<MapPoint> mMapPoints = new ArrayList<>();
    private GoogleMap.OnCameraIdleListener mListener;
    private MyClusterManager<T> mClusterManager = this;
    private BitmapDescriptor busBitmap = BitmapDescriptorFactory.fromResource(R.mipmap.bus_marker);
    private RecyclerView rvStopTime;
    private boolean draw;
    private ArrayList<Cluster> mClusters = new ArrayList<>();

    public MyClusterManager(boolean draw, Activity context, GoogleMap map, GoogleMap.OnCameraIdleListener listener) {
        super(context, map);
        mGoogleMap = map;
        mContext = context;
        mListener = listener;
        this.draw = draw;
        setRenderer(new MyRenderer());
        mGoogleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
    }


    @Override
    public void onCameraIdle() {

        Log.v("mytag", "onCameraIdle");
        mListener.onCameraIdle();
        if (draw) {
            (new GetInitialStopMapPoints(mContext, mGoogleMap.getProjection().getVisibleRegion().latLngBounds, new OnStopsCompleted() {
                @Override
                public void OnComplete(final ArrayList<MapPoint> mapPoints) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            int i = 0;
                            for (final MapPoint mp : mapPoints) {
                                if (!mMapPoints.contains(mp)) {
                                    mMapPoints.add(mp);

                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addItem(mp);

                                        }
                                    });

                                    Log.v("mytag", "adding mapPoint " + i);
                                }
                                i++;
                            }

                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cluster();
                                }
                            });
                        }
                    });
                }

                @Override
                public void OnError(String result, int resultCode, int resultType) {

                }
            })).execute();
        }

        super.onCameraIdle();
    }


    private class MyRenderer extends DefaultClusterRenderer<MapPoint> {

        public MyRenderer() {
            super(getApplicationContext(), mGoogleMap, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MapPoint mapPoint, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.

            JSONObject json = new JSONObject();

            try {
                json.put("name", mapPoint.getName());
                json.put("stop_id", mapPoint.getId());
                markerOptions.title(json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            markerOptions.icon(busBitmap).snippet(mapPoint.getId());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MapPoint> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            markerOptions.snippet("cluster");
            super.onBeforeClusterRendered(cluster, markerOptions);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    private class MyInfoWindowAdapter implements InfoWindowAdapter {

        private View myContentsView;

        MyInfoWindowAdapter() {
            Log.v("mytag", "starting adapter");
            myContentsView = mContext.getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(final Marker marker) {

            Log.v("mytag", "getting info contents1");

            if (!marker.getSnippet().equals("done") && !marker.getSnippet().equals("cluster")) {
                myContentsView = mContext.getLayoutInflater().inflate(R.layout.custom_info_contents, null);
                Log.v("mytag", "getting info contents11111");
            }
            Log.v("mytag", "getting info contents2");

            rvStopTime = ((RecyclerView) myContentsView.findViewById(R.id.rv_stop_times));
            TextView tvName = (TextView) myContentsView.findViewById(R.id.tv_stop_name);
            final TextView tvNoLines = (TextView) myContentsView.findViewById(R.id.tv_no_lines);
            final ProgressBar pbStopTimes = (ProgressBar) myContentsView.findViewById(R.id.pb_stop_times);
            /*pbStopTimes.getProgressDrawable().setColorFilter(
                    mContext.getResources().getColor(R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);*/
            LinearLayout llInfoWindow = (LinearLayout) myContentsView.findViewById(R.id.ll_info_window);


            Log.v("mytag", "getting info contents3");
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mContext.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            Log.v("mytag", "getting info contents4");
            /*(mContext).getWindowManager()
                    .getDefaultDisplay()
                    .getMetrics(displayMetrics);*/
            width = (int) Math.round(width * 0.8);

            llInfoWindow.setMinimumWidth(width);


            String stopName = "";
            String stopId = "";
            try {
                stopName = (new JSONObject(marker.getTitle())).getString("name");
                stopId = (new JSONObject(marker.getTitle())).getString("stop_id");
            } catch (Exception e) {
                e.printStackTrace();
            }


            tvName.setText(stopName);

            if (!marker.getSnippet().equals("done") && !marker.getSnippet().equals("cluster")) {
                Log.v("mytag", "getting stop times");

                (new GetStopTimes(mContext, stopId, new OnStopsTimesCompleted() {
                    @Override
                    public void OnComplete(ArrayList<Bundle> stopTimes) {

                        Collections.sort(stopTimes, new Comparator<Bundle>() {
                            @Override
                            public int compare(Bundle b1, Bundle b2) {
                                try {
                                    int tl1 = Integer.parseInt(b1.getString("timelapse"));
                                    int tl2 = Integer.parseInt(b2.getString("timelapse"));
                                    return tl1 - tl2;
                                } catch (Exception e) {
                                    if (b1.getString("timelapse").equals("X")) {
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                }
                            }
                        });

                        pbStopTimes.setVisibility(View.GONE);
                        AdjustStopTimes(stopTimes);

                        for (; stopTimes.size() > 5; ) {
                            stopTimes.remove(stopTimes.size() - 1);
                        }

                        if (stopTimes.size() == 0) {
                            tvNoLines.setVisibility(View.VISIBLE);
                        }



                        for(Bundle b:stopTimes){
                            Log.v("mytag",b.toString());
                        }


                        StopTimeAdapter mAdapter = new StopTimeAdapter(stopTimes, mContext);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
                        rvStopTime.setLayoutManager(mLayoutManager);
                        rvStopTime.setItemAnimator(new DefaultItemAnimator());
                        rvStopTime.setAdapter(mAdapter);
                        marker.setSnippet("done");
                        marker.showInfoWindow();

                        rvStopTime = new RecyclerView(mContext);
                    }

                    @Override
                    public void OnError(String result, int resultCode, int resultType) {

                    }
                })).execute();
            } else {
                Log.v("mytag", "not getting stop times");
                if (!marker.getSnippet().equals("cluster"))
                    marker.setSnippet("reload");
            }

            Log.v("mytag", "getting info contents8");
            if (marker.getSnippet().equals("cluster")) {
                Log.v("mytag", "getting info contents9");
                return null;
            } else {
                Log.v("mytag", "getting info contents10");
                return myContentsView;
            }

        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public void removeAll() {
        for (MapPoint mp : mMapPoints) {
            removeItem(mp);
        }
        cluster();
    }

    private void AdjustStopTimes(ArrayList<Bundle> stopTimes) {
        for (int i = 0; i < stopTimes.size(); i++) {
            Bundle st = stopTimes.get(i);
            if (st.getString("timelapse").equals("X")) {
                stopTimes.remove(i);
                AdjustStopTimes(stopTimes);
                break;
            }

        }
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }
}