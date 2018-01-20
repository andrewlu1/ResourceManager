package cn.andrewlu.resmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by andrewlu on 2018/1/20.
 */

public class ResManager {
    private final static ResManager sInstance = new ResManager();
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private final Map<String, SkinRes> skinCache = new HashMap<>();
    private SkinRes mCurrentTheme;
    private final Map<Integer, Integer> mMainIdMap = new HashMap<>();

    private final List<WeakReference<IThemeChangeListener>> themeObservers = new LinkedList<>();
    private final List<WeakReference<IThemeChangeListener>> deadThemeObservers = new LinkedList<>();

    private final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mWorkThread = Executors.newCachedThreadPool();

    //you should put you skin apk in assets/skins/ directory.
    private final String mSkinAssetDir = "skins/";

    private final static String TAG = ResManager.class.getSimpleName();

    private ResManager() {
    }

    public final static ResManager getInstance() {
        return sInstance;
    }

    /**
     * init must called as fast as the application created.
     *
     * @param context setup appContext.
     */
    public final void init(Context context) {
        if (mContext != null) return;
        if (context == null) throw new RuntimeException("init with null context!!");
        mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences("res", Context.MODE_PRIVATE);
        mWorkThread.execute(new Runnable() {
            @Override
            public void run() {
                mMainIdMap.putAll(loadIdMap(mContext.getResources(), mSkinAssetDir + "data", true));
            }
        });
        String currentThemeName = mSharedPreferences.getString("current_res_name", null);

        Log.d(TAG, "init,currentTheme is:" + currentThemeName);

        if (currentThemeName == null || currentThemeName.isEmpty()) return;
        loadSkinResFromAssets(currentThemeName);
    }

    private final static class SkinRes {
        public String resFilePath;
        public Resources res;
        public Map<Integer, Integer> idMap = new HashMap<>();
    }

    /**
     * get current theme resource object.
     *
     * @return return current ThemeResources or the system Resources.
     */
    public static Resources getResource() {
        SkinRes cur = getInstance().mCurrentTheme;
        if (cur != null && cur.res != null) return cur.res;
        return getInstance().mContext.getResources();
    }

    public void addThemeObserver(IThemeChangeListener observer) {
        for (WeakReference<IThemeChangeListener> ob : themeObservers) {
            if (ob.get() == observer) {
                Log.d(TAG, "addThemeObserver failed. same observer found:" + observer);
                return;
            }
        }
        themeObservers.add(new WeakReference<>(observer));
    }

    public void removeThemeObserver(IThemeChangeListener observer) {

        for (WeakReference<IThemeChangeListener> ob : themeObservers) {
            if (ob.get() == observer) {
                themeObservers.remove(ob);
                return;
            }
        }
        Log.d(TAG, "removeThemeObserver failed. no observer found:" + observer);
    }

    private void onThemeChanged(final Resources res) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onThemeChanged");
                deadThemeObservers.clear();
                for (WeakReference<IThemeChangeListener> ob : themeObservers) {
                    IThemeChangeListener observer = ob.get();
                    if (observer != null) {
                        observer.onThemeChanged(res);
                    } else {
                        deadThemeObservers.add(ob);
                    }
                }
                themeObservers.removeAll(deadThemeObservers);
            }
        });
    }

    /**
     * load skin id map data.
     *
     * @param resources 当前主题resource对象.
     * @param assetPath idmap data file in assets file path.
     * @param reverse   whether it should reverse the key-value pair.
     * @return resNameHashCode-IdValue.
     */
    public Map<Integer, Integer> loadIdMap(Resources resources, String assetPath,
                                           boolean reverse) {
        DataInputStream dataInput = null;
        try {
            dataInput = new DataInputStream(new BufferedInputStream(resources.getAssets().open(assetPath)));
            Map<Integer, Integer> map = new HashMap<>();
            int key = 0, value = 0;
            while (true) {
                if (dataInput.available() > 0) {
                    key = dataInput.readInt();
                } else break;
                if (dataInput.available() > 0) {
                    value = dataInput.readInt();
                } else break;
                if (reverse) {
                    map.put(value, key);
                } else {
                    map.put(key, value);
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dataInput != null) {
                try {
                    dataInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Collections.emptyMap();
    }

    /**
     * load a resource object file an apk file.
     *
     * @param apkPath skin file path.
     * @return resources object with the apk file.
     */
    public Resources loadSkinResources(String apkPath) {
        try {
            if (!new File(apkPath).exists()) {
                throw new Exception("resource apk does not exist!");
            }
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, apkPath);
            SkinResources resources = new SkinResources(assetManager, mContext.getResources());
            return resources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mContext.getResources();
    }

    /**
     * set theme that in assets file paths.
     *
     * @param themeName 主题名称.
     */
    public void setTheme(String themeName) {
        if (themeName == null) {
            Log.e(TAG, "setTheme themeName isNull");
            return;
        }
        if (mCurrentTheme != null && themeName.equals(mCurrentTheme.resFilePath)) {
            return;
        }
        if (skinCache.get(themeName) != null) {
            mCurrentTheme = skinCache.get(themeName);
            if (mCurrentTheme != null) {
                mSharedPreferences.edit().putString("current_res_name", themeName).apply();
            }
            onThemeChanged(getResource());
        } else {
            loadSkinResFromAssets(themeName);
        }
    }

    /**
     * set Theme from external file such as download skined apks.
     *
     * @param filePath  主题皮肤文件路径
     * @param themeName 主题名称
     */
    public void setThemeWithFile(String filePath, String themeName) {
        if (themeName == null) {
            Log.e(TAG, "setTheme themeName isNull");
            return;
        }
        if (mCurrentTheme != null && themeName.equals(mCurrentTheme.resFilePath)) {
            Log.i(TAG, "setTheme themeName equals currentTheme:" + themeName);
            return;
        }
        if (skinCache.get(themeName) != null) {
            mCurrentTheme = skinCache.get(themeName);
            if (mCurrentTheme != null) {
                mSharedPreferences.edit().putString("current_res_name", themeName).apply();
            }
            onThemeChanged(getResource());
        } else {
            loadSkinFromFile(filePath, themeName);
        }
    }

    private final static class MyIdMapFilter implements SkinResources.IdMapFilter {
        private Map<Integer, Integer> mainIdMap;
        private Map<Integer, Integer> resIdMap;

        public MyIdMapFilter(Map<Integer, Integer> mainIdMap, Map<Integer, Integer> resIdMap) {
            this.mainIdMap = mainIdMap;
            this.resIdMap = resIdMap;
        }

        @Override
        public Integer filter(int reId) {
            do {
                if (mainIdMap == null || resIdMap == null) break;
                Integer resHashCode = mainIdMap.get(reId);
                if (resHashCode == null) break;
                Integer targetId = resIdMap.get(resHashCode);
                if (targetId == null) break;
                Log.d(TAG, String.format("<%d---%d---%d>", reId, resHashCode.intValue(), targetId.intValue()));
                return targetId;
            } while (false);
            Log.e(TAG, String.format("resId:%d has no map data!", reId));
            return null;
        }
    }

    private void loadSkinResFromAssets(final String themeName) {
        mWorkThread.execute(new Runnable() {
            @Override
            public void run() {
                File skinFile = new File(mContext.getFilesDir(), String.format("%s%s.apk", mSkinAssetDir, themeName));
                if (!skinFile.exists()) {
                    if (!copyResFromAssets(themeName)) {
                        System.out.println("copy skin file failed.");
                        return;
                    }
                }
                loadSkin(skinFile.getAbsolutePath(), themeName);
            }
        });
    }

    private void loadSkinFromFile(final String filePath, final String themeName) {
        mWorkThread.execute(new Runnable() {
            @Override
            public void run() {
                File skinFile = new File(mContext.getFilesDir(), String.format("skins/%s.apk", themeName));
                if (!skinFile.exists()) {
                    if (!copySkinFromFile(filePath, themeName)) {
                        System.out.println("copy skin file failed.");
                        return;
                    }
                }
                loadSkin(skinFile.getAbsolutePath(), themeName);
            }
        });
    }

    private void loadSkin(String skinFilePath, String themeName) {
        Resources resources = loadSkinResources(skinFilePath);
        Map<Integer, Integer> idMap = loadIdMap(resources, mSkinAssetDir + "data", false);
        SkinRes res = new SkinRes();
        res.resFilePath = themeName;
        res.idMap.putAll(idMap);
        res.res = resources;
        mCurrentTheme = res;
        if (resources instanceof SkinResources) {
            ((SkinResources) resources).setFilter(new MyIdMapFilter(mMainIdMap, res.idMap));
        }
        onThemeChanged(getResource());
    }

    private boolean copyResFromAssets(String resName) {
        File skinDir = new File(mContext.getFilesDir(), "skins");
        if (!skinDir.exists()) skinDir.mkdirs();
        File targetFile = new File(skinDir, resName + ".apk");
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = mContext.getResources().getAssets().open(mSkinAssetDir + resName + ".apk");
            fileOutputStream = new FileOutputStream(targetFile);
            byte[] buff = new byte[1024];
            int readCount = -1;
            while ((readCount = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, readCount);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean copySkinFromFile(String srcFilePath, String skinName) {
        File skinDir = new File(mContext.getFilesDir(), "skins");
        if (!skinDir.exists()) skinDir.mkdirs();
        File targetFile = new File(skinDir, skinName + ".apk");
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = new FileInputStream(srcFilePath);
            fileOutputStream = new FileOutputStream(targetFile);
            byte[] buff = new byte[1024];
            int readCount = -1;
            while ((readCount = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, readCount);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
