package cn.andrewlu.resmanager.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.widget.TextView;

import cn.andrewlu.resmanager.ResManager;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class TypefaceAction extends SkinnableAction<TextView> {
    public TypefaceAction(TextView view, int resId) {
        super(view, resId, "setTypeface");
    }

    @Override
    public void onAction(TextView view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.setTypeface(ResManager.getResource().getFont(resId));
        }
    }
}
