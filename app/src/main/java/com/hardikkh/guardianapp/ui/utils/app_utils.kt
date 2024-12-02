package com.hardikkh.guardianapp.ui.utils

import android.content.Context
import android.util.Log
import android.widget.Toast


fun Context?.showToast(s: String?): Toast {
    return Toast.makeText(this, s, Toast.LENGTH_SHORT).apply { show() }
}

fun console(tag: String? = "CONSOLE", message: String?) {
    Log.w(tag, "$message")
}



