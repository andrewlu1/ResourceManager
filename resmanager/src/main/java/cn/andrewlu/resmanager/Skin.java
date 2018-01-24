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
import android.view.Gravity;
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
import cn.andrewlu.resmanager.view.CompoundDrawableAction;
import cn.andrewlu.resmanager.view.DrawablePaddingAction;
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

    public void attachAttrToSkin(View view, String attrName, String resIdValue) {
        try {
            Factory factory = getFactory(attrName);
            if (factory != null) {
                SkinnableAction action = factory.create(view, Integer.valueOf(resIdValue), attrName);
                addActionNotGoing(view, action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void attachAttrToSkin(View view, String attrName, int resId) {
        try {
            Factory factory = getFactory(attrName);
            if (factory != null) {
                SkinnableAction action = factory.create(view, resId, attrName);
                addActionNotGoing(view, action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endAttachment(View view) {
        Set<SkinnableAction> actions = findActionsOf(view);
        if (actions == null) return;
        try {
            for (SkinnableAction action : actions) {
                action.go();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void addActionNotGoing(View view, SkinnableAction action) {
        Set<SkinnableAction> actions = findActionsOf(view);
        actions.add(action);
    }

    @Override
    public void onThemeChanged(Resources currentTheme) {
        Set<Integer> viewHashes = actions.keySet();
        if (viewHashes == null) return;
        Integer[] keys = viewHashes.toArray(new Integer[viewHashes.size()]);
        for (Integer hash : keys) {
            Set<SkinnableAction> viewOfActions = actions.get(hash);
            //if there are no actions for this view, remove the hash key.
            if (viewOfActions == null) {
                continue;
            }
//            SkinnableAction[] viewActions = viewOfActions.toArray(new SkinnableAction[viewOfActions.size()]);
            for (SkinnableAction action : viewOfActions) {
                if (!action.go()) {
                    actions.remove(hash);
                    break;
                }
            }
        }
    }

    public final void checkLowMemory() {
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
                if (null == action.get()) {
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

    /**
     * use to replace TextView.setCompoundDrawable() API.
     * Attention: please put your drawable into drawable-nodpi directory to avoid the drawable scale unexcep.
     *
     * @param view        the textView to set with.
     * @param resIdLeft   drawableLeft Id. 0 will ignore.
     * @param resIdTop    drawableTop Id. 0 will ignore.
     * @param resIdRight  drawableRight Id. 0 will ignore.
     * @param resIdBottom drawableBottom Id. 0 will ignore.
     */
    public <T extends TextView> Skin setCompoundDrawable(T view,
                                                         @DrawableRes int resIdLeft,
                                                         @DrawableRes int resIdTop,
                                                         @DrawableRes int resIdRight,
                                                         @DrawableRes int resIdBottom) {
        addAction(view, new CompoundDrawableAction(view, resIdLeft, resIdTop, resIdRight, resIdBottom));
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

    public <T extends SkinnableAction> T getAction(View view, Class<T> actionClass) {
        Set<SkinnableAction> actions = findActionsOf(view);
        if (actions == null) return null;
        for (SkinnableAction action : actions) {
            if (actionClass == action.getClass()) {
                return (T) action;
            }
        }
        return null;
    }

    /**
     * must call it before setContentView API.
     *
     * @param activity the activity that need skinnable.
     */
    public void setXmlLayoutSkinnable(Activity activity) {
        activity.getLayoutInflater().setFactory2(new SkinLayoutInflaterFactory(activity));
    }

    public static Type checkResType(Context context, int resId) {
        if (resId == 0) {
            Log.e("Skin", "Error! onAction. resId == 0");
            return Type.UNKNOWN;
        }
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
        SkinnableAction create(T view, int resId, String actionName);
    }

    {
        appendFactory("background", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName) {
                return new BackgroundAction(view, resId);
            }
        });
        appendFactory("padding", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName) {
                return new PaddingAction(view, resId, resId, resId, resId);
            }
        });
        appendFactory("paddingLeft", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName) {
                PaddingAction action = getAction(view, PaddingAction.class);
                if (action == null) {
                    action = new PaddingAction(view, resId, 0, 0, 0);
                } else {
                    action.update(Gravity.LEFT, resId);
                }
                return action;
            }
        });
        appendFactory("paddingTop", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName) {
                PaddingAction action = getAction(view, PaddingAction.class);
                if (action == null) {
                    action = new PaddingAction(view, 0, resId, 0, 0);
                } else {
                    action.update(Gravity.TOP, resId);
                }
                return action;
            }
        });
        appendFactory("paddingRight", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName) {
                PaddingAction action = getAction(view, PaddingAction.class);
                if (action == null) {
                    action = new PaddingAction(view, 0, 0, resId, 0);
                } else {
                    action.update(Gravity.RIGHT, resId);
                }
                return action;
            }
        });
        appendFactory("paddingBottom", new Factory<View>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName) {
                PaddingAction action = getAction(view, PaddingAction.class);
                if (action == null) {
                    action = new PaddingAction(view, 0, 0, 0, resId);
                } else {
                    action.update(Gravity.BOTTOM, resId);
                }
                return action;
            }
        });
        appendFactory("text", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                return new TextAction(view, resId);
            }
        });

        appendFactory("textSize", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                return new TextSizeAction(view, resId);
            }
        });

        //how can i know if the color is color state List or a general color.
        appendFactory("textColor", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                return new TextColorAction(view, resId);
            }
        });

        appendFactory("src", new Factory<ImageView>() {
            @Override
            public SkinnableAction create(ImageView view, int resId, String actionName) {
                return new ImageSrcAction(view, resId);
            }
        });
        appendFactory("button", new Factory<CompoundButton>() {
            @Override
            public SkinnableAction create(CompoundButton view, int resId, String actionName) {
                return new ButtonDrawableAction(view, resId);
            }
        });

        appendFactory("drawableLeft", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                //here the drawableLeft,drawableTop,drawableRight,drawableBottom is all in one action.
                //so we need update the prev compoundDrawableAction than create a new one.
                CompoundDrawableAction compoundAction = getAction(view, CompoundDrawableAction.class);
                if (compoundAction == null) {
                    compoundAction = new CompoundDrawableAction(view, resId, 0, 0, 0);
                } else {
                    compoundAction.update(Gravity.LEFT, resId);
                }
                return compoundAction;
            }
        });
        appendFactory("drawableTop", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                //here the drawableLeft,drawableTop,drawableRight,drawableBottom is all in one action.
                //so we need update the prev compoundDrawableAction than create a new one.
                CompoundDrawableAction compoundAction = getAction(view, CompoundDrawableAction.class);
                if (compoundAction == null) {
                    compoundAction = new CompoundDrawableAction(view, 0, resId, 0, 0);
                } else {
                    compoundAction.update(Gravity.TOP, resId);
                }
                return compoundAction;
            }
        });
        appendFactory("drawableRight", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                //here the drawableLeft,drawableTop,drawableRight,drawableBottom is all in one action.
                //so we need update the prev compoundDrawableAction than create a new one.
                CompoundDrawableAction compoundAction = getAction(view, CompoundDrawableAction.class);
                if (compoundAction == null) {
                    compoundAction = new CompoundDrawableAction(view, 0, 0, resId, 0);
                } else {
                    compoundAction.update(Gravity.RIGHT, resId);
                }
                return compoundAction;
            }
        });
        appendFactory("drawableBottom", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                //here the drawableLeft,drawableTop,drawableRight,drawableBottom is all in one action.
                //so we need update the prev compoundDrawableAction than create a new one.
                CompoundDrawableAction compoundAction = getAction(view, CompoundDrawableAction.class);
                if (compoundAction == null) {
                    compoundAction = new CompoundDrawableAction(view, 0, 0, 0, resId);
                } else {
                    compoundAction.update(Gravity.BOTTOM, resId);
                }
                return compoundAction;
            }
        });
        appendFactory("drawablePadding", new Factory<TextView>() {
            @Override
            public SkinnableAction create(TextView view, int resId, String actionName) {
                return new DrawablePaddingAction(view, resId);
            }
        });
    }
}
