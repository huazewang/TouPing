package iHuaze.Yinao.Home;

import android.app.Application;

import com.hpplay.common.utils.LeLog;
import com.hpplay.sdk.source.api.IBindSdkListener;
import com.hpplay.sdk.source.api.LelinkSourceSDK;

import java.util.UUID;

/**
 * Created by Zippo on 2018/3/27.
 * Date: 2018/3/27
 * Time: 21:16:01
 */

public class MyApplication extends Application {

    private static final String APP_ID = "12609";
    private static final String APP_SECRET = "7acb9624686a5f67f7bea580faba14fb";

    @Override
    public void onCreate() {
        super.onCreate();
        LelinkSourceSDK.getInstance().bindSdk(MyApplication.this, APP_ID, APP_SECRET, new IBindSdkListener() {
            @Override
            public void onBindCallback(boolean b) {
                LeLog.i("onBindCallback", "--------->" + b);
            }
        });

    }

}
