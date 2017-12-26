package com.xuejinwei.downloadbuttondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xuejinwei.downloadbutton.DownloadButton;

public class MainActivity extends AppCompatActivity {

    private DownloadButton mDownloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        mDownloadButton = (DownloadButton) findViewById(R.id.download_button);
    }
}
