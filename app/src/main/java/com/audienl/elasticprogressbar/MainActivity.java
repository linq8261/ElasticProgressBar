package com.audienl.elasticprogressbar;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.audienl.elasticprogressbarcore.ElasticProgressBar;
import com.audienl.elasticprogressbarcore.HandlerUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.elastic_progress_bar) ElasticProgressBar mElasticProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mElasticProgressBar.start();
    }

    @OnClick({R.id.btn_success, R.id.btn_failed})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_success:
                runSuccessAnimation();
                break;
            case R.id.btn_failed:
                runFailedAnimation();
                break;
        }
    }

    private void runSuccessAnimation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i < 6; i++) {
                    SystemClock.sleep(1000);
                    HandlerUtils.post(new HandlerUtils.ArgsRunnable(i) {
                        @Override
                        public void run(Object... args) {
                            int p = (int) args[0];
                            mElasticProgressBar.setProgress(p * 20);
                        }
                    });
                }

                HandlerUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        mElasticProgressBar.success();
                    }
                });
            }
        }).start();
    }

    private void runFailedAnimation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i < 5; i++) {
                    SystemClock.sleep(1000);

                    HandlerUtils.post(new HandlerUtils.ArgsRunnable(i) {
                        @Override
                        public void run(Object... args) {
                            int p = (int) args[0];
                            mElasticProgressBar.setProgress(p * 20);
                        }
                    });
                }

                HandlerUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        mElasticProgressBar.fail();
                    }
                });
            }
        }).start();
    }
}
