package cn.andrewlu.resmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by andrewlu on 2018/1/24.
 */

public class AppCompatSkinActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Skin.R.setXmlLayoutSkinnable(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Skin.R.checkLowMemory();
    }
}
