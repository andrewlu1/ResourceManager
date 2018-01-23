package cn.andrewlu.resmanager.view;

import android.widget.TextView;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class TextSizeAction extends SkinnableAction<TextView> {
    public TextSizeAction(TextView view, int resId) {
        super(view, resId, "setTextSize");
    }

    @Override
    public void onAction(TextView view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Dimen) {
            view.setTextSize(ResManager.getResource().getDimension(resId));
        }
    }
}
