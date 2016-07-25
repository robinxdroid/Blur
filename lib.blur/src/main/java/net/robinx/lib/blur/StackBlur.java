package net.robinx.lib.blur;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * This is blur image class
 * Use {@link StackNative} fast blur bitmap
 * Blur arithmetic is StackBlur
 */
final public class StackBlur extends StackNative {

    private static Bitmap buildBitmap(Bitmap bitmap, boolean canReuseInBitmap) {
        // If can reuse in bitmap return this or copy
        Bitmap rBitmap;
        if (canReuseInBitmap) {
            rBitmap = bitmap;
        } else {
            rBitmap = bitmap.copy(bitmap.getConfig(), true);
        }
        return (rBitmap);
    }

    /**
     * StackBlur By Jni Bitmap
     *
     * @param original         Original Image
     * @param radius           Blur radius
     * @param canReuseInBitmap Can reuse In original Bitmap
     * @return Image Bitmap
     */
    public static Bitmap blurNatively(Bitmap original, int radius, boolean canReuseInBitmap) {
        if (radius < 1) {
            return null;
        }

        Bitmap bitmap = buildBitmap(original, canReuseInBitmap);

        // Return this none blur
        if (radius == 1) {
            return bitmap;
        }

        //Jni BitMap Blur
        blurBitmap(bitmap, radius);

        return (bitmap);
    }

    /**
     * StackBlur By Jni Pixels
     *
     * @param original         Original Image
     * @param radius           Blur radius
     * @param canReuseInBitmap Can reuse In original Bitmap
     * @return Image Bitmap
     */
    public static Bitmap blurNativelyPixels(Bitmap original, int radius, boolean canReuseInBitmap) {
        if (radius < 1) {
            return null;
        }

        Bitmap bitmap = buildBitmap(original, canReuseInBitmap);

        // Return this none blur
        if (radius == 1) {
            return bitmap;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        // Jni Pixels Blur
        blurPixels(pix, w, h, radius);

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    /**
     * StackBlur By Java Bitmap
     *
     * @param original         Original Image
     * @param radius           Blur radius
     * @param canReuseInBitmap Can reuse In original Bitmap
     * @return Image Bitmap
     */
    public static Bitmap blurJava(Bitmap original, int radius, boolean canReuseInBitmap) {
        if (radius < 1) {
            return (null);
        }

        Bitmap bitmap = buildBitmap(original, canReuseInBitmap);

        if (radius == 1) {
            return bitmap;
        }

        StackJava.blur(bitmap,radius);

        return (bitmap);
    }

    /**
     * StackBlur By RenderScript（Just used for API level 17）
     *
     * @param original         Original Image
     * @param radius           Blur radius 0~25
     * @param canReuseInBitmap Can reuse In original Bitmap
     * @return Image Bitmap
     */
    public static Bitmap blurRenderScript(Context context,Bitmap original, int radius, boolean canReuseInBitmap) {
        if (radius < 1) {
            return (null);
        }

        Bitmap bitmap = buildBitmap(original, canReuseInBitmap);

        if (radius == 1) {
            return bitmap;
        }

        StackRenderScript.getInstance(context).blur(bitmap,radius);

        return (bitmap);
    }
}
