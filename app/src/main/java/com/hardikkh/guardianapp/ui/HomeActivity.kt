package com.hardikkh.guardianapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
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
import com.hardikkh.guardianapp.databinding.ActivityHomeBinding
import com.hardikkh.guardianapp.ui.base.BaseActivity

class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>() {

    /**
     * Provides the ViewBinding for the activity.
     */
    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    /**
     * Provides the ViewModel class for this activity.
     */
    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    /**
     * Provides a factory for creating the ViewModel with required dependencies.
     */
    override fun getViewModelFactory(): ViewModelProvider.Factory {
        val sharedPreferences = getEncryptedSharedPreferences()
        return HomeViewModelFactory(application, sharedPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind the ViewModel to the layout for data binding.
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Observe LiveData from ViewModel to handle state changes.
        observeViewModel()
    }

    /**
     * Observes changes in LiveData properties from the ViewModel.
     */
    private fun observeViewModel() {
        viewModel.biometricEnabled.observe(this) { biometricEnabled ->
            viewModel.dontAskAgain.observe(this) { dontAskAgain ->
                viewModel.securityCode.observe(this) { securityCode ->
                    when {
                        biometricEnabled && !dontAskAgain -> promptBiometricAuthentication(securityCode)
                        isBiometricSupported() && !dontAskAgain -> {
                            val intent = Intent(this, BiometricSetupActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        !securityCode.isNullOrEmpty() -> promptCodeVerification()
                        else -> redirectToSetup()
                    }
                }
            }
        }
    }

    /**
     * Checks if biometric authentication is supported on the device.
     * @return True if supported, otherwise false.
     */
    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Prompts the user to authenticate using biometrics.
     */
    private fun promptBiometricAuthentication(securityCode: String?) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate")
            .setSubtitle("Please authenticate to access your home screen")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Use Security Code")
            .build()

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (securityCode.isNullOrEmpty()) {
                        setCodeSetupActivity()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(this@HomeActivity, "Authentication failed: $errString", Toast.LENGTH_SHORT).show()
                    }
                    if (!securityCode.isNullOrEmpty()) {
                        promptCodeVerification()
                    } else {
                        setCodeSetupActivity()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@HomeActivity, "Biometric Authentication Failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Prompts the user to enter a 6-digit security code for verification.
     */
    private fun promptCodeVerification() {
        val input = EditText(this@HomeActivity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter your 6-digit code"
            gravity = Gravity.CENTER
            maxLines = 1
        }

        lateinit var dialog: AlertDialog

        // Container for buttons and input field
        val buttonContainer = LinearLayout(this@HomeActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, 20, 0, 0)

            // Cancel Button
            addView(Button(this@HomeActivity).apply {
                text = "Cancel"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.GRAY)
                setOnClickListener {
                    Toast.makeText(this@HomeActivity, "Authorization canceled.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })

            addView(Space(this@HomeActivity).apply {
                layoutParams = LinearLayout.LayoutParams(20, 0)
            })

            // Submit Button
            addView(Button(this@HomeActivity).apply {
                text = "Submit"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.BLACK)
                setOnClickListener {
                    val enteredCode = input.text.toString()
                    if (enteredCode.length != 6) {
                        Toast.makeText(this@HomeActivity, "Please enter a valid 6-digit code.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val savedCode = viewModel.securityCode.value
                    if (savedCode != null && savedCode == enteredCode) {
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@HomeActivity, "Incorrect code. Authorization failed.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            })
        }

        val container = LinearLayout(this@HomeActivity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            addView(input)
            addView(buttonContainer)
        }

        dialog = AlertDialog.Builder(this)
            .setTitle("Security Code")
            .setView(container)
            .setCancelable(false)
            .create()

        dialog.show()
    }

    /**
     * Redirects the user to setup activities (Biometric or Code Setup).
     */
    private fun redirectToSetup() {
        if (!viewModel.biometricEnabled.value!! && viewModel.securityCode.value.isNullOrEmpty()) {
            val intent = Intent(this, BiometricSetupActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            setCodeSetupActivity()
        }
        finish()
    }

    /**
     * Navigates to the Code Setup Activity for PIN setup.
     */
    private fun setCodeSetupActivity() {
        val intent = Intent(this, CodeSetupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

