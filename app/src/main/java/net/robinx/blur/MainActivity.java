package net.robinx.blur;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.robinx.lib.blur.widget.BlurMaskRelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        this.findViewById(R.id.tv_blur).setOnClickListener(getOnClickListener());
        this.findViewById(R.id.tv_blur_view).setOnClickListener(getOnClickListener());

        BlurMaskRelativeLayout blurLayout = (BlurMaskRelativeLayout) this.findViewById(R.id.blur_container);
        blurLayout.blurRadius(25);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(blurLayout,View.ALPHA,0,1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.start();
    }

    private View.OnClickListener  mOnClickListener;
    private View.OnClickListener getOnClickListener() {
        if (mOnClickListener == null) {
            mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.tv_blur:
                            startActivity(new Intent(MainActivity.this,BlurActivity.class));
                            break;
                        case R.id.tv_blur_view:
                            startActivity(new Intent(MainActivity.this,BlurDrawableActivity.class));
                            break;
                    }
                }
            };
        }
        return mOnClickListener;
    }
}
