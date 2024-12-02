package com.hardikkh.guardianapp.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel for the MainActivity.
 *
 * This ViewModel handles user authentication-related data, including access tokens
 * and error messages, and interacts with SharedPreferences to store and retrieve data securely.
 */
class MainViewModel(application: Application, private val sharedPreferences: SharedPreferences) : AndroidViewModel(application) {

    // LiveData for storing and observing the access token.
    private val _accessToken = MutableLiveData<String?>()
    val accessToken: LiveData<String?> get() = _accessToken

    // LiveData for storing and observing authentication errors.
    private val _authError = MutableLiveData<String?>()
    val authError: LiveData<String?> get() = _authError

    /**
     * Checks if an access token exists in SharedPreferences and updates the LiveData.
     */
    fun checkAccessToken() {
        _accessToken.value = sharedPreferences.getString("access_token", null)
    }

    /**
     * Saves the provided access token to SharedPreferences and updates the LiveData.
     *
     * @param token The access token to be saved.
     */
    fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString("access_token", token) // Store the access token securely.
            .apply()
        _accessToken.value = token // Update LiveData to notify observers.
    }

    /**
     * Sets an authentication error message in the LiveData.
     *
     * @param error The error message to be displayed.
     */
    fun setAuthError(error: String?) {
        _authError.value = error
    }
}
/**
 * Factory class for creating an instance of MainViewModel.
 *
 * This factory provides the necessary dependencies for MainViewModel,
 * including the application context and SharedPreferences.
 */
class MainViewModelFactory(
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
