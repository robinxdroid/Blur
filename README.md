##Blur##
高斯模糊库，四种模糊方式 

### Support ###
    
    1 Blur 四种模糊方式:RenderScript; Native Pixels; Native Bitmap; Java
    2 BlurDrawable 实时模糊Drawable
    3 BlurMaskRelativeLayout 模糊底层背景Layout
    4.BlurRelativeLayout  模糊当前控件Background
    

### Example ###

[Download demo.apk](https://github.com/robinxdroid/Blur/blob/master/app-debug.apk?raw=true)

### Screenshot ###

![](https://raw.githubusercontent.com/robinxdroid/Blur/master/1.png) 
![](https://raw.githubusercontent.com/robinxdroid/Blur/master/2.png) 

### Usage ###
Gradle:
```java
    compile 'net.robinx:lib.blur:1.0.1'
```

**Blur:**

```java
blurBitmap = StackBlur.blurJava(willBlurBitmap, blurRadius, false);  //Java方式，缺点：相对于下面三种，速度慢，模糊大图时容易OOM

blurBitmap = StackBlur.blurNatively(willBlurBitmap, blurRadius, false); //Native Bitmap 方式，NDK实现，与Native Pixels方式差距不大

blurBitmap = StackBlur.blurNativelyPixels(willBlurBitmap, blurRadius, false);  //Native Pixels方式 ，NDK实现，推荐使用

blurBitmap = StackBlur.blurRenderScript(context,willBlurBitmap, blurRadius, false); //RenderScript方式，速度极快，约为java方式100倍的速度，NDK方式20倍速度（不同图片质量测试所得结果不同，仅供参考），缺点：API17以上有效，
// radius最大只能设置25，导致模糊的深度不够，不过可以先压缩图片，此问题不是太严重,如果可以忽略API 17的问题，此方式首选
```   

**BlurDrawable**

扩展Drawable可设置为任何View背景，缺点：实时Blur为了保证FPS使用了RenderScript模糊方式，API 17以上有效

```java
 BlurDrawable blurDrawable = new BlurDrawable(BlurDrawableActivity.this);
                blurDrawable.setDrawOffset(mBlurDrawableRelativeLayout.getLeft(), mBlurDrawableRelativeLayout.getTop() + BlurUtils.getStatusBarHeight(BlurDrawableActivity.this));
                blurDrawable.setCornerRadius(10);
                blurDrawable.setBlurRadius(10);
                blurDrawable.setOverlayColor(Color.parseColor("#64ffffff"));
                mBlurDrawableRelativeLayout.setBackgroundDrawable(blurDrawable);
```   

**BlurMaskRelativeLayout**：

模糊遮罩Layout,可对底层背景进行模糊，此View设置Background无效

 1.XML:

```java
 <net.robinx.lib.blur.widget.BlurMaskRelativeLayout
        android:id="@+id/blur_mask_container"
        android:layout_width="match_parent"
        android:layout_alignParentBottom="true"
        android:layout_height="50dp">
        <TextView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:text="BlurMaskRelativeLayout"
            android:textStyle="bold"
            android:textColor="@android:color/white"
            android:textSize="22sp"/>
    </net.robinx.lib.blur.widget.BlurMaskRelativeLayout>
```   
2.代码中使用: 
```java
mBlurMaskRelativeLayout = (BlurMaskRelativeLayout) this.findViewById(R.id.blur_mask_container);
mBlurMaskRelativeLayout.blurMode(BlurMode.RENDER_SCRIPT)
                 .blurRadius(4);
``` 
 

**BlurRelativeLayout**：

对Background进行模糊

```java
    <net.robinx.lib.blur.widget.BlurRelativeLayout
        android:layout_width="match_parent"
        android:background="@mipmap/bg_5"
        android:layout_height="50dp">
        <TextView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:text="BlurRelativeLayout"
            android:textStyle="bold"
            android:textColor="@android:color/white"
            android:textSize="22sp"/>
    </net.robinx.lib.blur.widget.BlurRelativeLayout>
```



#About me
Email:735506404@robinx.net<br>
Blog:[www.robinx.net](http://www.robinx.net)
