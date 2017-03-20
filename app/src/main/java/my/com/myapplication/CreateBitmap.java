package my.com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Administrator on 2017/3/16.
 */

public class CreateBitmap {
    private static MediaProjection mediaProjection = MyApplication.mediaProjection;
    private static ImageReader mImageReader;
    private static VirtualDisplay virtualDisplay1;
    private static DisplayMetrics displayMetrics = new DisplayMetrics();
    private static WindowManager windowManager;
    private static Bitmap bmp, bitmap;
    private static Image image;
    private static int width;
    private static int height;
    private static int pixelStride;
    private static int rowStride;
    private static int rowPadding;
    private static String path, name;
    private static int screenwidth, screenheight;
    private static Image.Plane[] planes;
    static ByteBuffer buffer;

    private static class singleHolder {
        static final CreateBitmap createbitmap = new CreateBitmap();
    }

    public static CreateBitmap getInstance(final Context context, final Boolean isSave, String inpath, String inname) {
        init(context, inpath, inname);
        if (image != null) {
            analysesImage();
            long time = System.currentTimeMillis() - MyApplication.time;
            Toast.makeText(context, "截图已生成" + time, Toast.LENGTH_SHORT).show();
            if (isSave)
                savePic(context);
        }
        return singleHolder.createbitmap;
    }

    private static void loop() {
        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (image != null);

    }

    //    初始化操作
    private static void init(final Context context, String inpath, String inname) {
        if (bitmap != null) {
            bitmap = null;
        }
        path = inpath;
        name = inname;
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenwidth = displayMetrics.widthPixels;
        screenheight = displayMetrics.heightPixels;
        mImageReader = ImageReader.newInstance(screenwidth, screenheight, PixelFormat.RGBA_8888, 2);
        virtualDisplay1 = mediaProjection.createVirtualDisplay("screen-mirror", screenwidth, screenheight, displayMetrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
        do {
            try {
                Thread.sleep(10);
                image = mImageReader.acquireLatestImage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (image == null);
    }

    //解析image
    private static void analysesImage() {
        planes = image.getPlanes();
        buffer = planes[0].getBuffer();
        width = image.getWidth();
        height = image.getHeight();
        pixelStride = planes[0].getPixelStride();
        rowStride = planes[0].getRowStride();
        rowPadding = rowStride - pixelStride * width;
        bmp = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);
        image.close();
    }

    //保存图片
    private static void savePic(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && bitmap != null) {
            if (path == null) {
                path = Environment.getExternalStorageDirectory().getPath() + "/截屏";
            } else {
                path = Environment.getExternalStorageDirectory().getPath() + path;
            }
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            if (name == null) {
                name = System.currentTimeMillis() + "";
            }
            File file1 = new File(file, name + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file1);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                Toast.makeText(context, "截屏已保存到本地" + path, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file1);
                intent.setData(uri);
                context.sendBroadcast(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
