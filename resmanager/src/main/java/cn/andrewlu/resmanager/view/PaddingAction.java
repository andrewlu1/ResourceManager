package cn.andrewlu.resmanager.view;

import android.content.res.Resources;
import android.support.annotation.DimenRes;
import android.view.Gravity;
import android.view.View;

import cn.andrewlu.resmanager.ResManager;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class PaddingAction extends SkinnableAction<View> {
    private int[] padding = new int[4];

    public PaddingAction(View view, @DimenRes int paddingLeft, @DimenRes int paddingTop, @DimenRes int paddingRight, @DimenRes int paddingBottom) {
        super(view, 0, "setPadding");
        padding[0] = paddingLeft > 0 ? paddingLeft : padding[0];
        padding[1] = paddingTop > 0 ? paddingTop : padding[1];
        padding[2] = paddingRight > 0 ? paddingRight : padding[2];
        padding[3] = paddingBottom > 0 ? paddingBottom : padding[3];
    }

    @Override
    protected void onUpdateAction(Object... args) {
        if (args != null && args.length >= 2) {
            if (args[0] instanceof Integer && args[1] instanceof Integer) {
                switch ((int) args[0]) {
                    case Gravity.LEFT: {
                        padding[0] = (int) args[1];
                        break;
                    }
                    case Gravity.TOP: {
                        padding[1] = (int) args[1];
                        break;
                    }
                    case Gravity.RIGHT: {
                        padding[2] = (int) args[1];
                        break;
                    }
                    case Gravity.BOTTOM: {
                        padding[3] = (int) args[1];
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onAction(View view) {
        Resources resources = ResManager.getResource();
        int pLeft = padding[0] > 0 ? (int) resources.getDimension(padding[0]) : 0;
        int pTop = padding[1] > 0 ? (int) resources.getDimension(padding[1]) : 0;
        int pRight = padding[2] > 0 ? (int) resources.getDimension(padding[2]) : 0;
        int pBottom = padding[3] > 0 ? (int) resources.getDimension(padding[3]) : 0;
        view.setPadding(pLeft, pTop, pRight, pBottom);
    }
}
