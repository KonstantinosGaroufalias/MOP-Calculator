package com.example.mop_calculator

import android.animation.*
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object AnimationUtils {

    private val fastOutSlowIn = FastOutSlowInInterpolator()

    fun View.fadeIn(duration: Long = AnimationConstants.DURATION_MEDIUM) {
        if (!isVisible) {
            alpha = 0f
            isVisible = true
        }
        animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(fastOutSlowIn)
            .start()
    }

    fun View.fadeOut(duration: Long = AnimationConstants.DURATION_MEDIUM, hideOnEnd: Boolean = true) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(fastOutSlowIn)
            .withEndAction {
                if (hideOnEnd) isVisible = false
            }
            .start()
    }

    fun View.slideInFromBottom(duration: Long = AnimationConstants.DURATION_LONG) {
        translationY = height.toFloat()
        isVisible = true
        animate()
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(fastOutSlowIn)
            .start()
    }

    fun View.bounceClick() {
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat("scaleX", AnimationConstants.SCALE_PRESSED),
            PropertyValuesHolder.ofFloat("scaleY", AnimationConstants.SCALE_PRESSED)
        )
        scaleDown.duration = 100
        scaleDown.interpolator = fastOutSlowIn

        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat("scaleX", AnimationConstants.SCALE_NORMAL),
            PropertyValuesHolder.ofFloat("scaleY", AnimationConstants.SCALE_NORMAL)
        )
        scaleUp.duration = 100
        scaleUp.interpolator = fastOutSlowIn

        AnimatorSet().apply {
            playSequentially(scaleDown, scaleUp)
            start()
        }
    }

    fun View.pulse(repeatCount: Int = 1) {
        val pulse = ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.1f, 1.0f),
            PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.1f, 1.0f)
        )
        pulse.duration = 600
        pulse.interpolator = fastOutSlowIn
        pulse.repeatCount = repeatCount
        pulse.start()
    }

    fun ViewGroup.animateChildrenSequentially(delay: Long = 100) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.alpha = 0f
            child.translationY = 50f

            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay * i)
                .setDuration(AnimationConstants.DURATION_MEDIUM)
                .setInterpolator(fastOutSlowIn)
                .start()
        }
    }

    fun View.shakeError() {
        val shake = ObjectAnimator.ofFloat(
            this,
            "translationX",
            0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f
        )
        shake.duration = 500
        shake.start()
    }

    fun View.successFlash(originalColor: Int = 0xFF1E1E1E.toInt()) {
        val flash = ObjectAnimator.ofArgb(
            this,
            "backgroundColor",
            originalColor,
            0xFF4CAF50.toInt(),
            originalColor
        )
        flash.duration = 400
        flash.start()
    }
}
