package com.xuejinwei.downloadbuttondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.xuejinwei.downloadbutton.DownloadButton;
import com.xuejinwei.downloadbutton.DownloadListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private DownloadButton mDownloadButton01;
    private DownloadButton mDownloadButton02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        final DownloadThread downloadThread01 = new DownloadThread(mDownloadButton01);
        final DownloadThread downloadThread02 = new DownloadThread(mDownloadButton02);
        mDownloadButton01.setDownloadListener(new DownloadListener() {
            @Override
            public void onStart() {
                downloadThread01.start();
                downloadThread01.setPause(false);
            }

            @Override
            public void onPause() {
                downloadThread01.setPause(true);
            }

            @Override
            public void onContinue() {
                downloadThread01.setPause(false);
            }

            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "下载完成，模拟安装", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRetry() {

            }
        });

        mDownloadButton02.setDownloadListener(new DownloadListener() {
            @Override
            public void onStart() {
                downloadThread02.start();
                downloadThread02.setPause(false);
            }

            @Override
            public void onPause() {
                downloadThread02.setPause(true);
            }

            @Override
            public void onContinue() {
                downloadThread02.setPause(false);
            }

            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "下载完成，模拟打开", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRetry() {

            }
        });

    }

    private void initView() {
        mDownloadButton01 = (DownloadButton) findViewById(R.id.download_button01);
        mDownloadButton02 = (DownloadButton) findViewById(R.id.download_button02);
    }

    /**
     * 简单模拟断点下载
     */
    private class DownloadThread extends Thread {

        public void setPause(boolean pause) {
            isPause = pause;
        }

        boolean isPause = true;// 是否暂停,默认暂停状态
        DownloadButton downloadButton;

        public DownloadThread(DownloadButton downloadButton) {
            this.downloadButton = downloadButton;
        }

        @Override
        public void run() {
            int progress = 0;

            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isPause) {
                    progress=progress+new Random().nextInt(6);
                    downloadButton.setProgress(progress);
                    if (progress >= 100) {
                        downloadButton.setComplete();//跟新按钮状态，设置为完成
                        return;
                    }
                }
            }

        }
    }

}
