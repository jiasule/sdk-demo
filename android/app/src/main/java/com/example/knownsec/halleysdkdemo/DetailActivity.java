package com.example.knownsec.halleysdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.example.knownsec.halleysdkdemo.*;
import com.tencent.halley.downloader.DownloaderTask;

public class DetailActivity extends AppCompatActivity implements DLObserverInterface {

    private final String tag = "Halley_DetailActivity";

    private String downloadUrl = null;

    public TextView dlUrlTextView       = null;
    public TextView savePathTextView    = null;
    public TextView taskIDTextView      = null;
    public TextView fileNameTextView    = null;
    public TextView fileSizeTextView    = null;
    public TextView dlSizeTextview      = null;
    public TextView timeCostTextView    = null;
    public TextView curRateTextView     = null;
    public TextView avgRateTextView     = null;
    public TextView curStateTextView    = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        downloadUrl = intent.getStringExtra("dlurl");
        DownloaderTask dlTask = TaskDownloadMgr.getInstance().getTaskbyUrl(downloadUrl);

        setContentView(R.layout.activity_detail);

        dlUrlTextView       = (TextView)findViewById(R.id.dlurl_text);
        savePathTextView    = (TextView)findViewById(R.id.savepath_text);
        taskIDTextView      = (TextView)findViewById(R.id.taskid_text);
        fileNameTextView    = (TextView)findViewById(R.id.filename_text);
        fileSizeTextView    = (TextView)findViewById(R.id.filesize_text);
        dlSizeTextview      = (TextView)findViewById(R.id.dlsize_text);
        timeCostTextView    = (TextView)findViewById(R.id.timecost_text);
        curRateTextView     = (TextView)findViewById(R.id.currate_text);
        avgRateTextView     = (TextView)findViewById(R.id.avgrate_text);
        curStateTextView    = (TextView)findViewById(R.id.taskstate_text);

        onUpdateUI(dlTask);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TaskDownloadMgr.getInstance().registerObserver(downloadUrl, this);
    }

    @Override
    protected void onDestroy(){
        TaskDownloadMgr.getInstance().unRegisterObserver(downloadUrl);
        super.onDestroy();
    }

    public void onUpdateUI(DownloaderTask dlTask){
        if (dlTask == null){
            Log.e(tag, "dlTask == null!");
            return;
        }

        dlUrlTextView.setText(downloadUrl);

        savePathTextView.setText(dlTask.getSaveDir());

        String id = dlTask.getId();

        taskIDTextView.setText(id);

        fileNameTextView.setText(dlTask.getRealSaveName());

        fileSizeTextView.setText(dlTask.getTotalLength() + " Bytes");

        dlSizeTextview.setText(dlTask.getReceivedLength()+ " Bytes");

        timeCostTextView.setText(dlTask.getCostTime() +" ms");

        curRateTextView.setText(dlTask.getRealTimeSpeed() + " Bps");

        avgRateTextView.setText(dlTask.getAverageSpeed() + " Bps");

        boolean isRunning  = dlTask.isRunning();
        boolean isWaiting  = dlTask.isWaiting();
        boolean isPausing  = dlTask.isPaused();
        boolean isComplete = dlTask.isCompleted();
        boolean isFailed = dlTask.isFailed();

        if (isRunning){
            curStateTextView.setText(R.string.running);
        }
        else if (isWaiting){
            curStateTextView.setText(R.string.wait);
        }
        else if (isPausing)
        {
            curStateTextView.setText(R.string.pause);
        }
        else if (isComplete){
            curStateTextView.setText(R.string.complete);
        }
        else if (isFailed) {
            String text     = getString(R.string.failed);
            int errCode     = dlTask.getFailCode();
            String errInfo  = dlTask.getFailInfo();
            curStateTextView.setText(text + " errCode:( " + errCode + " )" + errInfo);
        }
        else{
            curStateTextView.setText(R.string.error);
        }
    }

    public void onDLEvent(String url, DownloaderTask dlTask){
        if (!url.equalsIgnoreCase(downloadUrl)){
            Log.i(tag, "not target url, return");
            return;
        }

        onUpdateUI(dlTask);
    }
}
