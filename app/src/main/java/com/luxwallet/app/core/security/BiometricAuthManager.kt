package com.luxwallet.app.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

enum class BiometricAvailability { AVAILABLE, NO_HARDWARE, NOT_ENROLLED, UNAVAILABLE }

/** App lock (PRD §36): biometric unlock with device-credential fallback. */
object BiometricAuthManager {

    private const val AUTH_FLAGS =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun availability(activity: FragmentActivity): BiometricAvailability {
        val manager = BiometricManager.from(activity)
        return when (manager.canAuthenticate(AUTH_FLAGS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
            else -> BiometricAvailability.UNAVAILABLE
        }
    }

    /** Emits true once on success, false on failure/cancel, then completes. */
    fun authenticate(activity: FragmentActivity) = callbackFlow {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                trySend(true)
                close()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                trySend(false)
                close()
            }

            override fun onAuthenticationFailed() {
                trySend(false)
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Buka Lumi")
            .setAllowedAuthenticators(AUTH_FLAGS)
            .build()
        prompt.authenticate(promptInfo)

        awaitClose { }
    }
}
