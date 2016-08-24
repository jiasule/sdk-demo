package com.example.knownsec.halleysdkdemo;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tencent.halley.HalleyAgent;
import com.tencent.halley.downloader.*;
import com.tencent.halley.scheduler.AccessScheduler;
import com.tencent.halley.scheduler.access.stroage.AccessIP;
import com.tencent.halley.UserActionCallback;
import com.tencent.beacon.event.UserAction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//自定义下载状态
class GlobalDefine{
    public enum DownloadState{
        STATE_PREPARE,
        STATE_START,
        STATE_PAUSE,
        STATE_FAILED,
        STATE_FINIS,
        STATE_ERROR
    }
}

public class TaskDownloadMgr implements DownloaderTaskListener {
    private TaskDownloadMgr(){}
    //App Type
    public static final String tag = "HalleyDemo_DlMgr";

    public final int appType = 1;
    //App ID
    public final String appId = "halley_sdk_demo";

    public  String saveDir = null;

    public  String saveName = null;

    public   Context          mainContext = null;

    public   TaskViewAdapter  viewAdapter = null;

    private  Downloader       dloader = null;

    private  Handler          handler = null;

    private  LinkedHashMap<String,Object> mapTaskInfo = new LinkedHashMap<String, Object>();

    private  HashMap<String, Object>      mapObserver = new HashMap<String, Object>();

    //Public
    public enum AddTaskState{
        ADD_TASK_OKAY,
        ADD_TASK_EXIST,
        ADD_TASK_FAILED,
    };

    // Create singleton object
    private static final TaskDownloadMgr single = new TaskDownloadMgr();

    public static TaskDownloadMgr getInstance() {
        return single;
    }


    //Initial Halley SDK
    public void initialHalleySDK(Context context, TaskViewAdapter adapter){

        try{
            mainContext = context;
            viewAdapter = adapter;

            //接入灯塔
            UserAction.initUserAction(context);

            //如果app本身接入了灯塔，请用带灯塔分配的ID，bugReportID填写app本身的灯塔接入ID
            //如果app本身未接入了灯塔，请用的带有灯塔组件的SDK，则填写“0M100WJ33N1CQ08O”
            //String bugReportId = "0M100WJ33N1CQ08O"; //默认的SDK ID
            String bugReportId = "1VW1200WIV1GK3FV";  //该ID为app申请的应用ID
            String channelId = "100001"; //自定义
            String uuid = "user_id"; //自定义
            List<String> domainnameList = null; //下载模块不需要该参数

            HalleyAgent.init(context, bugReportId, channelId, uuid, domainnameList);

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        handler = new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        return;
    }


    public void updateView(DownloaderTask dltask){
        //call back detail activity
        try{
            if (dltask != null){
                DLObserverInterface observer = (DLObserverInterface)mapObserver.get(dltask.getUrl());
                if (observer != null){
                    observer.onDLEvent(dltask.getUrl(), dltask);
                }
            }
        }
        catch (Exception e){
            Log.e(tag, e.getMessage());
        }

        //call back main activity
        //In fact, if the list size is large, updating single list item is recommended!
        //This Demo is just a test, and update the entire view
        List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
        Iterator iter = mapTaskInfo.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Map<String, Object> detailInfo = (Map<String, Object>) entry.getValue();
            listData.add(detailInfo);
        }
        viewAdapter.updateData(listData);
        viewAdapter.notifyDataSetChanged();

        return;
    }

    public DownloaderTask getTaskbyUrl(String dlUrl){
        if (!mapTaskInfo.containsKey(dlUrl)){
            Log.w(tag, "Get task failed! [in] url:" + dlUrl);
            return null;
        }

        try{
            Map<String, Object> detailInfo = (Map<String, Object>)mapTaskInfo.get(dlUrl);
            return (DownloaderTask)detailInfo.get("taskobj");
        }
        catch (Exception e){
            Log.e(tag, e.getMessage());
        }

        return null;
    }


    public void registerObserver(String url, DLObserverInterface obs){
        if (mapObserver.containsKey(url)){
            Log.i(tag, "Observer already exist!");
            return;
        }

        mapObserver.put(url, obs);

        return;
    }


    public void unRegisterObserver(String url){
        if (mapObserver.containsKey(url)){
            mapObserver.remove(url);
            Log.i(tag, "Observer is removed!");
        }

        return;
    }

    public AddTaskState addTask(String downloadUrl, int taskID){
        if (downloadUrl == null || downloadUrl.length() < 2){
            Log.e(tag, "Input parameters are illegal!");
            return AddTaskState.ADD_TASK_FAILED;
        }

        String formatUrl = downloadUrl.trim();

        if (mapTaskInfo.containsKey(formatUrl)){
            return AddTaskState.ADD_TASK_EXIST;
        }

        DownloaderTask   dlTask = null;

        try{
            //获取下载器
            if (dloader == null) {
                dloader = DownloaderFactory.getDownloader();
                //设置各种类型的下载任务的并行下载数
                // Cate_DefaultEase类型默认值为3，其余默认值为2
                dloader.setTaskNumForCategory(DownloaderTaskCategory.Cate_DefaultMass, 3);
                dloader.setTaskNumForCategory(DownloaderTaskCategory.Cate_DefaultEase, 2);
                dloader.setTaskNumForCategory(DownloaderTaskCategory.Cate_CustomMass1, 2);
                dloader.setTaskNumForCategory(DownloaderTaskCategory.Cate_CustomMass1, 2);
            }

            List<String> cdnIpUrls = null;//下载业务，填写null即可
            List<String> srcIpUrls = null;//下载业务，填写null即可
            //strSrcUrl: 当云存储下载失败，会回源处理,也可以赋值null
            String strSrcUrl = "http://www.yuaaq.com/download/test_demo.apk";

            //下载保存目录，可以自定义 ，填写null为默认路径:"getExternalStorageDirectory()/package.name/HalleySdk/"
            saveDir = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" + mainContext.getPackageName() + File.separator + "MyDownload" ;

            //设置保存的文件名字，默认为null，sdk会根据http链接确定名字，通过接口getRealSaveName()获取真实存储名字。
            String saveName = "Sdk_demo.apk";
            long   knownSize  = 589824;

            if (!formatUrl.contains("HalleySdkDemo.apk")){
                knownSize = -1;
                saveName = null;
            }

            dlTask = dloader.createNewTask(appType, appId, formatUrl, saveDir, saveName, this, knownSize);

            //设置回源URL，必须再addNewTask这个接口之前调用
            dlTask.setBakUrl(strSrcUrl);

            //设置任务的类型，必须再addNewTask这个接口之前调用
            dlTask.setCategory(DownloaderTaskCategory.Cate_DefaultMass);

            //将任务添加到队列，
            dloader.addNewTask(dlTask);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return AddTaskState.ADD_TASK_FAILED;
        }

        Map<String, Object> detailInfo  = new HashMap<String, Object>();
        detailInfo.put("taskid", taskID);
        detailInfo.put("taskobj", dlTask);
        detailInfo.put("url", formatUrl);

        Log.w(tag, "New Created Task, dlTask addr is :" + dlTask.toString());

        mapTaskInfo.put(formatUrl, detailInfo);

        updateView(dlTask);

        /* test beacon */
        Map<String, String> map = new HashMap<String, String>();
        map.put("testKey", "testValue");
        if (false == UserAction.onUserAction("test_common_event", true, -1, -1, map,
                false)){
            Log.e(tag, "report failed!");
        }

        return AddTaskState.ADD_TASK_OKAY;
    }


    public void deleteTask(String url) {
        if (!mapTaskInfo.containsKey(url)){
            return;
        }

        Map<String, Object> taskInfo = (Map<String, Object>)mapTaskInfo.get(url);
        DownloaderTask dlTask = (DownloaderTask)taskInfo.get("taskobj");

        //delete Task and its download file cache
        dloader.deleteTask(dlTask,true);
        mapTaskInfo.remove(url);
        updateView(null);
    }


    public void resumeTask(String url) {
        if (!mapTaskInfo.containsKey(url)){
            return;
        }

        Map<String, Object> taskInfo = (Map<String, Object>)mapTaskInfo.get(url);

        final DownloaderTask  dlTask = (DownloaderTask)taskInfo.get("taskobj");

        Log.w(tag, "Resume Task, dlTask addr is :" + dlTask.toString());

        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    if( Thread.currentThread() == Looper.getMainLooper().getThread()){
                        Log.w(tag, "In Main Thread!");
                    }
                    dlTask.resume();
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    //
    public void pasueTask(String url){
        if (!mapTaskInfo.containsKey(url)){
            return;
        }
        Map<String, Object> taskInfo = (Map<String, Object>)mapTaskInfo.get(url);
        DownloaderTask dlTask = (DownloaderTask)taskInfo.get("taskobj");

        Log.w(tag, "Pause Task, dlTask addr is :" + dlTask.toString());

        dlTask.pause();
    }

    // the following call back in main thread
    @Override
    public void onTaskPendingMainloop(DownloaderTask var1){
        //main thread
        updateView(var1);
        Log.w(tag, "main thread :onTaskPendingMainloop!");
    }

    @Override
    public void onTaskStartedMainloop(DownloaderTask var1){
        updateView(var1);
        Log.w(tag, "main thread :onTaskStartedMainloop!");
    }

    @Override
    public void onTaskDetectedMainloop(DownloaderTask var1){
        //updateView();
        Log.w(tag, "main thread :onTaskDetectedMainloop!");
    }

    @Override
    public void onTaskReceivedMainloop(DownloaderTask var1){
        updateView(var1);
        Log.w(tag, "main thread :onTaskReceivedMainloop!");
    }

    @Override
    public void onTaskPausedMainloop(DownloaderTask var1){
        Log.w(tag, "main thread :onTaskPausedMainloop Task, call back dlTask addr is :" + var1.toString());

        updateView(var1);
    }

    @Override
    public void onTaskFailedMainloop(DownloaderTask var1){
        int errorCode = var1.getFailCode();
        updateView(var1);
        Log.w(tag, "main thread :onTaskFailedMainloop, error code =" + errorCode);
    }

    @Override
    public void onTaskCompletedMainloop(DownloaderTask var1){
        updateView(var1);
        Log.w(tag, "main thread :onTaskCompletedMainloop");
    }

    //the following is callback in sub thread
    @Override
    public void onTaskStartedSubloop(DownloaderTask var1){
        Log.w(tag, "sub thread :onTaskStartedSubloop");
    }

    @Override
    public void onTaskDetectedSubloop(DownloaderTask var1){
        Log.w(tag, "sub thread :onTaskDetectedSubloop");
    }

    @Override
    public void onTaskReceivedSubloop(DownloaderTask var1){
        Log.w(tag, "sub thread :onTaskReceivedSubloop");
    }

    @Override
    public void onTaskPausedSubloop(DownloaderTask var1){
        Log.w(tag, "sub thread :onTaskPausedSubloop");
        final DownloaderTask task = var1;
        /*handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    task.resume();
                }
                catch (DownloaderAddTaskException exception){
                    System.out.println(exception.getMessage());
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });*/
    }

    @Override
    public void onTaskFailedSubloop(DownloaderTask var1){
        int errorCode = var1.getFailCode();
        Log.w(tag, "sub thread :onTaskFailedSubloop" + errorCode);
    }

    @Override
    public void onTaskCompletedSubloop(DownloaderTask var1){
        Log.w(tag, "sub thread :onTaskCompletedSubloop");
    }
}
