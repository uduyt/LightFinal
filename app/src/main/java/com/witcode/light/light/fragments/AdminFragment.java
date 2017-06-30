package com.witcode.light.light.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.R;

import com.witcode.light.light.backend.AddContainer;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.OnLocationUpdateListener;
import com.witcode.light.light.backend.OnTaskCompletedListener;


public class AdminFragment extends Fragment{
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button btContainer, btMetro, btCercanias;
    private MaterialDialog mContainerDialog;
    private Location mLocation;
    private View myView;

    public AdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_admin, container, false);

        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);

        myToolbar.setTitle("Admin");
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

        btContainer=(Button) myView.findViewById(R.id.bt_container);

        btContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContainerDialog = new MaterialDialog.Builder(getActivity())
                        .title("Pulse enviar cuando la precisión sea baja (<10m)")
                        .content("precisión: nula")
                        .positiveText("Enviar")
                        .contentColor(Color.parseColor("#ffffff"))
                        .titleColor(Color.parseColor("#ffffff"))
                        .backgroundColor(getActivity().getResources().getColor(R.color.PrimaryDark))
                        .negativeText("Cancelar")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ((MainActivity) getActivity()).mLocationListener=null;
                                mContainerDialog.dismiss();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mContainerDialog.dismiss();
                                new AddContainer(getActivity(), mLocation, new OnTaskCompletedListener() {
                                    @Override
                                    public void OnComplete(String result, int resultCode, int resultType) {
                                        if(resultCode== MyServerClass.SUCCESSFUL && result.equals("ok")){
                                            Toast.makeText(getActivity(), "Se ha envíado con éxito",
                                                    Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getActivity(), "No se ha podido enviar...",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).execute();

                            }
                        }).build();

                ((MainActivity) getActivity()).mLocationListener=new OnLocationUpdateListener() {
                    @Override
                    public void OnLocationLoad(Location location) {
                        mLocation=location;
                        mContainerDialog.setContent("precisión: " + location.getAccuracy());
                    }

                    @Override
                    public void OnLocationTimeOut(@Nullable Location location) {

                    }
                };

                mContainerDialog.setCanceledOnTouchOutside(false);
                mContainerDialog.show();

            }
        });

        return myView;
    }
}
