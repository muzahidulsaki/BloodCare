package com.example.bloodcare

import android.graphics.drawable.Animatable // This import is necessary
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SuccessBottomSheet(
    private val message: String,
    private val onDismissAction: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_success_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivIcon = view.findViewById<ImageView>(R.id.ivSuccessIcon)
        val tvMessage = view.findViewById<TextView>(R.id.tvSuccessMessage)

        tvMessage.text = message

        val drawable = ivIcon.drawable
        if (drawable is Animatable) {
            drawable.start() // Start animation
        }

        Handler(Looper.getMainLooper()).postDelayed({
            dismiss()
            onDismissAction()
        }, 2500)
    }
}