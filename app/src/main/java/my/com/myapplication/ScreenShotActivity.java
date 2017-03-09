package my.com.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/8.
 */

public class ScreenShotActivity extends AppCompatActivity {
    MediaProjectionManager mediaProjectionManager;
    MediaProjection mediaProjection;
    ImageReader mImageReader;
    VirtualDisplay virtualDisplay1;
    DisplayMetrics displayMetrics;
    WindowManager windowManager;
    Boolean screenState = false;
    View view = this.getWindow().getDecorView().getRootView();

    public void createScreenShot() {
        checkPermission();
        requestCapturePermission();
    }

    public void requestCapturePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            return;
        }

        mediaProjectionManager = (MediaProjectionManager)
                getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        captureScreen();
    }

    public void checkPermission() {
        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionsList.size() != 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        100);
            } else {
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED)) {
            }
        }///instant-run
    }

    public void captureScreen() {
        displayMetrics = new DisplayMetrics();
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mImageReader = ImageReader.newInstance(view.getWidth(), view.getHeight(), PixelFormat.RGBA_8888, 2);
        virtualDisplay1 = mediaProjection.createVirtualDisplay("screen-mirror", view.getWidth(), view.getHeight(), displayMetrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
        mediaProjectionManager = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Image image = mImageReader.acquireLatestImage();
                if (image != null) {
                    final Image.Plane[] planes = image.getPlanes();
                    final ByteBuffer buffer = planes[0].getBuffer();
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;
                    Bitmap bmp = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                    bmp.copyPixelsFromBuffer(buffer);
                    Bitmap bitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 2, bmp.getHeight() / 2, false);
                    setSavePic(bitmap);
                    bitmap.recycle();
                    image.close();
                } else {
                    Log.e("Tag", "captureScreen: " + " image为空");
                }
            }
        }, 100);
    }

    private void setSavePic(Bitmap bitmap) {//保存截屏到本地
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/截屏";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            File file1 = new File(file, System.currentTimeMillis() + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file1);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                Toast.makeText(this, "截屏已保存到本地" + path, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file1);
                intent.setData(uri);
                sendBroadcast(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
