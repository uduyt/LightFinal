package com.witcode.light.light.backend;

/**
 * Created by rosety on 28/5/17.
 */

public class ListenerArguments {

    private String mResult;
    private int mResultCode;
    private int mResultType;

    public ListenerArguments(String result, int resultCode, int resultType){
        mResult=result;
        mResultCode=resultCode;
        mResultType=resultType;
    }

    public String getResult() {
        return mResult;
    }

    public void setResult(String mResult) {
        this.mResult = mResult;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public void setResultCode(int mResultCode) {
        this.mResultCode = mResultCode;
    }

    public int getResultType() {
        return mResultType;
    }

    public void setResultType(int mResultType) {
        this.mResultType = mResultType;
    }
}
