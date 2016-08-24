package com.example.knownsec.halleysdkdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String    tag = "HalleyDemo_MainActivity";

    private ListView        listView = null;

    private TaskViewAdapter listViewAdapter = null;

    private EditText        editText = null;

    private int             taskIndex = 0;

    //for convenience, test urls are put into editText one by one.
    private List<String>    listTestUrls  = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialTestUrl();

        Button btnAdd = (Button)findViewById(R.id.add_task);
        btnAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String strUrl = editText.getText().toString();
                TaskDownloadMgr.AddTaskState ret = TaskDownloadMgr.getInstance().addTask(strUrl,taskIndex);

                if ( TaskDownloadMgr.AddTaskState.ADD_TASK_EXIST == ret){
                    Log.w(tag, "Task added is already in the list!");
                    Toast.makeText(MainActivity.this, "Task added is already in the list!", Toast.LENGTH_SHORT).show();
                }
                else if ( TaskDownloadMgr.AddTaskState.ADD_TASK_FAILED == ret){
                    Log.w(tag, "Add task failed!");
                    Toast.makeText(MainActivity.this, "Add task failed!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    taskIndex++;
                    editText.setText("");
                    editText.setText(listTestUrls.get(taskIndex%listTestUrls.size()));
                }
            }
        });

        final ImageView imgView = (ImageView)findViewById(R.id.search_iv_delete);
        imgView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });


        editText = (EditText)findViewById(R.id.task_edit);
        editText.setText(listTestUrls.get(taskIndex%listTestUrls.size()));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    imgView.setVisibility(View.VISIBLE);
                }
                else{
                    imgView.setVisibility(View.INVISIBLE);
                }
            }
        });

        listView = (ListView)findViewById(R.id.tasklistview);
        try{
            List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();

            if (listViewAdapter == null){
                listViewAdapter = new TaskViewAdapter(this, listData);
            }

            TaskDownloadMgr.getInstance().initialHalleySDK(this, listViewAdapter);

            listView.setAdapter(listViewAdapter);
        }
        catch(Exception e){
            System.out.println("Test!");
        }

       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       */
    }

    @Override
    protected void onResume(){
        super.onResume();
        TaskDownloadMgr.getInstance().updateView(null);
    }


    private void initialTestUrl(){
        listTestUrls.add("http://jsl.myapp.com/jsl/jsl00000002/HalleySdkDemo.apk");
        listTestUrls.add("http://imtt.dd.qq.com/16891/7F89221B2932061861794F2B42AA96A7.apk?fsname=com.xtuan.meijia_3.2.0_107.apk&amp;csr=4d5s");
        listTestUrls.add("http://imtt.dd.qq.com/16891/5896350EF678994E890229976ADA6B60.apk?fsname=com.when.coco_6.4.1_1026.apk&amp;csr=4d5s");
        listTestUrls.add("http://imtt.dd.qq.com/16891/123FDF5A75BB9B384845B15EBBCC238E.apk?fsname=com.dianxinos.powermanager_4.3.0_1541.apk&amp;csr=4d5s");
        listTestUrls.add("http://imtt.dd.qq.com/16891/C58BC35BC9413536F826C16C4565380B.apk?fsname=com.mgyun.shua_3.4.1_72.apk&amp;csr=4d5s");
    }
}
