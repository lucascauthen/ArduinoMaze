package com.lucascauthen.ArduinoMazo.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import static android.view.View.GONE;

public class AnimationUtils {
    public static void fadeInFromGone(View v, int duration) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, 0f, 1f);
        animator.setDuration(duration);
        animator.start();
    }
    public static void fadeOutToGone(final View v, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, 1f, 0f);
        animator.setDuration(duration);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(GONE);
            }
        });
    }
    public static void fadeInFromGone(View v, int duration, Animator.AnimatorListener listener) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, 0f, 1f);
        animator.setDuration(duration);
        animator.start();
        animator.addListener(listener);
    }
    public static void fadeOutToGone(final View v, int duration, Animator.AnimatorListener listener) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, 1f, 0f);
        animator.setDuration(duration);
        animator.start();
        animator.addListener(listener);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(GONE);
            }
        });
    }
}
