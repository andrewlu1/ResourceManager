package cn.andrewlu.resmanager;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.FontRes;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.andrewlu.resmanager.view.BackgroundAction;
import cn.andrewlu.resmanager.view.BackgroundColorAction;
import cn.andrewlu.resmanager.view.BackgroundDrawableAction;
import cn.andrewlu.resmanager.view.BoundAction;
import cn.andrewlu.resmanager.view.ButtonDrawableAction;
import cn.andrewlu.resmanager.view.ImageSrcAction;
import cn.andrewlu.resmanager.view.PaddingAction;
import cn.andrewlu.resmanager.view.SkinLayoutInflaterFactory;
import cn.andrewlu.resmanager.view.SkinnableAction;
import cn.andrewlu.resmanager.view.TextAction;
import cn.andrewlu.resmanager.view.TextColorAction;
import cn.andrewlu.resmanager.view.TextSizeAction;
import cn.andrewlu.resmanager.view.TypefaceAction;

/**
 * Created by andrewlu on 2018/1/22.
 */

public class Skin implements IThemeChangeListener {
    private static final Map<Integer, Set<SkinnableAction>> actions = new HashMap<>();
    private static final Map<String, Factory> actionFactory = new HashMap<>();

    public final static Skin R = new Skin();

    private Skin() {
        ResManager.getInstance().addThemeObserver(this);
    }

    private <T extends View> Set<SkinnableAction> findActionsOf(T view) {
        Set<SkinnableAction> actionSet = actions.get(view.hashCode());
        if (actionSet == null) actionSet = new HashSet<>();
        actions.put(view.hashCode(), actionSet);
        return actionSet;
    }

    public final void addAction(View view, SkinnableAction action) {
        Set<SkinnableAction> actions = findActionsOf(view);
        actions.add(action);
        action.go();
    }

    /**
     * append factory used to create the skinnableAction with the attr.
     *
     * @param attrName attribute from xml.
     * @param factory  factory used to create a skinnable action.
     */
    public final void appendFactory(String attrName, Factory factory) {
        actionFactory.put(attrName, factory);
    }

    public Factory getFactory(String attrName) {
        return actionFactory.get(attrName);
    }

    public void addSkinnableIfNeed(View view, String attrName, String resIdValue) {
        try {
            Factory factory = getFactory(attrName);
            if (factory != null) {
                Log.d("SKIN", "add skinnable:" + attrName);
                SkinnableAction action = factory.create(view, Integer.valueOf(resIdValue), attrName);
                addAction(view, action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSkinnableIfNeed(View view, String attrName, int resId) {
        try {
            Factory factory = getFactory(attrName);
            if (factory != null) {
                SkinnableAction action = factory.create(view, resId, attrName);
                addAction(view, action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onThemeChanged(Resources currentTheme) {
        Set<Integer> viewHashes = actions.keySet();
        if (viewHashes == null) return;
        for (Integer hash : viewHashes) {
            Set<SkinnableAction> viewOfActions = actions.get(hash);
            //if there are no actions for this view, remove the hash key.
            if (viewOfActions == null || viewOfActions.size() <= 0) {
                actions.remove(hash);
                continue;
            }
            for (SkinnableAction action : viewOfActions) {
                if (!action.go()) {
                    viewOfActions.remove(action);
                    continue;
                }
            }
        }
    }

    //================================View =================================//
    public <T extends View> Skin setBackgroundColor(T view, @ColorRes int resId) {
        addAction(view, new BackgroundColorAction(view, resId));
        return R;
    }

    public <T extends View> Skin setBackground(T view, @DrawableRes int resId) {
        addAction(view, new BackgroundDrawableAction(view, resId));
        return R;
    }

    public <T extends View> Skin setPadding(T view, @DimenRes int paddingLeft, @DimenRes int paddingTop,
                                            @DimenRes int paddingRight, @DimenRes int paddingBottom) {
        addAction(view, new PaddingAction(view, paddingLeft, paddingTop, paddingRight, paddingBottom));
        return R;
    }

    public <T extends View> Skin setLeft(T view, @DimenRes int resId) {
        addAction(view, new BoundAction(view, resId, BoundAction.Bound.LEFT));
        return R;
    }

    public <T extends View> Skin setTop(T view, @DimenRes int resId) {
        addAction(view, new BoundAction(view, resId, BoundAction.Bound.TOP));
        return R;
    }

    public <T extends View> Skin setRight(T view, @DimenRes int resId) {
        addAction(view, new BoundAction(view, resId, BoundAction.Bound.RIGHT));
        return R;
    }

    public <T extends View> Skin setBottom(T view, @DimenRes int resId) {
        addAction(view, new BoundAction(view, resId, BoundAction.Bound.BOTTOM));
        return R;
    }

    //===============================TextView=============================//
    public <T extends TextView> Skin setText(T view, @StringRes int resId) {
        addAction(view, new TextAction(view, resId));
        return R;
    }

    public <T extends TextView> Skin setTextSize(T view, @DimenRes int resId) {
        addAction(view, new TextSizeAction(view, resId));
        return R;
    }

    public <T extends TextView> Skin setTextColor(T view, @ColorRes int resId) {
        addAction(view, new TextColorAction(view, resId));
        return R;
    }


    public <T extends TextView> Skin setTypeface(T view, @FontRes int resId) {
        addAction(view, new TypefaceAction(view, resId));
        return R;
    }

    //===============================ImageView============================//
    public <T extends ImageView> Skin setImageDrawable(T view, @DrawableRes int resId) {
        addAction(view, new ImageSrcAction(view, resId));
        return R;
    }


    //===============================CompoundButton========================//
    public <T extends CompoundButton> Skin setButtonDrawable(T view, @DrawableRes int resId) {
        addAction(view, new ButtonDrawableAction(view, resId));
        return R;
    }

    /**
     * must call it before setContentView API.
     *
     * @param activity the activity that need skinnable.
     */
    public static void setXmlLayoutSkinnable(Activity activity) {
        activity.getLayoutInflater().setFactory2(new SkinLayoutInflaterFactory(activity));
    }

    public static Type checkResType(Context context, int resId) {
        String type = context.getResources().getResourceTypeName(resId);
        return Type.valueFrom(type);
    }

    public enum Type {
        Drawable("drawable"), Color("color"), String("string"),
        Dimen("dimen"), Integer("integer"), Layout("layout"), Style("style"), Bool("bool"),
        UNKNOWN("");
        private String value;

        Type(String type) {
            value = type;
        }

        public static Type valueFrom(String type) {
            if (Drawable.value.equals(type)) return Drawable;
            if (Color.value.equals(type)) return Color;
            if (String.value.equals(type)) return String;
            if (Dimen.value.equals(type)) return Dimen;
            if (Integer.value.equals(type)) return Integer;
            if (Layout.value.equals(type)) return Layout;
            if (Style.value.equals(type)) return Style;
            if (Bool.value.equals(type)) return Bool;
            return Type.UNKNOWN;
        }
    }

    //=======================================================================//
    public interface Factory<T extends View> {
        SkinnableAction create(T view, int resId, String actionName, Object... args);
    }

    {
        appendFactory("background", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName, Object... args) {
                return new BackgroundAction(view, resId);
            }
        });
        appendFactory("padding", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName, Object... args) {
                return new PaddingAction(view, (int) args[0], (int) args[1], (int) args[2], (int) args[3]);
            }
        });
        appendFactory("paddingLeft", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName, Object... args) {
                return new PaddingAction(view, (int) args[0], 0, 0, 0);
            }
        });
        appendFactory("paddingTop", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName, Object... args) {
                return new PaddingAction(view, 0, (int) args[1], 0, 0);
            }
        });
        appendFactory("paddingRight", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName, Object... args) {
                return new PaddingAction(view, 0, 0, (int) args[2], 0);
            }
        });
        appendFactory("paddingBottom", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName, Object... args) {
                return new PaddingAction(view, 0, 0, 0, (int) args[3]);
            }
        });
        appendFactory("text", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName, Object... args) {
                return new TextAction(view, resId);
            }
        });

        appendFactory("textSize", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName, Object... args) {
                return new TextSizeAction(view, resId);
            }
        });

        //how can i know if the color is color state List or a general color.
        appendFactory("textColor", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName, Object... args) {
                return new TextColorAction(view, resId);
            }
        });

        appendFactory("src", new Factory<ImageView>() {
            @Override
            public SkinnableAction create(ImageView view, int resId, String actionName, Object... args) {
                return new ImageSrcAction(view, resId);
            }
        });
        appendFactory("button", new Factory<CompoundButton>() {
            @Override
            public SkinnableAction create(CompoundButton view, int resId, String actionName, Object... args) {
                return new ButtonDrawableAction(view, resId);
            }
        });
    }
}
