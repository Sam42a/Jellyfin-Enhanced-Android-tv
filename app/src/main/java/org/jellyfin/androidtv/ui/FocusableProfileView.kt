package org.jellyfin.androidtv.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import org.jellyfin.androidtv.R

class FocusableProfileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val focusBorderWidth = resources.getDimensionPixelSize(R.dimen.profile_focus_border_width)
    private val focusBorderColor = Color.WHITE
    private val backgroundDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.TRANSPARENT)
    }

    init {
        // Let the parent handle focus and clicks
        isFocusable = false
        isFocusableInTouchMode = false
        isClickable = false
        
        // Only handle the visual border
        background = backgroundDrawable
        
        // Listen to parent focus changes
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                backgroundDrawable.setStroke(focusBorderWidth, focusBorderColor)
            } else {
                backgroundDrawable.setStroke(0, Color.TRANSPARENT)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backgroundDrawable.setSize(w, h)
    }
}
