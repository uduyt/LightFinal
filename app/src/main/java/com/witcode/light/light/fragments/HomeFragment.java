package com.witcode.light.light.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.R;
import com.witcode.light.light.backend.DismissNotification;
import com.witcode.light.light.backend.GetNotifications;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.ToggleFav;
import com.witcode.light.light.domain.MarketItem;
import com.witcode.light.light.domain.MarketItemAdapter;
import com.witcode.light.light.domain.Notification;
import com.witcode.light.light.domain.NotificationsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    private android.support.v7.widget.Toolbar myToolbar;
    private RecyclerView recyclerView;
    private Context mContext;
    private ArrayList<Notification> mNotifications;
    private NotificationsAdapter mAdapter;
    private View myView, vEndOfFeed;
    private int mLimit = 0;
    private LinearLayoutManager mLayoutManager;
    private boolean mIsLoading = false;
    private OnTaskCompletedListener onCompleteListener;
    private ProgressBar pbNotifications;
    private NestedScrollView nsvNotifications;
    private HomeFragment mFragment = this;
    private SwipeRefreshLayout swipeContainer;

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

        //TODO shared preferences to see if notification card has been dismissed, if so, it should not appear

        pbNotifications = (ProgressBar) myView.findViewById(R.id.pb_notifications);
        recyclerView = (RecyclerView) myView.findViewById(R.id.rv_notifications);
        vEndOfFeed = myView.findViewById(R.id.ll_end_of_feed);
        nsvNotifications = (NestedScrollView) myView.findViewById(R.id.nsv_notifications);
        swipeContainer = (SwipeRefreshLayout) myView.findViewById(R.id.swipeContainer);

        mLayoutManager = new LinearLayoutManager(getActivity());

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLimit -= 10;
                LoadMore();
            }
        });

        nsvNotifications.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    Log.v("tagg", "scrolled to bottom");
                    LoadMore();
                }
            }
        });

        onCompleteListener = new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {
                mIsLoading = false;
                Log.v("tagg", "get notifications executed");
                pbNotifications.setVisibility(View.GONE);
                if (resultCode == MyServerClass.SUCCESSFUL) {

                    try {
                        JSONArray jsonNotifications = new JSONArray(result);
                        JSONObject jsonNotification;
                        mNotifications = new ArrayList<>();
                        Notification notification;
                        for (int i = 0; i < jsonNotifications.length(); i++) {
                            jsonNotification = jsonNotifications.getJSONObject(i);
                            notification = new Notification();
                            notification.setTitle(jsonNotification.getString("title"));
                            notification.setSubtitle(jsonNotification.getString("subtitle"));
                            notification.setImage(jsonNotification.getString("image"));
                            notification.setId(jsonNotification.getString("id"));
                            notification.setButton1Text(jsonNotification.getString("button1_text"));
                            notification.setButton2Text(jsonNotification.getString("button2_text"));


                            if (jsonNotification.getString("fav").equals("yes")) {
                                notification.setFav(true);
                            } else {
                                notification.setFav(false);
                            }

                            if (jsonNotification.getString("closeable").equals("1")) {
                                notification.setCloseable(true);
                            } else {
                                notification.setCloseable(false);
                            }

                            switch (jsonNotification.getString("type")) {
                                case "challenge":
                                    notification.setType(Notification.CHALLENGE);
                                    break;
                                case "market":
                                    notification.setType(Notification.MARKET);
                                    break;
                                case "new":
                                    notification.setType(Notification.NEW);
                                    break;
                                case "tweet":
                                    notification.setType(Notification.TWEET);
                                    break;
                                default:
                                    notification.setType(Notification.NEW);
                                    break;
                            }

                            switch (jsonNotification.getString("action")) {
                                case "market_action":
                                    notification.setAction(Notification.MARKET_ACTION);
                                    break;
                                case "ranking_action":
                                    notification.setAction(Notification.RANKING_ACTION);
                                    break;
                                case "start_activity_action":
                                    notification.setAction(Notification.START_ACTIVITY_ACTION);
                                    break;
                                default:
                                    notification.setAction(Notification.NONE);
                                    break;
                            }

                            switch (jsonNotification.getString("button1_action")) {
                                case "market_action":
                                    notification.setButton1Action(Notification.MARKET_ACTION);
                                    break;
                                case "ranking_action":
                                    notification.setButton1Action(Notification.RANKING_ACTION);
                                    break;
                                case "start_activity_action":
                                    notification.setButton1Action(Notification.START_ACTIVITY_ACTION);
                                    break;
                                default:
                                    notification.setButton1Action(Notification.NONE);
                                    break;
                            }

                            switch (jsonNotification.getString("button2_action")) {
                                case "market_action":
                                    notification.setButton2Action(Notification.MARKET_ACTION);
                                    break;
                                case "ranking_action":
                                    notification.setButton2Action(Notification.RANKING_ACTION);
                                    break;
                                case "start_activity_action":
                                    notification.setButton2Action(Notification.START_ACTIVITY_ACTION);
                                    break;
                                default:
                                    notification.setButton2Action(Notification.NONE);
                                    break;
                            }
                            mNotifications.add(notification);
                        }

                        mAdapter = new NotificationsAdapter(mNotifications, mFragment, getActivity());

                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(mAdapter);


                        mAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    swipeContainer.setRefreshing(false);

                }
            }
        };

        mNotifications = new ArrayList<>();
        LoadMore();

        View fabLighter;
        fabLighter = myView.findViewById(R.id.fab_lighter);
        fabLighter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).GotoStartActivityFragment();
            }
        });
        return myView;
    }

    private void LoadMore() {
        Log.v("tagg", "limit: " + mLimit + ", notifs: " + mNotifications.size());
        if (mLimit <= mNotifications.size()) {
            mLimit += 10;
            mIsLoading = true;
            pbNotifications.setVisibility(View.VISIBLE);
            (new GetNotifications(getActivity(), mLimit, onCompleteListener)).execute();
            vEndOfFeed.setVisibility(View.GONE);

        } else {
            vEndOfFeed.setVisibility(View.VISIBLE);
        }
    }

    public void RemoveNotification(int pos, String id) {
        mNotifications.remove(pos);
        mAdapter.notifyItemRemoved(pos);

        (new DismissNotification(getActivity(), id, new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {
                if (resultCode == MyServerClass.NOT_CONNECTED) {

                    Toast.makeText(mContext, "No hay conexión a internet, los cambios no se guardarán...", Toast.LENGTH_LONG).show();
                }
            }
        })).execute();

    }

    public void ToggleFav(String id, String fav) {

        (new ToggleFav(getActivity(), fav, id, new OnTaskCompletedListener() {
            @Override
            public void OnComplete(String result, int resultCode, int resultType) {
                if (resultCode == MyServerClass.NOT_CONNECTED) {

                    Toast.makeText(mContext, "No hay conexión a internet, los cambios no se guardarán...", Toast.LENGTH_LONG).show();
                }
            }
        })).execute();

    }

}