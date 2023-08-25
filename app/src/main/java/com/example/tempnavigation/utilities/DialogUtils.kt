package com.example.tempnavigation.utilities

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tempnavigation.R

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
}