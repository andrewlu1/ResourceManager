package cn.andrewlu.resmanager.view;

import android.widget.TextView;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class TextColorAction extends SkinnableAction<TextView> {
    public TextColorAction(TextView view, int resId) {
        super(view, resId, "setTextColor");
    }

    @Override
    public void onAction(TextView view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Color) {
            view.setTextColor(ResManager.getResource().getColorStateList(resId));
        }
    }
}
