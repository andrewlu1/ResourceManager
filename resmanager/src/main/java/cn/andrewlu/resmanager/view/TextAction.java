package cn.andrewlu.resmanager.view;

import android.widget.TextView;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class TextAction extends SkinnableAction<TextView> {
    public TextAction(TextView view, int resId) {
        super(view, resId, "setText");
    }

    @Override
    public void onAction(TextView view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.String) {
            view.setText(ResManager.getResource().getText(resId));
        }
    }
}
