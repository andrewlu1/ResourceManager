package cn.andrewlu.resmanager.view;

import android.view.View;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class BackgroundDrawableAction extends SkinnableAction<View> {
    public BackgroundDrawableAction(View view, int resId) {
        super(view, resId, "setBackgroundDrawable");
    }

    @Override
    public void onAction(View view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Drawable) {
            view.setBackgroundDrawable(ResManager.getResource().getDrawable(resId));
        }
    }
}
