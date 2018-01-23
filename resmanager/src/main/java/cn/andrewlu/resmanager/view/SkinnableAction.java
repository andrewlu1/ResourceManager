package cn.andrewlu.resmanager.view;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.lang.ref.WeakReference;

public abstract class SkinnableAction<T extends View> {
    private WeakReference<T> view;
    protected final int resId;
    private final String actionName;

    private final static Handler sHandler = new Handler(Looper.getMainLooper());

    public T get() {
        return view.get();
    }

    public String getAction() {
        return actionName;
    }

    public SkinnableAction(T view, int resId, String actionName) {
        this.view = new WeakReference<T>(view);
        this.resId = resId;
        this.actionName = actionName;
    }

    public abstract void onAction(T view);

    public final boolean go() {
        T v = get();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (v != null) {
                onAction(v);
            }
        } else {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    T v = get();
                    if (v != null) {
                        onAction(v);
                    }
                }
            });
        }
        return v != null;
    }

    @Override
    public int hashCode() {
        return actionName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SkinnableAction) {
            return actionName.equals(((SkinnableAction) obj).actionName);
        }
        return false;
    }
}