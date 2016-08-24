package com.example.knownsec.halleysdkdemo;

import com.tencent.halley.downloader.*;


public interface DLObserverInterface {
    public void onDLEvent(String url, DownloaderTask dlTask);
}
