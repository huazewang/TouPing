package iHuaze.Yinao.Home.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.PermissionChecker;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

/**
 * 摄像头权限检测
 * author Zippo
 * created 2017/3/6 18:08
 */
public class CameraPermissionCompat {

    private static final String TAG = "CameraPermissionCompat";
    public static final int REQUEST_CODE_CAMERA = 999;
    private static OnCameraPermissionListener mOnCameraPermissionListener;

    /**
     * 检测摄像头权限 没有就会申请
     *
     * @param context
     * @param listener 申请权限的结果回调
     * @return
     */
    public static boolean checkCameraPermission(Context context, OnCameraPermissionListener listener) {
        mOnCameraPermissionListener = listener;
        boolean granted = true;
        // 魅族或者6.0以下
        if (isFlyme() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Camera mCamera = null;
            try {
                mCamera = Camera.open();
                // setParameters 是针对魅族MX5 做的。MX5 通过Camera.open() 拿到的Camera
                // 对象不为null
                Camera.Parameters mParameters = mCamera.getParameters();
                mCamera.setParameters(mParameters);
            } catch (Exception e) {
                Log.w(TAG, e);
                granted = false;
            }
            if (mCamera != null) {
                mCamera.release();
            }
        } else {
            granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
        }
        if (granted) {
            if (mOnCameraPermissionListener != null) {
                mOnCameraPermissionListener.onGrantResult(true);
            }
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }
        }
        return granted;
    }

    public static void onRequestPermissionsResult(Context context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (mOnCameraPermissionListener != null) {
                mOnCameraPermissionListener.onGrantResult(checkCameraPermission(context, null));
            }

        }
    }

    private static boolean isFlyme() {
        if (Build.BRAND.contains("Meizu")) {
            return true;
        } else {
            return false;
        }
    }

    public interface OnCameraPermissionListener {
        void onGrantResult(boolean granted);
    }
}
