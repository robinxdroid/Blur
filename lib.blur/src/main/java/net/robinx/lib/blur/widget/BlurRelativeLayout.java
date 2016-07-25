package net.robinx.lib.blur.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import net.robinx.lib.blur.StackBlur;

/**
 * Created by Robin on 2016/7/23 11:51.
 */
public class BlurRelativeLayout extends RelativeLayout{
    private int mBlurRadius = 30;
    private int mCompressFactor = 8;

    private int BLUR_MODE = BlurMode.NATIVE_PIXELS;

    public BlurRelativeLayout(Context context) {
        super(context);
        init();
    }

    public BlurRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlurRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                //original bitmap
                /*Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                draw(new Canvas(bitmap));*/
                setDrawingCacheEnabled(true);
                buildDrawingCache();
                Bitmap bitmap = getDrawingCache();

                //compress bitmap
                Matrix matrix = new Matrix();
                matrix.postScale(1.0f / mCompressFactor, 1.0f / mCompressFactor);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                //blur bitmap
                switch (BLUR_MODE) {
                    case BlurMode.RENDER_SCRIPT:
                        bitmap = StackBlur.blurRenderScript(getContext(),bitmap, mBlurRadius, false);
                        break;
                    case BlurMode.NATIVE_PIXELS:
                        bitmap = StackBlur.blurNativelyPixels(bitmap, mBlurRadius, false);
                        break;
                    case BlurMode.NATIVE_BITMAP:
                        bitmap = StackBlur.blurNatively(bitmap, mBlurRadius, false);
                        break;
                    case BlurMode.JAVA:
                        bitmap = StackBlur.blurJava(bitmap, mBlurRadius, false);
                        break;
                }

                setBackgroundDrawable(new BitmapDrawable(getResources(),bitmap));

                return true;
            }
        });
    }

    public BlurRelativeLayout blurRadius(int blurRadius) {
        mBlurRadius = blurRadius;
        return this;
    }

    public BlurRelativeLayout compressFactor(int compressFactor) {
        mCompressFactor = compressFactor;
        return this;
    }

    public BlurRelativeLayout blurMode(int blurMode) {
        this.BLUR_MODE = blurMode;
        return this;
    }

}
