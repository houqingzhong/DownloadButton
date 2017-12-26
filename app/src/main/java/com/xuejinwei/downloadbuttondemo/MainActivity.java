package com.xuejinwei.downloadbuttondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xuejinwei.downloadbutton.DownloadButton;

public class MainActivity extends AppCompatActivity {

    private DownloadButton mDownloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 101; i++) {
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mDownloadButton.setProgress(i);
                        }

                    }
                }).start();
            }
        });

    }

    private void initView() {
        mDownloadButton = (DownloadButton) findViewById(R.id.download_button);
    }
}
