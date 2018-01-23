package cn.andrewlu.resmanager.view;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.widget.CompoundButton;
import android.widget.TextView;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class ButtonDrawableAction extends SkinnableAction<CompoundButton> {
    public ButtonDrawableAction(CompoundButton view, int resId) {
        super(view, resId, "setButtonDrawable");
    }

    @Override
    public void onAction(CompoundButton view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Drawable) {
            view.setButtonDrawable(ResManager.getResource().getDrawable(resId));
        } else if (type == Skin.Type.Color) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setButtonTintList(ResManager.getResource().getColorStateList(resId));
            } else {
                view.setButtonDrawable(new ColorDrawable(ResManager.getResource().getColor(resId)));
            }
        }
    }
}
