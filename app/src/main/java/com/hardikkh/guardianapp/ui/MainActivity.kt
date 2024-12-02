package com.hardikkh.guardianapp.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.button.MaterialButton
import com.hardikkh.guardianapp.R
import com.hardikkh.guardianapp.databinding.ActivityMainBinding
import com.hardikkh.guardianapp.ui.base.BaseActivity
import com.hardikkh.guardianapp.ui.utils.console
import com.hardikkh.guardianapp.ui.utils.showToast
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

/**
 * MainActivity manages the login screen and the OAuth2 authentication flow.
 *
 * It initializes the login screen, handles the OAuth2 flow, and navigates to the
 * HomeActivity upon successful login.
 */
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    // Launcher to handle OAuth authentication results.
    private lateinit var authResultLauncher: ActivityResultLauncher<Intent>

    // Lazy initialization of the AuthorizationService for OAuth operations.
    private val authService by lazy { AuthorizationService(this) }

    /**
     * Returns the binding for the activity.
     */
    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    /**
     * Returns the ViewModel class associated with the activity.
     */
    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    /**
     * Returns a factory for creating the ViewModel with dependencies.
     */
    override fun getViewModelFactory(): ViewModelProvider.Factory {
        val sharedPreferences = getEncryptedSharedPreferences()
        return MainViewModelFactory(application, sharedPreferences)
    }

    /**
     * Initializes the activity and sets up the ViewModel, LiveData observers, and UI interactions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up data binding with ViewModel and lifecycleOwner.
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Observe LiveData from ViewModel.
        observeViewModel()

        // Initialize the result launcher for handling OAuth authentication results.
        authResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleAuthResult(result)
        }

        // Check for existing access token.
        viewModel.checkAccessToken()

        // Set up the login button to start the OAuth flow.
        binding.loginButton.setOnClickListener {
            startOAuthFlow()
        }
    }

    /**
     * Observes ViewModel LiveData for state updates.
     */
    private fun observeViewModel() {
        // Observe the accessToken LiveData to navigate to HomeActivity if token is valid.
        viewModel.accessToken.observe(this) { token ->
            if (!token.isNullOrEmpty()) {
                navigateToHome()
            }
        }

        // Observe the authError LiveData to display error messages.
        viewModel.authError.observe(this) { error ->
            error?.let {
                showToast(it)
            }
        }
    }

    /**
     * Handles the result of the OAuth authentication flow.
     *
     * @param result The result of the OAuth authentication flow.
     */
    private fun handleAuthResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val authResponse = AuthorizationResponse.fromIntent(data!!)
            val authException = AuthorizationException.fromIntent(data)
            if (authResponse != null) {
                // Authorization successful, exchange the code for a token.
                exchangeAuthorizationCode(authResponse)
            } else if (authException != null) {
                // Handle authorization errors.
                viewModel.setAuthError("Authentication error: ${authException.error}")
            }
        } else {
            viewModel.setAuthError("Something went wrong...")
        }
    }

    /**
     * Starts the OAuth2 flow by launching the authorization intent.
     */
    private fun startOAuthFlow() {
        // Configuration for the OAuth authorization service.
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://id.stage.kidsxap.com.au/connect/authorize"), // Authorization endpoint.
            Uri.parse("https://id.stage.kidsxap.com.au/connect/token") // Token endpoint.
        )

        // Create an authorization request with required parameters.
        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            "GuardianApp", // Client ID
            ResponseTypeValues.CODE, // Authorization response type.
            Uri.parse("guardianapp://oauth-callback") // Redirect URI
        ).setScope("XapFinanceApi").build()

        // Launch the authorization intent.
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        authResultLauncher.launch(authIntent)
    }

    /**
     * Exchanges the authorization code for an access token.
     *
     * @param authResponse The authorization response containing the code.
     */
    private fun exchangeAuthorizationCode(authResponse: AuthorizationResponse) {
        // Create a token exchange request using the authorization response.
        val tokenRequest = authResponse.createTokenExchangeRequest()

        // Perform the token exchange request using AuthorizationService.
        authService.performTokenRequest(tokenRequest) { tokenResponse, exception ->
            if (tokenResponse != null) {
                val accessToken = tokenResponse.accessToken
                if (!accessToken.isNullOrEmpty()) {
                    // Save the access token in ViewModel and navigate to HomeActivity.
                    viewModel.saveAccessToken(accessToken)
                } else {
                    viewModel.setAuthError("Failed to retrieve access token.")
                }
            } else if (exception != null) {
                // Handle token exchange errors.
                viewModel.setAuthError("Error getting access token: ${exception.errorDescription}")
            }
        }
    }

    /**
     * Navigates to the HomeActivity after successful login.
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}


