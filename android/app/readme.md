HalleySdk 几点注意事项
#使用流程
1. New android project.
2. Import Halley SDK lib.
3. Initial SDK.
4. Write a download listener class.
5. Create a download task.
6. Update UI according to the call back results.


#注意事项
1, Downloadlistener 有两套回调接口，名字比较像，一套是在主线程，一套是在子线程里面。
在调用UI的时候，UI更新代码别误添加到子线程的回调里面了。否则会有莫名的问题（android UI处理必须在主线程）。

2,同时下载任务数的设置是通过sdk的initial接口中的最后一个参数DownloadConfig来设置的。如果想并行下载，还要注意一个地方，即adddownloadtask接口，倒数第二个数”isease” 应设置为false。

3,sdk的默认存储路径是：外存/packagename/halleysdk/***.xx.

4,正在下载中的任务，如果调用了pause，不能直接在pause的回调中调用resume，需要通过handler过度到下个消息循环，sdk本身的问题。