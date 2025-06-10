package org.jellyfin.androidtv.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.jellyfin.androidtv.R

class ProfileImageContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = context.resources.getDimension(R.dimen.profile_focus_border_width)
    }

    // Create the AsyncImageView programmatically
    private val imageView: AsyncImageView = AsyncImageView(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        id = View.generateViewId()
        isClickable = false
        isFocusable = false
        isFocusableInTouchMode = false
    }

    init {
        addView(imageView)

        // Set up the image view
        imageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        imageView.circleCrop = true

        // Set up focus and click handling
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        clipChildren = false
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS

        // Set background to handle the circular mask
        background = ContextCompat.getDrawable(context, R.drawable.circle_background)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Ensure the image view stays circular
        imageView.circleCrop = true
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        // Draw border if focused
        if (isFocused) {
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = (width.coerceAtMost(height) / 2f) - (borderPaint.strokeWidth / 2)

            canvas.drawCircle(centerX, centerY, radius, borderPaint)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // Delegate important methods to the image view
    fun setImageUrl(url: String?) {
        val placeholder = ContextCompat.getDrawable(context, R.drawable.tile_user)
        imageView.load(
            url = url,
            blurHash = null,
            placeholder = placeholder,
            aspectRatio = 1.0,
            blurHashResolution = 32
        )
    }

    fun setImageResource(resId: Int) {
        imageView.setImageResource(resId)
    }
}
