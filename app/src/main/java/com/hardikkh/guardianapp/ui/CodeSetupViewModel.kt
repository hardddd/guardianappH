package com.hardikkh.guardianapp.ui

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CodeSetupViewModel(
    application: Application,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {

    // LiveData to store the first PIN entry
    val firstCodeEntry = MutableLiveData<String?>()

    // LiveData to store the second PIN entry for confirmation
    val secondCodeEntry = MutableLiveData<String?>()

    // LiveData to track whether the user is entering the first PIN or confirming it
    private val _isFirstEntry = MutableLiveData<Boolean>(true)
    val isFirstEntry: LiveData<Boolean> get() = _isFirstEntry

    // LiveData to store error messages, displayed to the user when validation fails
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // LiveData to indicate when the security code has been successfully saved
    private val _securityCodeSaved = MutableLiveData<Boolean>()
    val securityCodeSaved: LiveData<Boolean> get() = _securityCodeSaved

    /**
     * Handles the PIN entry flow:
     * 1. Validates that the PIN is 6 digits long.
     * 2. Saves the first PIN entry and prompts the user for confirmation.
     * 3. Checks that the confirmation matches the first PIN.
     */
    fun handlePinEntry(pin: String) {
        // Check if the PIN is exactly 6 digits
        if (pin.length != 6) {
            _errorMessage.value = "PIN must be exactly 6 digits"
            return
        }

        if (_isFirstEntry.value == true) {
            // First entry: Save the PIN and switch to confirmation step
            firstCodeEntry.value = pin
            _isFirstEntry.value = false
            secondCodeEntry.value = "" // Clear the input field for the second PIN entry
            _errorMessage.value = "Re-enter your PIN to confirm"
        } else {
            // Second entry: Validate the PIN matches the first entry
            secondCodeEntry.value = pin
            if (firstCodeEntry.value == secondCodeEntry.value) {
                // PINs match, save the security code
                saveSecurityCode(pin)
                _securityCodeSaved.value = true
            } else {
                // PINs do not match, reset the entries and show an error message
                _errorMessage.value = "PINs do not match. Please try again."
                resetEntries()
            }
        }
    }

    /**
     * Saves the security code into the encrypted SharedPreferences.
     * @param pin The 6-digit PIN to save.
     */
    private fun saveSecurityCode(pin: String) {
        sharedPreferences.edit()
            .putString("security_code", pin)
            .apply()
    }

    /**
     * Resets the PIN entry process:
     * - Clears both PIN entries.
     * - Sets the state back to the first entry step.
     */
    private fun resetEntries() {
        firstCodeEntry.value = null
        secondCodeEntry.value = null
        _isFirstEntry.value = true
    }
}


class CodeSetupViewModelFactory(
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    /**
     * Creates an instance of CodeSetupViewModel with the required dependencies.
     * @param modelClass The class of the ViewModel to create.
     * @return An instance of CodeSetupViewModel.
     * @throws IllegalArgumentException if the modelClass is not CodeSetupViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CodeSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CodeSetupViewModel(application, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

