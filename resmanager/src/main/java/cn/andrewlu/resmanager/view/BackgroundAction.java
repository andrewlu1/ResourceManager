package cn.andrewlu.resmanager.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class BackgroundAction extends SkinnableAction<View> {
    public BackgroundAction(View view, int resId) {
        super(view, resId, "setBackground");
    }

    @Override
    public void onAction(View view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Drawable) {
            view.setBackgroundDrawable(ResManager.getResource().getDrawable(resId));
        } else if (type == Skin.Type.Color) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setBackgroundTintList(ResManager.getResource().getColorStateList(resId));
            } else {
                view.setBackgroundColor(ResManager.getResource().getColor(resId));
            }
        }
    }
}
