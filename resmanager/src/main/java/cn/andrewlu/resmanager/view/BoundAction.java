package cn.andrewlu.resmanager.view;

import android.util.Log;
import android.view.View;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class BoundAction extends SkinnableAction<View> {
    public enum Bound {
        LEFT, TOP, RIGHT, BOTTOM
    }

    private Bound bound;

    public BoundAction(View view, int resId, Bound bound) {
        super(view, resId, "set" + bound.name());
        this.bound = bound;
    }

    @Override
    public void onAction(View view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type != Skin.Type.Dimen) {
            Log.e("BoundAction", "resId is not dimen");
            return;
        }
        switch (bound) {
            case TOP: {
                view.setTop(ResManager.getResource().getDimensionPixelSize(resId));
                break;
            }
            case LEFT: {
                view.setLeft(ResManager.getResource().getDimensionPixelSize(resId));
                break;
            }
            case RIGHT: {
                view.setRight(ResManager.getResource().getDimensionPixelSize(resId));
                break;
            }
            case BOTTOM: {
                view.setBottom(ResManager.getResource().getDimensionPixelSize(resId));
                break;
            }
        }
    }
}
