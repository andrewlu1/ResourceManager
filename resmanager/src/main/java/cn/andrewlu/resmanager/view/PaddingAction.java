package cn.andrewlu.resmanager.view;

import android.content.res.Resources;
import android.support.annotation.DimenRes;
import android.view.View;

import cn.andrewlu.resmanager.ResManager;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class PaddingAction extends SkinnableAction<View> {
    private int[] padding = new int[4];

    public PaddingAction(View view, @DimenRes int paddingLeft, @DimenRes int paddingTop, @DimenRes int paddingRight, @DimenRes int paddingBottom) {
        super(view, 0, "setPadding");
        padding[0] = paddingLeft;
        padding[1] = paddingTop;
        padding[2] = paddingRight;
        padding[3] = paddingBottom;
    }

    @Override
    public void onAction(View view) {
        Resources resources = ResManager.getResource();
        view.setPadding(
                padding[0] > 0 ? resources.getDimensionPixelSize(padding[0]) : view.getPaddingLeft(),
                padding[1] > 0 ? resources.getDimensionPixelSize(padding[1]) : view.getPaddingTop(),
                padding[2] > 0 ? resources.getDimensionPixelSize(padding[2]) : view.getPaddingRight(),
                padding[3] > 0 ? resources.getDimensionPixelSize(padding[3]) : view.getPaddingBottom()
        );
    }
}
