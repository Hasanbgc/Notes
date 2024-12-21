package com.example.tempnavigation.utilities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.load.engine.Resource
import com.example.tempnavigation.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class DialogUtils:Dialogs {


    override fun doubleButtonDialog(
        context: Activity,
        title: String,
        message: String,
        yesButton: String,
        noButton: String,
        onYesClick: () -> Unit,
        onNoClick: () -> Unit
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setCancelable(false)
        val  dialogLayout = context.layoutInflater.inflate(R.layout.dialog_yes_no,null)
        alertDialogBuilder.setView(dialogLayout)

        val textViewTitle:TextView = dialogLayout.findViewById(R.id.textview_title)
        val textViewMessage:TextView = dialogLayout.findViewById(R.id.textview_des)
        textViewTitle.text = title
        textViewMessage.text = message
        val buttonYes: Button = dialogLayout.findViewById(R.id.button_yes)
        val buttonNo:Button = dialogLayout.findViewById(R.id.button_no)
        buttonYes.text = yesButton
        buttonNo.text = noButton
        val alertDialog = alertDialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        buttonYes.setOnClickListener{
            alertDialog.dismiss()
            onYesClick()
        }
        buttonNo.setOnClickListener{
            alertDialog.dismiss()
            onNoClick()
        }
        alertDialog.show()
    }

    override fun singleButtonDialog(
        context: Context,
        title: String,
        message: String,
        yesButton: String,
        noButton: String,
        onYesClick: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun progressDialog(context: Context) {
        TODO("Not yet implemented")
    }


    override fun showSnackBarWithActionButton(context:Context,view: View, msg:Int, confirmationMsg:Int,onUndo: ()->Unit,onTimeout: ()->Unit){
        Snackbar.make(view,msg,
            Snackbar.LENGTH_LONG).setAction("Undo"){
            onUndo()
            toast(context,"Deleted note is recovered")
        }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if(event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT){
                    onTimeout()
                    toast(context,context.resources.getString(confirmationMsg))
                }
            }
        }).show()
    }

    companion object {
        fun toast(context: Context, string: String) {
            Toast(context).cancel()
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
        }
    }
}