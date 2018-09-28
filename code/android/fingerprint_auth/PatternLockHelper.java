

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.infairy.mobilemycasa.R;
import com.infairy.mobilemycasa.util.log.Log;

import java.util.ArrayList;
import java.util.List;


public class PatternLockHelper implements PatternLockViewListener {

    private final SharedPreferences mSharedPreferences;
//    private final RelativeLayout settingContent;
    private final Runnable onResult;
    private final Runnable onSettingFinish;
    private PatternLockView mPatternLockView;
    private static final String PATTERN_PASSWD = "pattern_passwd";
    private int settingCount;

    private TextView settingMsg;

    private PatternLockView settingLockPattern;

    private PatternLockViewListener patternLockViewListener = new PatternLockViewListener() {

        String tempPasswd;
        Runnable curRunnable;
        int runDelay = 1000;
        boolean isRunning = false;

        public void pushRun(Runnable run) {
            curRunnable = run;
            runDelay = 1000;
            if (!isRunning) {
                isRunning = true;
                new Thread(()->{
                    while (runDelay > 0) {
                        runDelay = runDelay - 100;
                        try {
                            Thread.sleep(100);
                        } catch (Exception ignored) {}
                    }

                    settingLockPattern.post(()->{
                        if (curRunnable!=null)
                            curRunnable.run();
                    });

                    isRunning = false;
                }).start();
            }
        }

        @Override
        public void onStarted() {
            settingMsg.setText("完成後手指離開螢幕");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            curRunnable = null;
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            String res = PatternLockUtils.patternToString(settingLockPattern, pattern);
            if (pattern.size()<5) {
                settingMsg.setText("至少連接4個點，請在試一次");
                settingLockPattern.setViewMode(PatternLockView.PatternViewMode.WRONG);
                pushRun(settingLockPattern::clearPattern);
                return;
            }

            if (settingCount > 0) {

                if (!res.equals(tempPasswd)) {
                    settingMsg.setText("圖形錯誤");
                    settingLockPattern.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    pushRun(settingLockPattern::clearPattern);
                    return;
                }

                String passwd = Tool.SHA256(res);
                mSharedPreferences.edit().putString(PATTERN_PASSWD,passwd).apply();
                settingMsg.setText("設定完成");
                settingLockPattern.postDelayed(()->{
                    settingLockPattern.clearPattern();
                    onSettingFinish.run();
                },1000);

            } else if (settingCount == 0) {
                tempPasswd = res;
                settingMsg.setText("請再重新輸入圖形密碼");
                pushRun(settingLockPattern::clearPattern);
                settingCount = 1;
            } else {
                String oldPasswd = mSharedPreferences.getString(PATTERN_PASSWD, null);
                String passwd = Tool.SHA256(res);
                if (!passwd.equals(oldPasswd)) {
                    settingMsg.setText("圖形錯誤");
                    settingLockPattern.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    pushRun(()->{
                        settingMsg.setText("請輸入舊的圖形密碼");
                        settingLockPattern.clearPattern();
                    });
                } else {
                    settingCount = 0;
                    settingMsg.setText(settingMsg.getResources().getString(R.string.draw_pattern));
                    pushRun(settingLockPattern::clearPattern);
                }
            }
        }

        @Override
        public void onCleared() {

        }
    };

    public PatternLockHelper(RelativeLayout settingContent, SharedPreferences mSharedPreferences, PatternLockView mPatternLockView, Runnable onSuccess,Runnable onSettingFinish) {
        this.mSharedPreferences = mSharedPreferences;
        this.mPatternLockView = mPatternLockView;
//        this.settingContent = settingContent;
        this.onSettingFinish = onSettingFinish;
        this.onResult = onSuccess;

        settingCount = 0;

        settingContent.setTag(777);
        settingContent.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
           int visibility = settingContent.getVisibility();

           if ((int)settingContent.getTag()==visibility)
               return;
           else settingContent.setTag(visibility);

           if (visibility == View.GONE) {
               settingCount = 0;
               if (settingLockPattern!=null)
                   settingLockPattern.removePatternLockListener(patternLockViewListener);
           } else if (visibility == View.VISIBLE) {
               bindview(settingContent);
               settingLockPattern.addPatternLockListener(patternLockViewListener);
               if (isPatternSetted()) {
                   settingMsg.setText("請輸入舊的圖形密碼");
                   settingCount = -1;
               }
           }
        });

        mPatternLockView.addPatternLockListener(this);
    }

    private void bindview(RelativeLayout settingContent) {
        settingLockPattern = settingContent.findViewById(R.id.pattern_lock_view_set);
        settingMsg = settingContent.findViewById(R.id.pattern_lock_view_set_msg);
    }


    public boolean isPatternSetted() {
        return mSharedPreferences.getString(PATTERN_PASSWD,null) != null;
    }

    @Override
    public void onStarted() {
        Log.d(getClass().getName(), "Pattern drawing started");

    }

    @Override
    public void onProgress(List<PatternLockView.Dot> progressPattern) {
        Log.d(getClass().getName(), "Pattern progress: " +
                PatternLockUtils.patternToString(mPatternLockView, progressPattern));
    }

    @Override
    public void onComplete(List<PatternLockView.Dot> pattern) {
        String res = PatternLockUtils.patternToString(mPatternLockView, pattern);
        Log.d(getClass().getName(), "Pattern complete: " + res);
        String passwd = Tool.SHA256(res);
        if (passwd.equals(mSharedPreferences.getString(PATTERN_PASSWD,null))) {
            onResult.run();
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        } else {
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
            mPatternLockView.postDelayed(mPatternLockView::clearPattern,1000);

        }
    }

    @Override
    public void onCleared() {
        Log.d(getClass().getName(), "Pattern has been cleared");
    }
}
