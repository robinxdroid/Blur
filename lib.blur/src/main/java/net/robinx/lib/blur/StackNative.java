package net.robinx.lib.blur;

import android.graphics.Bitmap;

/**
 * This use jni blur bitmap and pixels
 * Blur arithmetic is StackBlur
 */
public class StackNative {

    /**
     * Load genius jni file
     */
    static {
        System.loadLibrary("blur");
    }

    /**
     * Blur Image By Pixels
     *
     * @param img Img pixel array
     * @param w   Img width
     * @param h   Img height
     * @param r   Blur radius
     */
    protected static native void blurPixels(int[] img, int w, int h, int r);

    /**
     * Blur Image By Bitmap
     *
     * @param bitmap Img Bitmap
     * @param r      Blur radius
     */
    protected static native void blurBitmap(Bitmap bitmap, int r);
}
