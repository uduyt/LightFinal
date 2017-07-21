package com.witcode.light.light.domain;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by rosety on 6/7/17.
 */

public class ResizeAnimation extends Animation {
    final int targetHeight;
    View view;
    int startHeight;
    private boolean mAsc=true;

    public ResizeAnimation(View view, int targetHeight, int startHeight) {
        this.view = view;
        this.targetHeight = targetHeight;
        this.startHeight = startHeight;
    }

    public ResizeAnimation(View view, int targetHeight, int startHeight, boolean asc) {
        this.view = view;
        this.targetHeight = targetHeight;
        this.startHeight = startHeight;
        mAsc=asc;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight;
        if(mAsc){
            newHeight = (int) (startHeight + targetHeight * interpolatedTime);
        }else{
            newHeight = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
        }

        view.getLayoutParams().height = newHeight;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}