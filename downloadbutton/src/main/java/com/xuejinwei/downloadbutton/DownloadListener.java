package com.xuejinwei.downloadbutton;

/**
 * Created by xuejinwei on 2017/12/27.
 * Email:xuejinwei@outlook.com
 */

public interface DownloadListener {

    void onStart();// 开始下载

    void onPause();// 暂停下载

    void onContinue();// 继续下载

    void onStop();// 停止下载

}
