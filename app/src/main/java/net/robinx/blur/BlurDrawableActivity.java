package net.robinx.blur;

import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import net.robinx.lib.blur.utils.BlurUtils;
import net.robinx.lib.blur.widget.BlurDrawable;
import net.robinx.lib.blur.widget.BlurMaskRelativeLayout;
import net.robinx.lib.blur.widget.BlurMode;

public class BlurDrawableActivity extends AppCompatActivity {

    private ScrollView mScrollView;
    private RelativeLayout mBlurDrawableRelativeLayout;
    private BlurMaskRelativeLayout mBlurMaskRelativeLayout;

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur_drawable);

        init();
    }

    private void init() {
        mBlurDrawableRelativeLayout = (RelativeLayout) this.findViewById(R.id.blur_drawable_container);
        mBlurDrawableRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBlurDrawableRelativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BlurDrawable blurDrawable = new BlurDrawable(BlurDrawableActivity.this);
                blurDrawable.setDrawOffset(mBlurDrawableRelativeLayout.getLeft(), mBlurDrawableRelativeLayout.getTop() + BlurUtils.getStatusBarHeight(BlurDrawableActivity.this));
                blurDrawable.setCornerRadius(10);
                blurDrawable.setBlurRadius(10);
                blurDrawable.setOverlayColor(Color.parseColor("#64ffffff"));
                mBlurDrawableRelativeLayout.setBackgroundDrawable(blurDrawable);
            }
        });

        mBlurMaskRelativeLayout = (BlurMaskRelativeLayout) this.findViewById(R.id.blur_mask_container);
        mBlurMaskRelativeLayout.blurMode(BlurMode.RENDER_SCRIPT)
                .blurRadius(4);
        mScrollView = (ScrollView) this.findViewById(R.id.sv);
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(getOnScrollChangedListener());
    }

    public ViewTreeObserver.OnScrollChangedListener getOnScrollChangedListener() {
        if (mOnScrollChangedListener == null) {
            mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    //BlurDrawable
                    ViewCompat.postInvalidateOnAnimation(mBlurDrawableRelativeLayout);

                    //BlurMaskRelativeLayout
                    int scrollY = mScrollView.getScrollY();
                    Log.i("robin","scrollY:"+scrollY);
                    float alpha = (float) scrollY/1000;
                    if (alpha > 1f) {
                        alpha = 1f;
                    }
                    Log.i("robin","alpha:"+alpha);
                    mBlurMaskRelativeLayout.setAlpha(1f-(alpha));

                }
            };
        }
        return mOnScrollChangedListener;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOnScrollChangedListener != null) {
            mScrollView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
            mOnScrollChangedListener = null;
        }

    }
}
