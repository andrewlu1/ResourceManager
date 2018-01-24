package cn.andrewlu.resmanager.view;

import android.widget.TextView;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/24.
 */

public class DrawablePaddingAction extends SkinnableAction<TextView> {
    public DrawablePaddingAction(TextView view, int resId) {
        super(view, resId, "setDrawablePadding");
    }

    @Override
    public void onAction(TextView view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Dimen) {
            view.setCompoundDrawablePadding((int) ResManager.getResource().getDimension(resId));
        }
    }
}
