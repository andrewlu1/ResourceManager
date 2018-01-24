package cn.andrewlu.resmanager.view;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import cn.andrewlu.resmanager.ResManager;
import cn.andrewlu.resmanager.Skin;

/**
 * Created by andrewlu on 2018/1/24.
 */

public class CompoundDrawableAction extends SkinnableAction<TextView> {
    private int[] mIds = new int[4];

    // left | top | right | bottom.
    public CompoundDrawableAction(TextView view, int... ids) {
        super(view, 0, "setCompoundDrawable");
        if (ids == null) return;
        if (ids.length >= 4) mIds[3] = ids[3] > 0 ? ids[3] : mIds[3];
        if (ids.length >= 3) mIds[2] = ids[2] > 0 ? ids[2] : mIds[2];
        if (ids.length >= 2) mIds[1] = ids[1] > 0 ? ids[1] : mIds[1];
        if (ids.length >= 1) mIds[0] = ids[0] > 0 ? ids[0] : mIds[0];

    }

    @Override
    protected void onUpdateAction(Object... args) {
        if (args != null && args.length >= 2) {
            if (args[0] instanceof Integer && args[1] instanceof Integer) {
                switch ((int) args[0]) {
                    case Gravity.LEFT: {
                        mIds[0] = (int) args[1];
                        break;
                    }
                    case Gravity.TOP: {
                        mIds[1] = (int) args[1];
                        break;
                    }
                    case Gravity.RIGHT: {
                        mIds[2] = (int) args[1];
                        break;
                    }
                    case Gravity.BOTTOM: {
                        mIds[3] = (int) args[1];
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onAction(TextView view) {
        Drawable dLeft = mIds[0] > 0 ? getDrawable(mIds[0]) : null;
        Drawable dTop = mIds[1] > 0 ? getDrawable(mIds[1]) : null;
        Drawable dRight = mIds[2] > 0 ? getDrawable(mIds[2]) : null;
        Drawable dBottom = mIds[3] > 0 ? getDrawable(mIds[3]) : null;
        view.setCompoundDrawablesWithIntrinsicBounds(dLeft, dTop, dRight, dBottom);
    }

    private Drawable getDrawable(int resId) {
        View view = get();
        if (view == null) return null;
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Drawable) {
            Drawable drawable = ResManager.getResource().getDrawable(resId);
//            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            return drawable;
        } else {
            Log.e("CompoundDrawableAction", "setCompoundDrawable miss match a none drawable res");
        }
        return null;
    }
}
