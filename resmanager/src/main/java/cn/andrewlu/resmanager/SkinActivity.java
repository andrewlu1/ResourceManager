package cn.andrewlu.resmanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

/**
 * Created by andrewlu on 2018/1/24.
 */

public class SkinActivity extends Activity {

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
