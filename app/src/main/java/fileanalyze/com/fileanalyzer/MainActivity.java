package fileanalyze.com.fileanalyzer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements ShareActionProvider.OnShareTargetSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    private List<File> mFiles = new ArrayList<>();

    private int mTotalFiles = 0;

    private long mTotalSize = 0;

    private long mAverageFileSize = 0;

    private Map<String, Long> mFileSizeMap = new HashMap<>();

    private HashMap<String, Long> mSortedFileSizeMap;

    private Map<String, Integer> mExtMap = new HashMap<>();

    private HashMap<String, Integer> mSortedExtMap;

    private String mScanResult = null;

    private MyAsyncTask mMyAsyncTask = null;

    private View mProgressbarView;
    private View mContentView;
    private TextView mLargestFileResultTV;
    private TextView mAverageResultTV;
    private TextView mFrequentExtResultTV;

    private ShareActionProvider mShareActionProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressbarView = findViewById(R.id.progressbar_layout);
        mContentView = findViewById(R.id.scan_content);
        mLargestFileResultTV = (TextView) findViewById(R.id.largest_file_result);
        mAverageResultTV = (TextView) findViewById(R.id.average_file_result);
        mFrequentExtResultTV = (TextView) findViewById(R.id.frequent_ext_result);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("TASK_RUNNING", false)) {
                mMyAsyncTask = new MyAsyncTask();
                mMyAsyncTask.execute();
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar(true);
                setNotification();
                mMyAsyncTask = new MyAsyncTask();
                mMyAsyncTask.execute();
            }
        });

        FloatingActionButton stop = (FloatingActionButton) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMyAsyncTask != null && mMyAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mMyAsyncTask.cancel(true);
                    showProgressBar(false);
                    mContentView.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        return super.onCreateOptionsMenu(menu);
    }

    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, mScanResult);
            mShareActionProvider.setShareIntent(intent);
        }
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMyAsyncTask != null && mMyAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            outState.putSerializable("TASK_RUNNING", true);
        } else {
            outState.putSerializable("largestFileResult", mSortedFileSizeMap);
            outState.putSerializable("frequentExtResult", mSortedExtMap);
            outState.putLong("averageFileSize", mAverageFileSize);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getLong("averageFileSize") != 0) {
            mSortedFileSizeMap = (HashMap<String, Long>) savedInstanceState.getSerializable("largestFileResult");
            mSortedExtMap = (HashMap<String, Integer>) savedInstanceState.getSerializable("frequentExtResult");
            mAverageFileSize = savedInstanceState.getLong("averageFileSize");

            showScanResult();
        }
    }


    @Override
    public void onBackPressed() {
        if (mMyAsyncTask != null && mMyAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mMyAsyncTask.cancel(true);
            showProgressBar(false);
            mContentView.setVisibility(View.GONE);
        } else {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private void analyzeFiles(List<File> files) {

        for (File file : files) {
            long fileSize = file.length();
            mFileSizeMap.put(file.getName(), file.length());
            mTotalSize += fileSize;
            mTotalFiles++;
            addToFrequencyMap(Utils.getExtension(file));
        }

        mAverageFileSize = mTotalSize / mTotalFiles;
        mSortedFileSizeMap = Utils.sortByValue(mFileSizeMap);
        mSortedExtMap = Utils.sortByValue(mExtMap);
    }

    private void addToFrequencyMap(String extension) {
        if (mExtMap.containsKey(extension)) {
            mExtMap.put(extension, mExtMap.get(extension) + 1);
        } else {
            mExtMap.put(extension, 1);
        }
    }

    class MyAsyncTask extends AsyncTask<String, Void, String> {

        public MyAsyncTask() {
            super();
        }

        @Override
        protected String doInBackground(String... params) {
            mFiles = Utils.getFileFromPath(FILE_PATH);
            analyzeFiles(mFiles);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showScanResult();
            showProgressBar(false);
        }
    }

    private void showProgressBar(boolean isToShow) {
        mProgressbarView.setVisibility(isToShow ? View.VISIBLE : View.GONE);
        mContentView.setVisibility(isToShow ? View.GONE : View.VISIBLE);
    }

    private void setNotification() {
        Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setContentTitle(getResources().getString(R.string.notification))
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getResources().getString(R.string.notification)))
                        .setContentText(getResources().getString(R.string.notification));
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void showScanResult() {
        String largestFileResult = "";
        String frequentExtResult = "";

        int i = 0;
        int j = 0;

        for (Map.Entry<String, Long> entry : mSortedFileSizeMap.entrySet()) {
            if (++i > 10) {
                break;
            }
            largestFileResult += entry.getKey() + Const.FIELD_SEPERATOR + entry.getValue() + Const.POST_FIX;
        }

        for (Map.Entry<String, Integer> entry : mSortedExtMap.entrySet()) {
            if (++j > 5) {
                break;
            }
            frequentExtResult += entry.getKey() + Const.FIELD_SEPERATOR + entry.getValue() + Const.NEW_LINE;
        }

        mScanResult = largestFileResult + mAverageFileSize + Const.POST_FIX + frequentExtResult;
        setShareIntent();

        mLargestFileResultTV.setText(largestFileResult);
        mAverageResultTV.setText(mAverageFileSize + Const.POST_FIX);
        mFrequentExtResultTV.setText(frequentExtResult);
    }
}
