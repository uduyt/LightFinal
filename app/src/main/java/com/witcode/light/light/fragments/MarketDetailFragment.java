package com.witcode.light.light.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.domain.MarketItem;
import com.witcode.light.light.R;

import com.witcode.light.light.backend.GetLights;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.SendPromotion;
import com.witcode.light.light.backend.UpdateLights;

public class MarketDetailFragment extends Fragment{
    private static Toolbar myToolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageView ivPhoto, ivDiscount;
    private View myView;
    private MarketItem mMarketItem;

    public MarketDetailFragment() {
        // Required empty public constructor
    }

    public static MarketDetailFragment getInstance(MarketItem item){
        MarketDetailFragment mFragment= new MarketDetailFragment();
        mFragment.setmMarketItem(item);
        return mFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_market_detail, container, false);

        myToolbar = (Toolbar) myView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        myToolbar.setTitle(mMarketItem.getName());
        myToolbar.setTitleTextColor(Color.parseColor("#ffffff"));

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        myView.findViewById(R.id.fab_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("tagg", "clicked");
                new GetLights(getActivity(), GetLights.SUMMED_LIGHTS, new OnTaskCompletedListener() {
                    @Override
                    public void OnComplete(String result, int resultCode, int resultType) {
                        Log.d("tagg", "get_lights with result: " + result);
                        if(Integer.parseInt(result)-Integer.parseInt(mMarketItem.getLights())>0){
                            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                    .content("¿De verdad quieres canjear este descuento?\nTe costará " + mMarketItem.getLights() + " lights")
                                    .positiveText("Canjear")
                                    .negativeText("Cancelar")
                                    .contentColor(Color.parseColor("#ffffff"))
                                    .backgroundColorRes(R.color.PrimaryDark)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            Snackbar.make(myView,"Se ha cancelado la acción", Snackbar.LENGTH_SHORT);
                                        }
                                    })
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            new SendPromotion(getActivity(), mMarketItem, new OnTaskCompletedListener() {
                                                @Override
                                                public void OnComplete(String result, int resultCode, int resultType) {
                                                    Log.d("tagg", "promotion sent");
                                                    if(resultCode==SendPromotion.SUCCESSFUL){
                                                        Log.d("tagg", "promotion sent successful");
                                                        new UpdateLights(getActivity(), -Integer.parseInt(mMarketItem.getLights()),UpdateLights.REMOVE, new OnTaskCompletedListener() {
                                                            @Override
                                                            public void OnComplete(String result, int resultCode, int resultType) {
                                                                if(resultCode==UpdateLights.SUCCESSFUL){
                                                                    Log.d("tagg", "comprado con exito");
                                                                    Snackbar.make(myView,"La acción se ha realizado con éxito", Snackbar.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        }).execute();
                                                    }
                                                }
                                            }).execute();
                                        }
                                    })
                                    .build();

                            dialog.show();
                        }else{
                            Snackbar.make(myView.findViewById(R.id.cl_container),"No tienes suficientets lights", Snackbar.LENGTH_SHORT).show();
                            Log.d("tagg", "not enough lights");
                        }
                    }
                }).execute();
            }
        });

        TextView tvName= (TextView) myView.findViewById(R.id.tv_market_detail_name);
        TextView tvNameDetail= (TextView) myView.findViewById(R.id.tv_market_detail_name_desc);
        TextView tvDescription= (TextView) myView.findViewById(R.id.tv_market_detail_description);
        TextView tvDiscount= (TextView) myView.findViewById(R.id.tv_market_detail_discounts);
        TextView tvPrice= (TextView) myView.findViewById(R.id.tv_market_detail_lights);
        ivPhoto = (ImageView) myView.findViewById(R.id.iv_market_photo);
        ivDiscount = (ImageView) myView.findViewById(R.id.iv_market_discount);

        tvName.setText(mMarketItem.getName());
        tvNameDetail.setText(mMarketItem.getNameDescription());
        tvDiscount.setText(mMarketItem.getDiscount());
        tvPrice.setText(mMarketItem.getLights());
        tvDescription.setText(mMarketItem.getInfo());
        Picasso.with(getActivity()).load("http://sustainabilight.com/fotos/market/market_photo_" + mMarketItem.getId() + ".jpg").into(ivPhoto);
        Picasso.with(getActivity()).load("http://sustainabilight.com/fotos/market/market_discounttt.png").into(ivDiscount);

        return myView;
    }

    public MarketItem getmMarketItem() {
        return mMarketItem;
    }

    public void setmMarketItem(MarketItem mMarketItem) {
        this.mMarketItem = mMarketItem;
    }
}
