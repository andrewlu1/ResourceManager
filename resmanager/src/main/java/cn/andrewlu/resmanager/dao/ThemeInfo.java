package cn.andrewlu.resmanager.dao;

/**
 * 用于插件参数配置。
 */
public class ThemeInfo {
    public int id;
    //主题名称
    public String name;

    //作者名称
    public String author;

    //描述信息
    public String description;

    //一些特性标签
    public String[] tags;

    //预览画面res/drawable-name
    public String[] previewDrawables;

    //最小允许apk版本。
    public int minCompileVersion;

    //最大允许apk版本。主工程版本在此区间内才是安全可靠的。
    public int maxCompileVersion;

    //dev userId
    public String userId;

    //dev apiKey
    public String apiKey;

    //resManager适配版本。不写默认将是+
    public String resToolVersion;

    /**
     * 主题包路径。主题包需要被拷贝到data/data/xx/file/skins/目录下才能生效。
     * 否则不会被识别。
     */
    public String fullPath;

    /**
     * 内置皮肤在assets中的路径。
     */
    public String assetsPath;

    /**
     * 皮肤版本号名称。
     * 在Manifest中配置。
     */
    public String verName;

    /**
     * 皮肤版本号大小。特定的应用版本应该匹配特定的皮肤版本。
     * 在Manifest中配置。
     */
    public int verCode;

    /**
     * 创建时间
     */
    public long createAt;

    /**
     * 皮肤大小。字节为单位。
     * 读取apk 大小。
     */
    public long size;

    /**
     * 皮肤MD5.防止中途被人篡改。
     */
    public String md5;

    //当前选中
    public boolean selected;
}