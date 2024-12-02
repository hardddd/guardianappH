package com.hardikkh.guardianapp.ui.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.viewbinding.ViewBinding
import com.hardikkh.guardianapp.ui.utils.ProgressUtils
import com.hardikkh.guardianapp.ui.utils.console
import com.hardikkh.guardianapp.ui.utils.showToast

abstract class BaseActivity<VB : ViewBinding, VM : ViewModel> : AppCompatActivity() {

    protected lateinit var binding: VB
    protected lateinit var viewModel: VM
    protected lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = getViewBinding()
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getEncryptedSharedPreferences()

        // Initialize ViewModel with custom factory if provided
        viewModel = initializeViewModel()
    }

    protected abstract fun getViewBinding(): VB
    protected abstract fun getViewModelClass(): Class<VM>

    // Optionally override this to provide a custom factory
    open fun getViewModelFactory(): ViewModelProvider.Factory? = null

    private fun initializeViewModel(): VM {
        val factory = getViewModelFactory()
        return if (factory != null) {
            ViewModelProvider(this, factory).get(getViewModelClass())
        } else {
            ViewModelProvider(this).get(getViewModelClass())
        }
    }

    protected fun getEncryptedSharedPreferences(): SharedPreferences {
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


