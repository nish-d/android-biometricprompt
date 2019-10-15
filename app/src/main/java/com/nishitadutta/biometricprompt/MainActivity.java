package com.nishitadutta.biometricprompt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executor;
//import android.hardware.biometrics.BiometricManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private DeviceLockManager deviceLockManager;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //deviceLockManager = new DeviceLockManager(getAuthenticationCallback(), getMainThreadExecutor(), this);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            deviceLockManager = new DeviceLockManager(getAuthenticationCallback(), getMainThreadExecutor(), this);
            if (!showBiometricPrompt()) {
                //ask user to enroll
                Toast.makeText(MainActivity.this, "Cannot use biometric", Toast.LENGTH_SHORT).show();
                deviceLockManager.askUserToEnroll();
            }
        });
    }

    private boolean showBiometricPrompt() {
        return deviceLockManager.authenticate("Title", "Subtitle", "Description");
    }

    private BiometricPrompt.AuthenticationCallback getAuthenticationCallback() {
        // Callback for biometric authentication result
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "Error + " + errorCode + " " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Log.i(TAG, "onAuthenticationSucceeded");
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Executor getMainThreadExecutor() {
        return new MainThreadExecutor();
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable r) {
            handler.post(r);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DeviceLockManager.LOCK_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    //If screen lock authentication is success update text
                    Toast.makeText(MainActivity.this, "LOCK_REQUEST_CODE Success", Toast.LENGTH_SHORT).show();
                } else {
                    //If screen lock authentication is failed update text
                    Toast.makeText(MainActivity.this, "LOCK_REQUEST_CODE Failure", Toast.LENGTH_SHORT).show();
                }
                break;
            case DeviceLockManager.SECURITY_SETTING_REQUEST_CODE:
                //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (deviceLockManager.isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    Toast.makeText(MainActivity.this, "SECURITY_SETTING_REQUEST_CODE Success", Toast.LENGTH_SHORT).show();
                    showBiometricPrompt();
                } else {
                    //If screen lock is not enabled just update text
                    Toast.makeText(MainActivity.this, "SECURITY_SETTING_REQUEST_CODE device not secure", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
