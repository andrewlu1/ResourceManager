package cn.andrewlu.resmanager.view;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.widget.ImageView;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class ImageSrcAction extends SkinnableAction<ImageView> {
    public ImageSrcAction(ImageView view, int resId) {
        super(view, resId, "setImageDrawable");
    }

    @Override
    public void onAction(ImageView view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Drawable) {
            view.setImageDrawable(ResManager.getResource().getDrawable(resId));
        } else if (type == Skin.Type.Color) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setImageTintList(ResManager.getResource().getColorStateList(resId));
            } else {
                view.setImageDrawable(new ColorDrawable(ResManager.getResource().getColor(resId)));
            }
        }
    }
}
