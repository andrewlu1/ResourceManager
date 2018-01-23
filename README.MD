# Release Note
此方案实现了 分离式皮肤APK 的资源管理。用于多主题方案的资源查询。方案的详细原理参看ATA文档：
https://www.atatech.org/articles/98848

#V1.0.2 (2018.1.23)
* 新增Xml 布局的皮肤化支持。
* 新增自动皮肤化监听，不需要手动监听themeChange事件。
* 修复SkinResources类的一个传参异常。

_使用示例：_
> 在Xml布局中使用：

1. 添加 skin="enable|disable" 属性在你希望得到皮肤支持的控件上， 或者自定义控件 implements Skinnable 接口。
2. 对于希望使用皮肤资源的属性应用 @资源引用，而非具体的值。如：android:text="@string/xxx"

##### 示例：
```xml
    <TextView
        android:id="@+id/previewText"
        skin="enable"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:background="@color/colorAccent"
        android:gravity="center"
        android:text="@string/app_name"
        android:textColor="@color/colorPrimary"
        android:textSize="@dimen/text_size" />
```
> 在代码中添加代码：

在Activity的onCreate函数中调用：<i>Skin.setXmlLayoutSkinnable(this);</i>
[Tip:] 必须在setContentView()之前调用。否则不生效。

> 在[非Xml]代码中支持换肤：

有时候我们希望在代码中修改控件的某属性，而同时又希望此属性修改支持不同皮肤，此时就要用到以下API:
1. Skin.setBackgroundColor(T view, @ColorRes int resId)
2. Skin.setBackground(T view, @DrawableRes int resId);
3. Skin.setPadding(T view, @DimenRes int paddingLeft, @DimenRes int paddingTop,
                                               @DimenRes int paddingRight, @DimenRes int paddingBottom);
4. Skin.setLeft(T view, @DimenRes int resId);
5. Skin.setTop(T view, @DimenRes int resId);
6. Skin.setRight(T view, @DimenRes int resId);
7. Skin.setBottom(T view, @DimenRes int resId);
8. Skin.setText(T view, @StringRes int resId);
9. Skin.setTextSize(T view, @DimenRes int resId);
10. Skin.setTextColor(T view, @ColorRes int resId);
11. Skin.setTypeface(T view, @FontRes int resId);
12. Skin.setImageDrawable(T view, @DrawableRes int resId);
13. Skin.setButtonDrawable(T view, @DrawableRes int resId)

> 功能扩展：
### 1. Xml属性扩展:
框架仅实现了Xml中有限属性的皮肤化支持，支持的属性有：
1. background (View)
2. padding         (View)
3. paddingLeft | paddingTop | paddingRight | paddingBottom  (View)
4. text|textSize | textColor  (TextView)
5. src  (ImageView)
6. button (CompoundButton.buttonDrawable)
<b/>__以上属性会自动识别并加入皮肤变更监听。__
如果以上无法满足你的需求，或者自定义属性需要纳入皮肤监听。则进行以下扩展即可：
1. 继承抽象类：SkinnableAction<T extends View>, 实现[void onAction(T view)]。
```java
//示例：
public class TextSizeAction extends SkinnableAction<TextView> {
    public TextSizeAction(TextView view, int resId) {
        //"setTextSize"字符串用来区分不同的action. 不同的action请不要设置相同的内容。
        super(view, resId, "setTextSize");
    }

    @Override
    public void onAction(TextView view) {
        Skin.Type type = Skin.checkResType(view.getContext(), resId);
        if (type == Skin.Type.Dimen) {
            view.setTextSize(ResManager.getResource().getDimension(resId));
        }
    }
}
```
2. 在Application 初始化代码中注册action:
```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        ResManager.getInstance().init(this);
        //add your own attr in xml, then it will be set skinnable when theme changed. 
        Skin.R.appendFactory("customAttr", new Skin.Factory<CustomView>() {
            @Override
            public SkinnableAction create(View view, int resId, String actionName, Object... args) {
                return new CustomSkinAction(view, resId,actionName,args);
            }
        });
    }
}
```
### 2. 自定义API调用扩展：
通常情况下，调用Skin 类中的API已经能够满足各种要求。但并不是全部，随着Lib升级，我们还将加入更多的设置属性支持换肤。
但在使用过程中，如果有这样的需求，又该怎么办呢？
* 一种方案是:你可以下载ResManager源码，并在Skin中仿照其他接口添加自己的API.
* 另一种方案，Skin已经为你添加了这样的扩展方式，允许用户添加自己的换肤属性到框架中去：
> 扩展步骤：
1. 仍然像扩展XML 属性一样，实现一个自己的属性设置Action. 而且通常情况下，这个action 也将用于Xml中某属性的换肤。

<p>  Action的作用是将某个属性的设置操作包装成一个动作，能够重复调用，以一个代理的角色存在。
因为我们知道，类似View.setBackgroundColor(int resId)这样的操作，有对resId的值进行缓存处理，即当我们第二次调用setBackgroundColor(sameId)时，
系统并不会重复去查询Resource以获取新的属性值，而是直接return. 这导致了当ThemeChange发生时，我们再次调用此接口将不会有任何效果。
于是我们需要将resId的API 调用转换成同名的setBackgroundColor(int color)调用。
<p>  这里的Action 对象将充当一个属性设置的记录Record, 能够用来重复的，回放的多次对View进行属性设置。
<p>
2. 实现自己的Skin API， 做法就如Skin 类中的做法一样:

```java
class MySkinApi{
    public static  <T extends View> Skin setBackgroundColor(T view, @ColorRes int resId) {
        Skin.R.addAction(view, new BackgroundColorAction(view, resId));
        return R;
    }
}
```

### 3. 兼容

如果一个项目已接近完结，而此时需要引入换肤功能，该做哪些事情呢？
你可能需要以下步骤：
1. 制作皮肤APK：
基本做法是： 新建一个Application module 作为皮肤Module, 然后将主工程中的res 资源 拷贝一份到皮肤module中，并根据皮肤UI,修改相应资源Value.
2. 依赖skin-plugin 插件：
只有添加了skin-plugin 插件的工程才能够支持动态换肤，单纯的引入ResManager Lib并不能实现换肤。
```groovy
buildscript.dependencies{ classpath "cn.andrewlu.plugins:skinplugin:+" }

//注意：apply 操作必须要在所有module 包括主工程app Module中 应用。
apply plugin: 'cn.andrewlu.plugins.skinplugin'

```
3. 为皮肤module增加 皮肤属性配置：
每一款皮肤都有一定的自定义属性可供开发者配置，如皮肤名称，作者，描述等信息。
配置内容以gradle DSL 语言形式（类似 android{} 配置）,具体参考本项目提供的demo。
```groovy
skinConfig {
    name = "红色皮肤" //可选，可在Manifest.xml中配置application:label 作为name.
    author = "andrewlu"
    description = "这是一个测试皮肤工程"
    tags = ["美丽", "夜空蓝", "水晶效果"]
    previewDrawables = ["preview_pic_0"]
    minCompileVersion = 1
    maxCompileVersion = 5
    userId = "andrewlu1"
    apiKey = "1234567890qwertyuiop"
}
```
4. 编译皮肤module， 生成release apk. 将皮肤apk 放入主工程的assets/skins/ 目录下。或者放在自己的后台服务器在运行时下载使用。
5. 改造主工程，将所有Xml布局中的需要换样式的属性，修改为资源引用形式 如：@color/...， 并标记需要换肤的控件为skin="enable",
6. 将代码中所有需要换肤的控件属性设置更换为如：view.setBackgroundColor(color) -> Skin.R.setBackgroundColor(R.color.xxx)
7. 在Application 中初始化ResManager：__ResManager.getInstance().init(this);__
8. 在Activity.setContentView() 调用之前开启Xml换肤支持：__Skin.setXmlLayoutSkinnable(this);__
9. 实现自己的皮肤列表，皮肤选择界面。以供用户能够选择某皮肤进行切换。 皮肤列表获取等API,参看ResManager类。
10. 编译主工程，打包发布。
11. DONE.

### DONE!!!!
这个版本将能够实现无缝的皮肤切换业务。 而代码中则不需要做任何多余的有皮肤有关的工作。



#V1.0.1 (2018.1.19)
 * 新增皮肤信息解析逻辑
 * 新增皮肤列表管理能力
 * 重构皮肤初始化逻辑，在程序首次运行时即拷贝assets下所有皮肤到特定目录。
 

#V1.0.0
* 能够解析皮肤包资源内容。
* 能够通知主题变更