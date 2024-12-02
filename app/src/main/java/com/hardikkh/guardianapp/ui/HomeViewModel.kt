package com.hardikkh.guardianapp.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel for the HomeActivity. It manages the state related to biometric authentication
 * and security code preferences.
 *
 * @param application The Application instance.
 * @param sharedPreferences The SharedPreferences instance for storing/retrieving preferences.
 */
class HomeViewModel(application: Application, private val sharedPreferences: SharedPreferences) : AndroidViewModel(application) {

    // LiveData for storing the state of biometric authentication enablement.
    private val _biometricEnabled = MutableLiveData<Boolean>()
    val biometricEnabled: LiveData<Boolean> get() = _biometricEnabled

    // LiveData for the "Don't Ask Again" preference state.
    private val _dontAskAgain = MutableLiveData<Boolean>()
    val dontAskAgain: LiveData<Boolean> get() = _dontAskAgain

    // LiveData for storing the saved security code (if any).
    private val _securityCode = MutableLiveData<String?>()
    val securityCode: LiveData<String?> get() = _securityCode

    init {
        // Load the preferences when the ViewModel is created.
        loadPreferences()
    }

    /**
     * Loads the preferences related to biometric authentication and security code
     * from SharedPreferences and updates the respective LiveData objects.
     */
    private fun loadPreferences() {
        _biometricEnabled.value = sharedPreferences.getBoolean("biometric_enabled", false) // Whether biometric is enabled.
        _dontAskAgain.value = sharedPreferences.getBoolean("biometric_do_not_ask_again", false) // Whether user chose "Don't Ask Again".
        _securityCode.value = sharedPreferences.getString("security_code", null) // The saved security code.
    }
}
/**
 * Factory class for creating instances of HomeViewModel with dependencies injected.
 *
 * @param application The Application instance.
 * @param sharedPreferences The SharedPreferences instance for storing/retrieving preferences.
 */
class HomeViewModelFactory(
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the specified ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A new instance of the specified ViewModel class.
     * @throws IllegalArgumentException If the ViewModel class is not assignable from HomeViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}