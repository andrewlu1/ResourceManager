package cn.andrewlu.resmanager.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.andrewlu.resmanager.Skin;
import cn.andrewlu.resmanager.Skinnable;

/**
 * Created by andrewlu on 2018/1/23.
 */

public class SkinLayoutInflaterFactory implements LayoutInflater.Factory2 {
    private WeakReference<Activity> activityWeakReference;
    private final static Map<String, String> attrKV = new HashMap<>();
    private final static String SKIN_ATTR = "skin";

    public SkinLayoutInflaterFactory(Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;
        try {
            Activity activity = activityWeakReference.get();
            if (activity instanceof AppCompatActivity) {
                AppCompatDelegate delegate = ((AppCompatActivity) activity).getDelegate();
                view = delegate.createView(parent, name, context, attrs);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                if (-1 == name.indexOf('.')) {//-1则不是自定义的view
                    if ("ViewStub".equals(name)) {
                        return null;
                    }
                    if ("View".equals(name)) {
                        view = inflater.createView(name, "android.view.", attrs);
                    }
                    if (view == null) {
                        view = inflater.createView(name, "android.widget.", attrs);
                    }
                    if (view == null) {
                        view = inflater.createView(name, "android.webkit.", attrs);
                    }
                } else {
                    view = inflater.createView(name, null, attrs);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (view != null) {
            applySkinActions(context, view, attrs);
        }
        return view;
    }

    private void applySkinActions(Context context, View view, AttributeSet attrs) {
        Log.d("SkinLayoutInflater", "============================");
        attrKV.clear();
        if (view instanceof Skinnable) {
            attrKV.put(SKIN_ATTR, "enable");
        }
        String name, value;
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            name = attrs.getAttributeName(i);
            value = attrs.getAttributeValue(i);
            Log.d("SkinLayoutInflater", name + ":" + value);
            if (value.charAt(0) == '@') {
                attrKV.put(name, value);
            } else if (SKIN_ATTR.equals(name)) {
                attrKV.put(name, value);
            }
        }
        if (!"enable".equals(attrKV.get(SKIN_ATTR))) {
            return;
        }
        appendSkinnableActions(view, attrKV);
    }

    private void appendSkinnableActions(View view, Map<String, String> attrs) {
        Set<String> attrNames = attrs.keySet();
        for (String attrName : attrNames) {
            Skin.R.addSkinnableIfNeed(view, attrName, attrs.get(attrName).substring(1));
        }
    }
}
