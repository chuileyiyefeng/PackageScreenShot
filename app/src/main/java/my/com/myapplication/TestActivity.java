package my.com.myapplication;

import android.os.Bundle;

/**
 * Created by Administrator on 2017/3/8.
 */

public class TestActivity extends ScreenShotActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        createScreenShot();
    }
}
