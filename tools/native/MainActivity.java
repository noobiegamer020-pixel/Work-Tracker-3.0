package com.mizou.fieldplanner;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ClockAlarmPlugin.class);
        super.onCreate(savedInstanceState);
        try {
            float scale = getResources().getConfiguration().fontScale;
            getBridge().getWebView().getSettings().setTextZoom(Math.round(scale * 100));
        } catch (Exception e) {
            // keep the default text size
        }
    }
}
