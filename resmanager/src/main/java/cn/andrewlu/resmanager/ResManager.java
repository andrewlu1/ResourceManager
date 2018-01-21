package cn.andrewlu.resmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.andrewlu.resmanager.dao.ThemeDao;
import cn.andrewlu.resmanager.dao.ThemeInfo;

/**
 * Created by andrewlu on 2018/1/20.
 */

public class ResManager {
    private final static ResManager sInstance = new ResManager();
    private Context mContext;
    private final Map<String, SkinRes> skinCache = new HashMap<>();
    private SkinRes mCurrentTheme;
    private final Map<Integer, Integer> mMainIdMap = new HashMap<>();
    private SharedPreferences mSharedPreferences;

    private final List<WeakReference<IThemeChangeListener>> themeObservers = new LinkedList<>();
    private final List<WeakReference<IThemeChangeListener>> deadThemeObservers = new LinkedList<>();

    private final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mWorkThread = Executors.newCachedThreadPool();

    //you should put you skin apk in assets/skins/ directory.
    private final String mSkinAssetDir = "skins/";
    private ThemeDao themeDao;

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
        mSharedPreferences = mContext.getSharedPreferences("theme_info", Context.MODE_PRIVATE);

        themeDao = new ThemeDao(context);
        //加载主工程皮肤资源。
        mWorkThread.execute(new Runnable() {
            @Override
            public void run() {
                mMainIdMap.putAll(loadIdMap(mContext.getResources(), mSkinAssetDir + "data", true));
            }
        });

        //检查当前选中主题。
        ThemeInfo currentTheme = themeDao.getCurrentTheme();
        if (currentTheme != null) {
            //异步加载当前皮肤。
            loadCurrentThemeAyn(currentTheme.name);
        }

        //检查assets中默认皮肤是否拷贝完成。
        checkSkinFileLoadFinished();
    }

    /**
     * 获取所有主题列表。
     * 包括内置的，以及从外部拷贝过来的。
     *
     * @return
     */
    public final List<ThemeInfo> getAllThemes() {
        return themeDao.getAllThemeInfo();
    }

    /**
     * 从数据库查找theme 相应的信息并加载。
     *
     * @param themeName
     */
    private void loadCurrentThemeAyn(final String themeName) {
        mWorkThread.execute(new Runnable() {
            @Override
            public void run() {
                ThemeInfo themeInfo = themeDao.getThemeByName(themeName);
                if (themeInfo != null) {
                    loadSkin(themeInfo.fullPath, themeInfo.name);
                }
            }
        });
    }

    private final static class SkinRes {
        public String resFilePath;
        public Resources res;
        public Map<Integer, Integer> idMap = new HashMap<>();
    }

    //计算文件md5
    private String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    //检查皮肤资源是否初始化完成。
    private void checkSkinFileLoadFinished() {
        //处理assets中的资源，并将其拷贝到data中。
        boolean loadThemeFinished = mSharedPreferences.getBoolean("theme_load_finish", false);
        if (!loadThemeFinished) {
            mWorkThread.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String[] skinFiles = mContext.getResources().getAssets().list(mSkinAssetDir);
                        if (skinFiles == null || skinFiles.length == 0) {
                            mSharedPreferences.edit().putBoolean("theme_load_finish", true).apply();
                            return;
                        }
                        boolean hasError = false;
                        for (String skinFile : skinFiles) {
                            if (!skinFile.toLowerCase().endsWith(".apk")) continue;
                            ThemeInfo info = themeDao.getThemeByAssetName(skinFile);
                            if (info != null) continue;

                            String target = copyResFromAssets(skinFile);
                            if (target != null) {
                                Resources resources = loadSkinResources(target);
                                PackageInfo packageInfo = getPackageInfo(target);
                                info = getThemeInfoInner(resources);
                                if (info != null) {
                                    info.verName = packageInfo.versionName;
                                    info.verCode = packageInfo.versionCode;
                                    if (info.name == null && packageInfo.applicationInfo != null) {
                                        info.name = packageInfo.applicationInfo.name;
                                    }
                                    info.fullPath = target;
                                    info.assetsPath = skinFile;//带了.apk后缀
                                    info.size = new File(target).length();
                                    info.md5 = getMd5ByFile(new File(target));

                                    if (!themeDao.saveTheme(info)) {
                                        hasError = true;
                                        continue;
                                    }
                                } else {
                                    hasError = true;
                                    continue;
                                }
                            }
                        }
                        if (!hasError) {
                            mSharedPreferences.edit().putBoolean("theme_load_finish", true).apply();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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

    /**
     * 添加主题监听。
     *
     * @param observer
     */
    public void addThemeObserver(IThemeChangeListener observer) {
        for (WeakReference<IThemeChangeListener> ob : themeObservers) {
            if (ob.get() == observer) {
                Log.d(TAG, "addThemeObserver failed. same observer found:" + observer);
                return;
            }
        }
        themeObservers.add(new WeakReference<>(observer));
    }

    /**
     * 移除主题变更监听。
     *
     * @param observer
     */
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
    private Map<Integer, Integer> loadIdMap(Resources resources, String assetPath,
                                            boolean reverse) {
        DataInputStream dataInput = null;
        try {
            dataInput = new DataInputStream(new BufferedInputStream(resources.getAssets().open(assetPath)));
            String magic = dataInput.readUTF();
            if (!"SKIN".equals(magic)) return null;
            dataInput.readUTF();

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
    private Resources loadSkinResources(String apkPath) {
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
     * 获取apk 信息。
     *
     * @param apkPath
     * @return
     */
    private PackageInfo getPackageInfo(String apkPath) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        return pInfo;
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
            onThemeChanged(getResource());
        } else {
            loadCurrentThemeAyn(themeName);
        }
        themeDao.setThemeSelected(themeName);
    }

    /**
     * set Theme from external file such as download skined apks.
     *
     * @param filePath 主题皮肤文件sd路径
     */
    public void setThemeWithFile(final String filePath) {
        if (filePath == null) {
            Log.e(TAG, "setTheme themeName isNull");
            return;
        }

        mWorkThread.execute(new Runnable() {
            @Override
            public void run() {
                String target = copySkinFromFile(filePath);
                ThemeInfo info = getThemeInfoFromFile(target);
                if (info != null) {
                    themeDao.setThemeSelected(info.name);
                    loadSkin(info.fullPath, info.name);
                }
            }
        });
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
                return targetId;
            } while (false);
            return null;
        }
    }

    /**
     * 从文件加载皮肤资源。
     *
     * @param skinFilePath
     * @param themeName
     */
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

    //从assets中拷贝到data目录
    private String copyResFromAssets(String resName) {
        File skinDir = new File(mContext.getFilesDir(), mSkinAssetDir);
        if (!skinDir.exists()) skinDir.mkdirs();
        if (!resName.endsWith(".apk")) {
            resName += ".apk";
        }
        File targetFile = new File(skinDir, resName);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = mContext.getResources().getAssets().open(mSkinAssetDir + resName);
            fileOutputStream = new FileOutputStream(targetFile);
            byte[] buff = new byte[1024];
            int readCount = -1;
            while ((readCount = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, readCount);
            }
            return targetFile.getAbsolutePath();
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
        return null;
    }

    /**
     * 从外部文件拷贝皮肤到data目录。
     *
     * @param srcFilePath
     * @return
     */
    private String copySkinFromFile(String srcFilePath) {
        File srcFile = new File(srcFilePath);
        if (!srcFile.exists()) return null;

        File skinDir = new File(mContext.getFilesDir(), mSkinAssetDir);
        if (!skinDir.exists()) skinDir.mkdirs();

        File targetFile = new File(skinDir, srcFile.getName());

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            fileOutputStream = new FileOutputStream(targetFile);
            byte[] buff = new byte[1024];
            int readCount = -1;
            while ((readCount = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, readCount);
            }
            return targetFile.getAbsolutePath();
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
        return null;
    }

    //从data 文件中加载主题配置。
    private ThemeInfo getThemeInfoInner(Resources resources) {
        DataInputStream dataInput = null;
        ThemeInfo info = new ThemeInfo();
        try {
            dataInput = new DataInputStream(new BufferedInputStream(resources.getAssets().open(mSkinAssetDir + "data")));
            String magic = dataInput.readUTF();
            if (!"SKIN".equals(magic)) return null;
            String skinConfig = dataInput.readUTF();
            JSONObject jsonObject = new JSONObject(skinConfig);
            info.name = jsonObject.optString("name");
            info.userId = jsonObject.optString("userId");
            info.apiKey = jsonObject.optString("apiKey");
            info.assetsPath = jsonObject.optString("assetsPath");
            info.author = jsonObject.optString("author");
            info.createAt = jsonObject.optLong("createAt");
            info.description = jsonObject.optString("description");
            info.maxCompileVersion = jsonObject.optInt("maxCompileVersion");
            info.md5 = jsonObject.optString("md5");
            info.minCompileVersion = jsonObject.optInt("minCompileVersion");

            JSONArray tags = jsonObject.optJSONArray("tags");
            if (tags != null) {
                String[] tagsArrays = new String[tags.length()];
                for (int i = 0; i < tags.length(); i++) {
                    tagsArrays[i] = tags.optString(i, "");
                }
                info.tags = tagsArrays;
            }

            JSONArray prevDrawables = jsonObject.optJSONArray("previewDrawables");
            if (prevDrawables != null) {
                String[] drawableArrays = new String[prevDrawables.length()];
                for (int i = 0; i < prevDrawables.length(); i++) {
                    drawableArrays[i] = prevDrawables.optString(i, "");
                }
                info.previewDrawables = drawableArrays;
            }

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
        return info;
    }

    //从data 中加载主题配置。
    private ThemeInfo getThemeInfoFromFile(String filePath) {
        try {
            Resources resources = loadSkinResources(filePath);
            PackageInfo packageInfo = getPackageInfo(filePath);
            ThemeInfo info = getThemeInfoInner(resources);
            if (info != null) {
                info.verName = packageInfo.versionName;
                info.verCode = packageInfo.versionCode;
                if (info.name == null && packageInfo.applicationInfo != null) {
                    info.name = packageInfo.applicationInfo.name;
                }
                info.fullPath = filePath;
//                info.assetsPath = skinFile;//带了.apk后缀
                info.size = new File(filePath).length();
                info.md5 = getMd5ByFile(new File(filePath));

                if (!themeDao.saveTheme(info)) {
                    return null;
                }
                return info;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
