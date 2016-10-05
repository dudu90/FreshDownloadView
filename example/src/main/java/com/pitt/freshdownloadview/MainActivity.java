package com.pitt.freshdownloadview;


import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pitt.library.fresh.FreshDownloadView;


public class MainActivity extends Activity implements View.OnClickListener {

    private FreshDownloadView freshDownloadView;
    private Button btDownloaded;
    private TextView btReset;
    private TextView btDownloadError;
    private final int FLAG_SHOW_OK = 10;
    private final int FLAG_SHOW_ERROR = 11;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int progress = (int) msg.obj;
            freshDownloadView.upDateProgress(progress);
            switch (msg.what) {
                case FLAG_SHOW_OK:
                    break;
                case FLAG_SHOW_ERROR:
                    freshDownloadView.showDownloadError();
                    break;
            }
        }
    };

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        freshDownloadView = (FreshDownloadView) findViewById(R.id.pitt);
        btDownloaded = (Button) findViewById(R.id.bt_downloaded);
        btReset = (Button) findViewById(R.id.bt_reset);
        btDownloadError = (Button) findViewById(R.id.bt_download_error);
        btDownloaded.setOnClickListener(this);
        btReset.setOnClickListener(this);
        btDownloadError.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_downloaded:
                if (freshDownloadView.using()) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i <= 100; i++) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Message message = Message.obtain();
                            message.obj = i;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
                break;
            case R.id.bt_reset:
                freshDownloadView.reset();
                break;
            case R.id.bt_download_error:
                if (freshDownloadView.using()) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i <= 30; i++) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Message message = Message.obtain();
                            if (i == 30) {
                                message.what = FLAG_SHOW_ERROR;
                            }
                            message.obj = i;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
                break;
        }
    }
}
