## Android 指紋 Dialog Fragment

### 參考資料：
* https://github.com/googlesamples/android-FingerprintDialog
* https://github.com/aritraroy/PatternLockView

### 目標:
* 使用系統的指紋辨識功能，若不支援則改用圖形密碼

### 可能情況：
1. 不支援指紋，未設定圖形密碼
2. 不支援指紋，已設定圖形密碼
3. 支援指紋，未設定圖形密碼
4. 支援指紋，已設定圖形密碼

### 如何使用：
```JAVA
FingerprintAuthenticationDialogFragment fadf = new FingerprintAuthenticationDialogFragment();

public void onResume() {
    FragmentActivity ac = activity;
    if (ac !=null && fadf!=null && !fadf.isAdded() && isNeedToAuthorize)
        fadf.show(ac.getSupportFragmentManager(),"top");
}

public void onPause() {
    FragmentActivity ac = activity;
    if (ac !=null && fadf!=null && fadf.isAdded())
        fadf.dismiss();
}
```

### 程式碼：
參考`code/android/fingerprint_auth`
