package com.andarb.simplyforreddit.utils;

import android.content.Context;
import android.view.View;

import com.andarb.simplyforreddit.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/* Initialize AdMob banner */
public final class AdMob {

    // App id used with admob for testing purposes only
    private static final String ADMOB_ID = "ca-app-pub-3940256099942544~3347511713";

    public static void initialize(Context context, View rootView) {
        AdView adView = rootView.findViewById(R.id.admob_banner);
        MobileAds.initialize(context, ADMOB_ID);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
