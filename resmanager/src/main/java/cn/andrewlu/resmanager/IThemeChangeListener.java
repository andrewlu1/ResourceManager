package cn.andrewlu.resmanager;

import android.content.res.Resources;

/**
 * Created by andrewlu on 2018/1/20.
 */

public interface IThemeChangeListener {
    /**
     * @param currentTheme 当前主题Resources对象
     */
    void onThemeChanged(Resources currentTheme);

}
