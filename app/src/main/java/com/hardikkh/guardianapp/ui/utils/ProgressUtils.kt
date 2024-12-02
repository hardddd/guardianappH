package com.hardikkh.guardianapp.ui.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.hardikkh.guardianapp.R

class ProgressUtils (val activity: Context) {

    var dialog: Dialog? = null
//    val dialog by lazy { Dialog(activity) }

    //..we need the context else we can not create the dialog so get context in constructor
    fun showLoading() {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.dialog_progress)

        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
        if (dialog?.isShowing == false) {
            dialog?.show()
        }

    }

    //..also create a method which will hide the dialog when some work is done
    fun hideLoading() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
        dialog?.dismiss()
    }
}