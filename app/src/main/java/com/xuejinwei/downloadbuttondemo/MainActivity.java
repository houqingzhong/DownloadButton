package com.xuejinwei.downloadbuttondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xuejinwei.downloadbutton.DownloadButton;
import com.xuejinwei.downloadbutton.DownloadListener;

public class MainActivity extends AppCompatActivity {

    private DownloadButton mDownloadButton01;
    private DownloadButton mDownloadButton02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mDownloadButton01.setDownloadListener(new DownloadListener() {
            @Override
            public void onStart() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 101; i++) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mDownloadButton01.setProgress(i);
                        }

                    }
                }).start();
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onContinue() {

            }

            @Override
            public void onStop() {

            }
        });

        mDownloadButton02.setDownloadListener(new DownloadListener() {
            @Override
            public void onStart() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 101; i++) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mDownloadButton02.setProgress(i);
                        }

                    }
                }).start();
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onContinue() {

            }

            @Override
            public void onStop() {

            }
        });

    }

    private void initView() {
        mDownloadButton01 = (DownloadButton) findViewById(R.id.download_button01);
        mDownloadButton02 = (DownloadButton) findViewById(R.id.download_button02);
    }
}
