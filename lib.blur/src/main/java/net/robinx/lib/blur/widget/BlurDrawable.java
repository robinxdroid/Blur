package net.robinx.lib.blur.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.Window;

/**
 * Created by Robin on 2016/7/23 12:20.
 */
public class BlurDrawable extends ColorDrawable {

    private int mDownSampleFactor;
    private final View mBlurredBgView;
    private int mBlurredViewWidth, mBlurredViewHeight;
    private boolean mDownSampleFactorChanged;
    private Bitmap mBitmapToBlur, mBlurredBitmap;
    private Canvas mBlurringCanvas;

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlurScript;
    private Allocation mBlurInput, mBlurOutput;

    private float offsetX;
    private float offsetY;

    private int mBlurRadius = 1;

    private static boolean enabled;

    private int mOverlayColor = Color.TRANSPARENT;

    private float cornerRadius = 0;
    private final Path path = new Path();

    /**
     * will only initial once when class loaded
     */
    static {
        enabled = (Build.VERSION.SDK_INT >= 19);
    }

    private final RectF rectF = new RectF();

    public BlurDrawable(View blurredBgView) {
        this.mBlurredBgView = blurredBgView;
        if (enabled) {
            initializeRenderScript(blurredBgView.getContext());
        }
        setOverlayColor(mOverlayColor);
    }

    /**
     * used for dialog/fragment/popWindow/dialog
     *
     * @param activity the blurredView attached
     * @see #setDrawOffset
     */
    public BlurDrawable(Activity activity) {
        this(activity.getWindow().getDecorView());
    }

    /**
     * Set for window
     *
     * @param blurredWindow another window,void draw self(may throw stackoverflow)
     */
    public BlurDrawable(Window blurredWindow) {
        this(blurredWindow.getDecorView());
    }

    @TargetApi(17) public void setBlurRadius(int radius) {
        if (!enabled) {
            return;
        }
        this.mBlurRadius = radius;
        mBlurScript.setRadius(radius);
    }

    @TargetApi(17) public void setDownSampleFactor(int factor) {
        if (!enabled) {
            return;
        }
        if (mDownSampleFactor != factor) {
            mDownSampleFactor = factor;
            mDownSampleFactorChanged = true;
        }
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
    }

    /**
     * set both for blur and non-blur
     */
    public void setOverlayColor(int color) {
        mOverlayColor = color;
        setColor(color);
    }

    @TargetApi(17) private void initializeRenderScript(Context context) {
        mRenderScript = RenderScript.create(context);
        mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        setBlurRadius(mBlurRadius);
        setDownSampleFactor(8);
    }

    private boolean prepare() {
        final int width = mBlurredBgView.getWidth();
        final int height = mBlurredBgView.getHeight();
        if (mBlurringCanvas == null
                || mDownSampleFactorChanged
                || mBlurredViewWidth != width
                || mBlurredViewHeight != height) {
            mDownSampleFactorChanged = false;

            mBlurredViewWidth = width;
            mBlurredViewHeight = height;

            int scaledWidth = width / mDownSampleFactor;
            int scaledHeight = height / mDownSampleFactor;

            scaledWidth = scaledWidth - scaledWidth % 4 + 4;
            scaledHeight = scaledHeight - scaledHeight % 4 + 4;

            if (mBlurredBitmap == null
                    || mBlurredBitmap.getWidth() != scaledWidth
                    || mBlurredBitmap.getHeight() != scaledHeight) {
                mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                if (mBitmapToBlur == null) {
                    return false;
                }

                mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                if (mBlurredBitmap == null) {
                    return false;
                }
            }
            mBlurringCanvas = new Canvas(mBitmapToBlur);

            mBlurringCanvas.scale(1f / mDownSampleFactor, 1f / mDownSampleFactor);
            mBlurInput = Allocation.createFromBitmap(mRenderScript, mBitmapToBlur,
                    Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput.getType());
        }
        return true;
    }

    @TargetApi(17) private void blur(Bitmap mBitmapToBlur, Bitmap mBlurredBitmap) {
        if (!enabled) {
            return;
        }
        mBlurInput.copyFrom(mBitmapToBlur);
        mBlurScript.setInput(mBlurInput);
        mBlurScript.forEach(mBlurOutput);
        mBlurOutput.copyTo(mBlurredBitmap);

        /*this.mBlurredBitmap = StackBlur.blurRenderScript(mBlurredBgView.getContext(),mBitmapToBlur,16,false);*/
        /*this.mBlurredBitmap = StackBlur.blurNatively(mBitmapToBlur,16,false);*/
    }

    /**
     * force enable blur, however it will only works on API 17 or higher
     * if your want to support more, use Support RenderScript Pack
     */
    public void setEnabled(boolean enabled) {
        BlurDrawable.enabled = enabled && (Build.VERSION.SDK_INT >= 17);
    }

    @TargetApi(17)
    public void onDestroy() {
        if (!enabled) {
            return;
        }
        if (mRenderScript != null) {
            mRenderScript.destroy();
        }
    }

    @Override public void draw(Canvas canvas) {

        if (cornerRadius != 0){
            path.reset();
            rectF.set(0, 0, canvas.getWidth(), canvas.getHeight());
            path.addRoundRect(rectF,cornerRadius, cornerRadius, Path.Direction.CCW);
            canvas.clipPath(path);
        }
        if (enabled) {
            drawBlur(canvas);
        }
        //draw overlayColor
        super.draw(canvas);

    }

    @TargetApi(17) private void drawBlur(Canvas canvas) {
        if (prepare()) {
            mBlurredBgView.draw(mBlurringCanvas);
            blur(mBitmapToBlur, mBlurredBitmap);
            canvas.save();
            canvas.translate(mBlurredBgView.getX() - offsetX, mBlurredBgView.getY() - offsetY);
            canvas.scale(mDownSampleFactor, mDownSampleFactor);
            canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
            canvas.restore();
        }
    }

    /**
     * set the offset between top view and blurred view
     */
    @TargetApi(17)
    public void setDrawOffset(float x, float y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    public View getBlurredBgView() {
        return mBlurredBgView;
    }
}
