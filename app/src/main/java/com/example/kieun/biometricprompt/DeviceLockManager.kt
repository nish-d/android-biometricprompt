package com.example.kieun.biometricprompt

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor


/**
 *Created by nishita.dutta on 2019-10-08, 17:37.
 */
open class DeviceLockManager(private var authenticationCallback: BiometricPrompt.AuthenticationCallback, private val mainThreadExecutor: Executor, private val fragmentActivity: FragmentActivity) {

    companion object {
        const val SECURITY_SETTING_REQUEST_CODE: Int = 2000
        const val LOCK_REQUEST_CODE: Int = 2001
    }

    private var keyguardManager: KeyguardManager = fragmentActivity.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

    /**
     * Opens device lock or fingerprint dialog according to user settings and hardware capabilities
     *
     * @param title
     * @param subtitle
     * @param description
     * @return true if authentication is possible , false if authentication is  not possible
     */
    @SuppressLint("NewApi")
    fun authenticate(title: String, subtitle: String, description: String): Boolean {
        when {
            canAuthenticateWithBiometrics() -> showBiometricPrompt(title, description, title)
            isDeviceSecure() -> showDeviceLockPrompt(title, description)
            else -> return false
            //askUserToEnroll()
        }
        return true
    }

    private fun showBiometricPrompt(title: String, subtitle: String, description: String) {
        val mBiometricPrompt = BiometricPrompt(fragmentActivity, mainThreadExecutor, authenticationCallback)

        // Set prompt info
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setDescription(description)
                .setTitle(title)
                .setSubtitle(subtitle)
                //allow device credentials only if pin/pattern is set
                .setDeviceCredentialAllowed(isDeviceSecure())
                .build()

        mBiometricPrompt.authenticate(promptInfo)
        mBiometricPrompt.cancelAuthentication()
    }

    fun isDeviceSecure(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceSecure
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //will also return true even if sim card is locked
            keyguardManager.isKeyguardSecure
        } else {
            return false
        }
    }

    /**
     * Indicate whether this device can authenticate the user with biometrics
     * @return true if there are any available biometric sensors and biometrics are enrolled on the device, if not, return false
     */
    fun canAuthenticateWithBiometrics(): Boolean {
        return BiometricManager.from(fragmentActivity).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isBiometricNotEnrolled(): Boolean {
        return BiometricManager.from(fragmentActivity).canAuthenticate() == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    fun askUserToEnroll() : Boolean{
        if (isBiometricNotEnrolled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //open security
            try {
                //Start activity for result
                fragmentActivity.startActivityForResult( Intent(Settings.ACTION_SECURITY_SETTINGS), SECURITY_SETTING_REQUEST_CODE)
            } catch (ex: Exception) {
                //If app is unable to find any Security settings then redirect to Settings
                fragmentActivity.startActivityForResult(Intent(Settings.ACTION_SETTINGS), SECURITY_SETTING_REQUEST_CODE)
            }
            return true
        }
        return false;
    }

    private fun showDeviceLockPrompt(title: String, description: String) {
        //Create an intent to open device screen lock screen to authenticate
        //Pass the Screen Lock screen Title and Description
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val i = keyguardManager.createConfirmDeviceCredentialIntent(title, description)

            try {
                //Start activity for result
                fragmentActivity.startActivityForResult(i, LOCK_REQUEST_CODE)
            } catch (e: Exception) {
            }
        }
    }
}