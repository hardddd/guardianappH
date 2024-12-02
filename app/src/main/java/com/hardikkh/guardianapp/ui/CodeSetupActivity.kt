package com.hardikkh.guardianapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hardikkh.guardianapp.R
import com.hardikkh.guardianapp.databinding.ActivityCodeSetupBinding
import com.hardikkh.guardianapp.ui.base.BaseActivity
import com.hardikkh.guardianapp.ui.utils.console
import com.hardikkh.guardianapp.ui.utils.showToast

class CodeSetupActivity : BaseActivity<ActivityCodeSetupBinding, CodeSetupViewModel>() {

    /**
     * Binds the view layout to the activity using View Binding.
     * @return An instance of ActivityCodeSetupBinding.
     */
    override fun getViewBinding(): ActivityCodeSetupBinding {
        return ActivityCodeSetupBinding.inflate(layoutInflater)
    }

    /**
     * Specifies the ViewModel class to be used with this activity.
     * @return The class type of CodeSetupViewModel.
     */
    override fun getViewModelClass(): Class<CodeSetupViewModel> {
        return CodeSetupViewModel::class.java
    }

    /**
     * Provides a custom ViewModelFactory to supply dependencies (e.g., SharedPreferences) to the ViewModel.
     * @return An instance of ViewModelProvider.Factory.
     */
    override fun getViewModelFactory(): ViewModelProvider.Factory {
        val sharedPreferences = getEncryptedSharedPreferences()
        return CodeSetupViewModelFactory(application, sharedPreferences)
    }

    /**
     * Called when the activity is first created.
     * - Binds the ViewModel to the view.
     * - Sets up LiveData observation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind ViewModel to the layout for data binding.
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Start observing ViewModel's LiveData properties.
        observeViewModel()
    }

    /**
     * Observes LiveData from the ViewModel and reacts to changes.
     */
    private fun observeViewModel() {
        // Observes error messages from the ViewModel and displays them as Toast messages.
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // Observes the `securityCodeSaved` LiveData to navigate to the home screen when true.
        viewModel.securityCodeSaved.observe(this) { isSaved ->
            if (isSaved) {
                navigateToHome() // Navigate to HomeActivity upon successful security code setup.
            }
        }
    }

    /**
     * Navigates the user to the HomeActivity upon successful security code setup.
     * Also clears the back stack to prevent returning to this activity.
     */
    private fun navigateToHome() {
        // Inform the user about the successful setup using a Toast message.
        Toast.makeText(this, "Security code setup successfully.", Toast.LENGTH_LONG).show()

        // Create an intent for HomeActivity and clear the activity stack.
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        // Finish the current activity.
        finish()
    }
}

