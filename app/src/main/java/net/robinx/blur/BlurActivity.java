package net.robinx.blur;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import net.robinx.lib.blur.StackBlur;
import net.robinx.lib.blur.utils.BlurUtils;

import java.util.ArrayList;
import java.util.List;

public class BlurActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE_GALLERY_IMAGE = 0x01;
    private ImageView mRootImageView;
    private ImageView mOriginalImageView;
    private ImageView mBlurImageView;
    private CheckBox mCompressCheckBox;
    private TextView mBlurTimeTextView;

    private Bitmap mOriginalBitmap;
    private Bitmap mCompressedBitmap;

    private int blurRadius = 5;

    private static final int JAVA_BLUR = 0x01,JNI_BITMAP_BLUR = 0x02,JNI_PIXELS_BLUR = 0x03,RENDER_SCRIPT_BLUR = 0x04;
    private int BlurMode = RENDER_SCRIPT_BLUR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur);

        init();
    }

    private void init() {
        mRootImageView = (ImageView) this.findViewById(R.id.img_root_bg);
        mBlurTimeTextView = (TextView) this.findViewById(R.id.tv_blur_time);
        mOriginalImageView = (ImageView) this.findViewById(R.id.img_origin);
        mBlurImageView = (ImageView) this.findViewById(R.id.img_blur);
        this.findViewById(R.id.tv_choose).setOnClickListener(getOnClickListener());
        this.findViewById(R.id.tv_blur).setOnClickListener(getOnClickListener());
        Spinner spinner = (Spinner) this.findViewById(R.id.sp);
        mCompressCheckBox = (CheckBox) this.findViewById(R.id.cb_compress);
        final TextView radiusTextView = (TextView) this.findViewById(R.id.tv_radius);
        SeekBar radiusSeekBar = (SeekBar) this.findViewById(R.id.sb_radius);

        //background
        Bitmap bgBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.bg_2);
        Bitmap compressedBgBitmap = BlurUtils.compressBitmap(bgBitmap,8);
        Bitmap blurBgBitmap = StackBlur.blurNativelyPixels(compressedBgBitmap,25,false);
        mRootImageView.setImageBitmap(blurBgBitmap);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mRootImageView,View.ALPHA,0,1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.start();

        List<String> strings = new ArrayList<>();
        strings.add("render script");
        strings.add("jni pixels");
        strings.add("jni bitmap");
        strings.add("java");

        spinner.setAdapter(new SpinnerAdapter(strings));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        BlurMode = RENDER_SCRIPT_BLUR;

                        break;
                    case 1:
                        BlurMode = JNI_PIXELS_BLUR;

                        break;
                    case 2:
                        BlurMode = JNI_BITMAP_BLUR;
                        break;
                    case 3:
                        BlurMode = JAVA_BLUR;
                        break;
                }
                blur();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mCompressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                blur();
            }
        });

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blurRadius = i;
                radiusTextView.setText("blur radius:"+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                blur();
            }
        });
    }

    private View.OnClickListener mOnClickListener;

    private View.OnClickListener getOnClickListener() {
        if (mOnClickListener == null) {
            mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.tv_choose:
                            chooseGalleryImage(BlurActivity.this, REQUEST_CODE_CHOOSE_GALLERY_IMAGE);
                            break;
                        case R.id.tv_blur:

                            blur();
                            break;
                    }
                }
            };
        }
        return mOnClickListener;
    }

    private void blur() {

        if (mOriginalBitmap == null) {
            return;
        }

        final Bitmap willBlurBitmap ;
        if (mCompressCheckBox.isChecked()) {
            willBlurBitmap = mCompressedBitmap;
        } else {
            willBlurBitmap = mOriginalBitmap;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);

        new AsyncTask<Void,Void,Bitmap>(){

            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                long blurStartTime = System.currentTimeMillis();
                Bitmap blurBitmap = null;
                switch (BlurMode) {
                    case JAVA_BLUR:
                        Log.i("robin","BlurMode:JAVA");
                        blurBitmap = StackBlur.blurJava(willBlurBitmap, blurRadius, false);
                        break;
                    case JNI_BITMAP_BLUR:
                        Log.i("robin","BlurMode:JNI_BITMAP");
                        blurBitmap = StackBlur.blurNatively(willBlurBitmap, blurRadius, false);
                        break;
                    case JNI_PIXELS_BLUR:
                        Log.i("robin","BlurMode:JNI_PIXELS");
                        blurBitmap = StackBlur.blurNativelyPixels(willBlurBitmap, blurRadius, false);
                        break;
                    case RENDER_SCRIPT_BLUR:
                        Log.i("robin","BlurMode:RENDER_SCRIPT");
                        blurBitmap = StackBlur.blurRenderScript(BlurActivity.this,willBlurBitmap, blurRadius, false);
                        break;
                }
                final long blurTime = System.currentTimeMillis() - blurStartTime;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBlurTimeTextView.setText("blur time: "+blurTime+" ms");
                    }
                });
                return blurBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                progressDialog.dismiss();
                mBlurImageView.setImageBitmap(bitmap);
            }
        }.execute();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_GALLERY_IMAGE:
                if (data == null) {
                    return;
                }
                clearImageView();

                String picPath = chooseGalleryImageHandler(BlurActivity.this, data);
                Log.i("robin","PicPath:"+picPath);

                // Original
                mOriginalBitmap = BitmapFactory.decodeFile(picPath);
                // Compress
                /*Matrix matrix = new Matrix();
                matrix.postScale(1.0f / 6, 1.0f / 6);
                mCompressedBitmap = Bitmap.createBitmap(mOriginalBitmap, 0, 0,
                        mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight(), matrix, true);*/
                mCompressedBitmap = BlurUtils.compressBitmap(mOriginalBitmap,6);

                mOriginalImageView.setImageBitmap(mOriginalBitmap);
                break;
        }
    }

    public void clearImageView() {
        mOriginalImageView.setImageBitmap(null);
        mBlurImageView.setImageBitmap(null);
    }

    public static void chooseGalleryImage(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public static String chooseGalleryImageHandler(
            Context context, Intent data) {
        if (data == null) {
            return null ;
        }
        Uri uri = data.getData();
        String filePath;
        if (uri.toString().substring(0, 4).equals("file")) {
            filePath = uri.getPath();
        } else {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(columnIndex);
        }
        return filePath;
    }

    public static class SpinnerAdapter extends BaseAdapter{

        private List<String> mStrings;

        public SpinnerAdapter(List<String> strings) {
            mStrings = strings;
        }

        @Override
        public int getCount() {
            return mStrings.size();
        }

        @Override
        public Object getItem(int i) {
            return mStrings.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = View.inflate(viewGroup.getContext(), R.layout.item_spinner, null);
                viewHolder.mTextView = (TextView) view.findViewById(R.id.tv);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.mTextView.setText(mStrings.get(i));

            return view;
        }

        class ViewHolder{
            TextView mTextView;
        }
    }


}
