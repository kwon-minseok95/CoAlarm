package com.corebank.Coalarm;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //초기화
        KakaoSdk.init(this, "367c8105671161e1bee19d910f9160e0");

    }
}
