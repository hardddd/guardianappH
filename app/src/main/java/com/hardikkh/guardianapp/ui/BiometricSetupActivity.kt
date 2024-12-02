package com.hardikkh.guardianapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hardikkh.guardianapp.R
import com.hardikkh.guardianapp.databinding.ActivityBiometricSetupBinding
import com.hardikkh.guardianapp.ui.base.BaseActivity
import com.hardikkh.guardianapp.ui.utils.console
import java.lang.System.load
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class BiometricSetupActivity : AppCompatActivity() {

    private lateinit var encryptedPrefs: SharedPreferences
    var securityCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric_setup)

        // Initialize encrypted shared preferences
        encryptedPrefs = getEncryptedSharedPreferences()
        securityCode = encryptedPrefs.getString("security_code", null)
        val doNotAskAgain = encryptedPrefs.getBoolean("biometric_do_not_ask_again", false)

        // Step 1: Check if biometric authentication is supported
        if (!isBiometricSupported()) {
            console("BiometricSetupActivity...", "isBiometricSupported false")
            // If unsupported, decide next step based on whether a security code exists
            if (securityCode.isNullOrEmpty()) {
                promptForCodeSetup() // Navigate to security code setup
            } else {
                promptForHomeScreen() // Navigate to home screen
            }
            return
        }

        // Step 2: Check if the user selected "Don't Ask Again"
        if (isBiometricSupported() && doNotAskAgain) {
            console("BiometricSetupActivity...", "User opted for 'Don't Ask Again'")
            // Skip biometric setup and decide next step
            if (securityCode.isNullOrEmpty()) {
                promptForCodeSetup()
            } else {
                promptForHomeScreen()
            }
            return
        }

        // Step 3: Show the explanation dialog to the user
        showExplanationDialog()
    }

    /**
     * Shows an explanation dialog to the user about why biometric authentication is beneficial.
     * Provides options to agree, skip, or opt-out of future prompts.
     */
    private fun showExplanationDialog() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(
            this,
            com.google.android.material.R.style.Theme_Material3_Light_Dialog_Alert
        )
            .setTitle("Why Enable Biometric Authentication?")
            .setMessage(
                "Biometric authentication allows you to securely access your account using your fingerprint or face." +
                        "\n\nIt ensures better security and convenience compared to traditional PIN or password methods."
            )
            .setPositiveButton("Agree") { dialog, _ ->
                dialog.dismiss()
                showBiometricSetupPrompt() // Proceed to biometric setup
            }
            .setNegativeButton("Skip") { dialog, _ ->
                dialog.dismiss()
                saveBiometricPreference(false) // User declined biometric setup
                // Decide next step based on whether a security code exists
                if (securityCode.isNullOrEmpty()) {
                    promptForCodeSetup()
                } else {
                    promptForHomeScreen()
                }
            }
            .setNeutralButton("Don't Ask Again") { dialog, _ ->
                dialog.dismiss()
                saveDoNotAskAgainPreference() // Save preference to skip future prompts
                if (securityCode.isNullOrEmpty()) {
                    promptForCodeSetup()
                } else {
                    promptForHomeScreen()
                }
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }

    /**
     * Saves the "Don't Ask Again" preference in encrypted shared preferences.
     */
    private fun saveDoNotAskAgainPreference() {
        encryptedPrefs.edit()
            .putBoolean("biometric_do_not_ask_again", true)
            .apply()
    }

    /**
     * Checks if the device supports biometric authentication.
     * @return True if supported, false otherwise.
     */
    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Creates a cipher for biometric encryption/decryption.
     * Used for encrypting authentication data securely.
     */
    private fun createCipher(): Cipher {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keySpec = KeyGenParameterSpec.Builder(
            "biometric_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .build()

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()

        val secretKey = keyStore.getKey("biometric_key", null) as SecretKey
        return Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
            .apply { init(Cipher.ENCRYPT_MODE, secretKey) }
    }

    /**
     * Displays the biometric setup prompt and handles user interactions.
     */
    private fun showBiometricSetupPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Authentication")
            .setDescription("Use your fingerprint or face to secure your account.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Skip")
            .build()

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    console("BiometricSetupActivity...", "onAuthenticationSucceeded")
                    handleBiometricSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    console("BiometricSetupActivity...", "onAuthenticationError: $errString")
                    handleBiometricError()
                }

                override fun onAuthenticationFailed() {
                    console("BiometricSetupActivity...", "onAuthenticationFailed")
                    handleBiometricError()
                }
            })

        try {
            val cipher = createCipher()
            val cryptoObject = BiometricPrompt.CryptoObject(cipher)
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        } catch (e: KeyPermanentlyInvalidatedException) {
            console("BiometricSetupActivity", "Key permanently invalidated. Regenerating...")
            regenerateKey()
        }
    }

    /**
     * Handles successful biometric authentication.
     * @param result The biometric authentication result.
     */
    private fun handleBiometricSuccess(result: BiometricPrompt.AuthenticationResult) {
        saveBiometricPreference(true)
        if (securityCode.isNullOrEmpty()) {
            promptForCodeSetup()
        } else {
            promptForHomeScreen()
        }
    }

    /**
     * Handles biometric authentication errors or failures.
     */
    private fun handleBiometricError() {
        saveBiometricPreference(false)
        if (securityCode.isNullOrEmpty()) {
            promptForCodeSetup()
        } else {
            promptForHomeScreen()
        }
    }

    /**
     * Regenerates a cryptographic key if the existing one is invalidated.
     */
    private fun regenerateKey() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias("biometric_key")) {
            keyStore.deleteEntry("biometric_key")
        }
        createCipher() // Recreates the key
    }

    /**
     * Saves the biometric preference in encrypted shared preferences.
     * @param enabled True if biometric is enabled, false otherwise.
     */
    private fun saveBiometricPreference(enabled: Boolean) {
        encryptedPrefs.edit()
            .putBoolean("biometric_enabled", enabled)
            .apply()
    }

    /**
     * Navigates to the Code Setup screen.
     */
    private fun promptForCodeSetup() {
        val intent = Intent(this, CodeSetupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to the Home screen.
     */
    private fun promptForHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Initializes and returns encrypted shared preferences.
     * @return The shared preferences instance.
     */
    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            this,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
