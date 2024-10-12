package com.example.tempnavigation.views.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.tempnavigation.R

class AlarmDialogFragment(val title:String,val msg:String,val fragmentMan: FragmentManager?,val onSuccess:()->Unit) : DialogFragment() {
    private  var rootView:View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = buildDialog()

        return dialog
    }
    private fun buildDialog():Dialog {
         val dialog = AlertDialog.Builder(requireContext())
         dialog.setTitle(title)
            .setMessage(msg)
            .setPositiveButton("Alarm Off") { dialog, which ->
                onSuccess.invoke()
                dialog.dismiss()
            }
        return  dialog.create()
    }
    fun show(){
        if (fragmentMan!= null) {
            show(fragmentMan,"Alarm_Dialog")
        }
    }
}