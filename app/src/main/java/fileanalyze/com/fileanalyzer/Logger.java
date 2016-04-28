package fileanalyze.com.fileanalyzer;

import android.util.Log;

public class Logger {

    public static void v(String TAG, String message) {
        if (Const.DEBUG_MODE) {
            Log.v(TAG, message);
        }
    }

    public static void d(String TAG, String message) {
        if (Const.DEBUG_MODE) {
            Log.v(TAG, message);
        }
    }

    public static void w(String TAG, String message) {
        if (Const.DEBUG_MODE) {
            Log.v(TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        if (Const.DEBUG_MODE) {
            Log.v(TAG, message);
        }
    }
}
