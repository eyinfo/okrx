package io.github.eyinfo.okrx;

import com.eyinfo.android_pure_utils.events.Action1;

import java.io.File;

import io.github.eyinfo.okrx.enums.RequestState;
import io.github.eyinfo.okrx.events.OnDownloadProgressAction;
import io.github.eyinfo.okrx.events.OnDownloadSuccessAction;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2019-06-11
 * Description:文件下载管理
 * Modifier:
 * ModifyContent:
 */
public class DownloadManager {

    private static volatile DownloadManager downloadManager;

    private DownloadManager() {
        //init
    }

    //get instance
    public static DownloadManager getInstance() {
        if (downloadManager == null) {
            synchronized (DownloadManager.class) {
                if (downloadManager == null) {
                    downloadManager = new DownloadManager();
                }
            }
        }
        return downloadManager;
    }

    /**
     * 文件下载
     *
     * @param url            下载url
     * @param targetFile     下载后保存文件
     * @param progressAction 下载进度
     * @param successAction  下载成功
     * @param extras         扩展数据
     */
    public void download(String url, final File targetFile, OnDownloadProgressAction progressAction, final OnDownloadSuccessAction successAction, Object... extras) {
        OnDownloadSuccessAction downloadSuccessAction = new OnDownloadSuccessAction() {
            @Override
            public void call(File targetFile) {
                if (successAction == null) {
                    return;
                }
                successAction.setExtras(super.getExtras());
                if (targetFile.exists() && targetFile.length() > 0) {
                    //文件存在且不为空
                    successAction.call(targetFile);
                } else {
                    successAction.failure(targetFile);
                }
            }
        };
        downloadSuccessAction.setExtras(extras);
        OkRxManager.getInstance().download(url, null, null, targetFile, progressAction, downloadSuccessAction, new Action1<RequestState>() {
            @Override
            public void call(RequestState requestState) {
                if (requestState == RequestState.Error && successAction != null) {
                    successAction.failure(targetFile);
                }
            }
        });
    }

    /**
     * 文件下载
     *
     * @param url           下载url
     * @param targetFile    下载后保存文件
     * @param successAction 下载成功
     * @param extras        扩展数据
     */
    public void download(String url, File targetFile, OnDownloadSuccessAction successAction, Object... extras) {
        download(url, targetFile, null, successAction, extras);
    }
}
