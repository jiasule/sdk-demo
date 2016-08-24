package com.example.knownsec.halleysdkdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.knownsec.halleysdkdemo.TaskDownloadMgr.*;
import com.tencent.halley.downloader.*;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TaskViewAdapter extends BaseAdapter {
    class ItemEventListener implements OnClickListener{
        public int position;

        public GlobalDefine.DownloadState dlState = GlobalDefine.DownloadState.STATE_ERROR;

        public ItemEventListener(int pos){
            position = pos;
        }

        public void setDlState(GlobalDefine.DownloadState state){
            dlState = state;
        }

        public void onClick(View v){
           int id = v.getId();
           switch (id){
               case R.id.cancel:
                   onClickCancelBtn();
                   break;
               case R.id.pause:
                   onClickPauseBtn();
                   break;
               case R.id.detail:
                   onClickDetailBtn();
                   break;
               default:
                   break;
           }
        }

        private void onClickPauseBtn(){
            try{
                String dlUrl = (String)listItemData.get(position).get("url");
                if (dlState == GlobalDefine.DownloadState.STATE_START){
                    TaskDownloadMgr.getInstance().pasueTask(dlUrl);
                }
                else if (dlState == GlobalDefine.DownloadState.STATE_PAUSE){
                    TaskDownloadMgr.getInstance().resumeTask(dlUrl);
                }
                else if (dlState == GlobalDefine.DownloadState.STATE_FINIS){
                    Intent intent = getFileIntent((DownloaderTask)listItemData.get(position).get("taskobj"));
                    if (intent != null){
                        context.startActivity(intent);
                    }
                    else{
                        Toast.makeText(context, "Successfully Download! Can not Install non-apk file!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (Exception e){

            }
        }

        private Intent getFileIntent(DownloaderTask dlTask) {
            try{
                String dlFilePath = dlTask.getSaveDir() + File.separator + dlTask.getRealSaveName();
                if (!dlFilePath.endsWith(".apk")){
                    return null;
                }
                File   file = new File(dlFilePath);
                Uri uri = Uri.fromFile(file);
                String type = "application/vnd.android.package-archive";
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uri, type);
                return intent;
            }
            catch (Exception e){
                System.out.print(e.getMessage());
            }
            return null;
        }

        private void onClickDetailBtn(){
           try {
                String dlUrl = (String)listItemData.get(position).get("url");
                Intent myIntent = new Intent();
                myIntent.putExtra("dlurl", dlUrl);
                myIntent.setClass(context, DetailActivity.class);
                context.startActivity(myIntent);
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

        private void onClickCancelBtn(){
            String dlUrl = (String)listItemData.get(position).get("url");
            TaskDownloadMgr.getInstance().deleteTask(dlUrl);
        }
    }

    private List<Map<String, Object>>       listItemData;
    private LayoutInflater                  layoutInflater;
    private Context                         context;

    public TaskViewAdapter(Context context,List<Map<String, Object>> data){
        this.context=context;
        this.listItemData=data;
        this.layoutInflater=LayoutInflater.from(context);
    }

    public void updateData(List<Map<String, Object>> data){
        listItemData = data;
    }

    @Override
    public int getCount() {
        return listItemData.size();
    }


    @Override
    public Object getItem(int position) {
        return listItemData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public final class ItemData{
        public ProgressBar progBar;
        public Button      btnPause;
        public Button      btnCancel;
        public Button      btnDetail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemData itemData = null;
        if(convertView==null){
            itemData = new ItemData();
            convertView = layoutInflater.inflate(R.layout.list_item, null);
            itemData.progBar = (ProgressBar)convertView.findViewById(R.id.prog_bar);
            itemData.btnPause = (Button)convertView.findViewById(R.id.pause);
            itemData.btnCancel = (Button)convertView.findViewById(R.id.cancel);
            itemData.btnDetail = (Button)convertView.findViewById(R.id.detail);
            convertView.setTag(itemData);
        }else{
            itemData=(ItemData)convertView.getTag();
        }

        //bind data with view
        ItemEventListener listener = new ItemEventListener(position);
        itemData.btnPause.setOnClickListener(listener);
        itemData.btnDetail.setOnClickListener(listener);
        itemData.btnCancel.setOnClickListener(listener);

        DownloaderTask dlTask = (DownloaderTask)listItemData.get(position).get("taskobj");

        boolean isRunning  = dlTask.isRunning();
        boolean isWaiting  = dlTask.isWaiting();
        boolean isPausing  = dlTask.isPaused();
        boolean isComplete = dlTask.isCompleted();
        boolean isFailed   = dlTask.isFailed();

        GlobalDefine.DownloadState currentState = GlobalDefine.DownloadState.STATE_ERROR;
        itemData.btnPause.setClickable(true);

        if (isRunning){
            itemData.btnPause.setText(R.string.pause);
            currentState = GlobalDefine.DownloadState.STATE_START;
        }
        else if (isWaiting){
            itemData.btnPause.setText(R.string.wait);
            currentState = GlobalDefine.DownloadState.STATE_PREPARE;
        }
        else if (isPausing)
        {
            itemData.btnPause.setText(R.string.resume);
            currentState = GlobalDefine.DownloadState.STATE_PAUSE;
        }
        else if (isComplete){
            itemData.btnPause.setText(R.string.open);
            currentState = GlobalDefine.DownloadState.STATE_FINIS;
        }
        else if (isFailed) {
            itemData.btnPause.setText(R.string.failed);
            currentState = GlobalDefine.DownloadState.STATE_FAILED;
            itemData.btnPause.setClickable(false);
        }
        else{
            itemData.btnPause.setText(R.string.error);
            currentState = GlobalDefine.DownloadState.STATE_ERROR;
            itemData.btnPause.setClickable(false);
        }

        listener.setDlState(currentState);

        try{
            int  percent  = dlTask.getPercentage();
            itemData.progBar.setProgress((int)percent);
            itemData.progBar.setContentDescription(""+percent);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        return convertView;
    }
}
