/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.infairy.mobilemycasa.MainActivity;
import com.infairy.mobilemycasa.R;
import com.infairy.mobilemycasa.util.FingerprintUiHelper;
import com.infairy.mobilemycasa.util.PatternLockHelper;
import com.infairy.mobilemycasa.util.log.Log;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements TextView.OnEditorActionListener, FingerprintUiHelper.Callback {

    private static final String TAG = "Fingerprint";

//    @BindView(R.id.cancel_button) Button mCancelButton;

    @BindView(R.id.second_dialog_button) Button mSecondDialogButton;

    @BindView(R.id.fingerprint_container) View mFingerprintContent;

    @BindView(R.id.pattern_lock_view_set_content) RelativeLayout settingContent;

    @BindView(R.id.pattern_lock_view_passwd) PatternLockView patternLockView;

    @BindView(R.id.fingerprint_status) TextView statusTextView;

    @BindView(R.id.fingerprint_icon) ImageView fingerprintIcon;

    private Stage mStage = Stage.FINGERPRINT;

    private FingerprintManagerCompat.CryptoObject mCryptoObject;
    private FingerprintUiHelper mFingerprintUiHelper;
    private MainActivity mActivity;
    private PatternLockHelper patternLockHelper;
    private Runnable onSuccess;
    private SharedPreferences mSharedPreferences;

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
//        setStyle(DialogFragment.STYLE_NORMAL, android.support.v4.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.sign_in));
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);

        ButterKnife.bind(this,v);

        mSecondDialogButton.setOnClickListener(view -> {
            if (mStage == Stage.SET_PATTERN_LOCK) {
                if (mFingerprintUiHelper.isFingerprintAuthAvailable()) {
                    mStage = Stage.BOTH;
                    mFingerprintUiHelper.startListening(mCryptoObject);
                } else {
                    mStage = Stage.PATTERN_LOCK;
                }
                updateView();
            } else {
                goToBackup();
            }
        });

        mFingerprintUiHelper = new FingerprintUiHelper(
                FingerprintManagerCompat.from(mActivity),
                v.findViewById(R.id.fingerprint_icon),
                v.findViewById(R.id.fingerprint_status), this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        patternLockHelper = new PatternLockHelper(settingContent,mSharedPreferences,patternLockView, this::onAuthenticated,()->{

            if (mFingerprintUiHelper.isFingerprintAuthAvailable()) {
                mStage = Stage.BOTH;
                mFingerprintUiHelper.startListening(mCryptoObject);
            } else
                mStage = Stage.PATTERN_LOCK;
            updateView();
        });

        if (!mFingerprintUiHelper.isFingerprintAuthAvailable() && !patternLockHelper.isPatternSetted()) {
            goToBackup();
        } else if (!mFingerprintUiHelper.isFingerprintAuthAvailable()) {
            mStage = Stage.PATTERN_LOCK;
            updateView();
        } else if (!patternLockHelper.isPatternSetted()) {
            mStage = Stage.FINGERPRINT;
            updateView();
        } else {
            mStage = Stage.BOTH;
            updateView();
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.BOTH || mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }

    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManagerCompat.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private void goToBackup() {
        mStage = Stage.SET_PATTERN_LOCK;
        updateView();

        // Fingerprint is not used anymore. Stop listening for it.
        mFingerprintUiHelper.stopListening();
    }

    private final Runnable mShowKeyboardRunnable = () -> {
//            mInputMethodManager.showSoftInput(mPassword, 0);
    };

    private void updateView() {
        switch (mStage) {
            case FINGERPRINT:
//                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setVisibility(View.VISIBLE);
                mSecondDialogButton.setText(R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                fingerprintIcon.setVisibility(View.VISIBLE);
                statusTextView.setVisibility(View.VISIBLE);
                settingContent.setVisibility(View.GONE);
                patternLockView.setVisibility(View.GONE);
                break;
            case PATTERN_LOCK:
//                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setVisibility(View.VISIBLE);
                mSecondDialogButton.setText(R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                fingerprintIcon.setVisibility(View.GONE);
                statusTextView.setVisibility(View.GONE);
                settingContent.setVisibility(View.GONE);
                patternLockView.setVisibility(View.VISIBLE);
                break;
            case BOTH:
//                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setVisibility(View.VISIBLE);
                mSecondDialogButton.setText(R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                fingerprintIcon.setVisibility(View.VISIBLE);
                statusTextView.setVisibility(View.VISIBLE);
                settingContent.setVisibility(View.GONE);
                patternLockView.setVisibility(View.VISIBLE);
                break;
            case SET_PATTERN_LOCK:
//                mCancelButton.setText(R.string.cancel);
                if (patternLockHelper.isPatternSetted())
                    mSecondDialogButton.setText("取消");
                else
                    mSecondDialogButton.setVisibility(View.GONE);
                mFingerprintContent.setVisibility(View.GONE);
                patternLockView.setVisibility(View.GONE);
                settingContent.setVisibility(View.VISIBLE);
//                if (mStage == Stage.NEW_FINGERPRINT_ENROLLED) {
////                    mPasswordDescriptionTextView.setVisibility(View.GONE);
//                    mNewFingerprintEnrolledTextView.setVisibility(View.VISIBLE);
//                    mUseFingerprintFutureCheckBox.setVisibility(View.VISIBLE);
//                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
//            verifyPassword();
            return true;
        }
        return false;
    }

    @Override
    public void onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
//        mActivity.onPurchased(true /* withFingerprint */, mCryptoObject);
        if (onSuccess!=null)
            onSuccess.run();
        dismiss();
    }

    @Override
    public void onError() {
        goToBackup();
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage {
        FINGERPRINT,
        PATTERN_LOCK,
        BOTH,
        SET_PATTERN_LOCK
    }
}
