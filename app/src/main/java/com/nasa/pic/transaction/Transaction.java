package com.nasa.pic.transaction;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;

public final class Transaction {
	/**
	 * There is different between android pre 3.0 and 3.x, 4.x on this wording.
	 */
	public static final String ALPHA =
			(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) ? "alpha" : "Alpha";
	private static final int ANIM_DURATION = 600;


	private ColorDrawable mColorDrawable;

	private Thumbnail mThumbnail;
	private ImageView mTarget;

	private int mLeftDelta;
	private int mTopDelta;
	private float mWidthScale;
	private float mHeightScale;


	public static class Builder {
		private Transaction mTransaction;

		public Builder() {
			mTransaction = new Transaction();
		}


		public Builder setThumbnail(Thumbnail thumbnail) {
			mTransaction.mThumbnail = thumbnail;
			return this;
		}

		public Builder setTarget(ImageView target) {
			mTransaction.mTarget = target;
			return this;
		}


		public Transaction build(Context cxt) {
			Picasso.with(cxt).load(mTransaction.mThumbnail.getSource()).into(mTransaction.mTarget);

			// Figure out where the thumbnail and full size versions are, relative
			// to the screen and each other
			int[] screenLocation = new int[2];
			mTransaction.mTarget.getLocationOnScreen(screenLocation);
			mTransaction.mLeftDelta = mTransaction.mThumbnail.getLeft() - screenLocation[0];
			mTransaction.mTopDelta = mTransaction.mThumbnail.getTop() - screenLocation[1];

			// Scale factors to make the large version the same size as the thumbnail
			mTransaction.mWidthScale = (float) mTransaction.mThumbnail.getWidth() / mTransaction.mTarget.getWidth();
			mTransaction.mHeightScale = (float) mTransaction.mThumbnail.getHeight() / mTransaction.mTarget.getHeight();

			return mTransaction;
		}
	}

	public Transaction() {
		mColorDrawable = new ColorDrawable(Color.BLACK);
	}

	/**
	 * The enter animation scales the picture in from its previous thumbnail size/location.
	 *
	 * @param listener
	 * 		For end of animation.
	 */
	public void enterAnimation(Animator.AnimatorListener listener) {

		// Set starting values for properties we're going to animate. These
		// values scale and position the full size version down to the thumbnail
		// size/location, from which we'll animate it back up
		mTarget.setPivotX(0);
		mTarget.setPivotY(0);
		mTarget.setScaleX(mWidthScale);
		mTarget.setScaleY(mHeightScale);
		mTarget.setTranslationX(mLeftDelta);
		mTarget.setTranslationY(mTopDelta);

		// Animate scale and translation to go from thumbnail to full size
		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(mTarget);
		animator.setDuration(ANIM_DURATION).scaleX(1).scaleY(1).
				translationX(0).translationY(0).setInterpolator(new DecelerateInterpolator()).setListener(listener);

		// Fade in the black background
		ObjectAnimator bgAnim = ObjectAnimator.ofInt(mColorDrawable, ALPHA, 0, 255);
		bgAnim.setDuration(ANIM_DURATION);
		bgAnim.start();

	}


	/**
	 * The exit animation is basically a reverse of the enter animation. This Animate image back to thumbnail
	 * size/location as relieved from bundle.
	 */
	public void exitAnimation(Animator.AnimatorListener listener) {
		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(mTarget);
		animator.setDuration(ANIM_DURATION).scaleX(mWidthScale).scaleY(mHeightScale).
				translationX(mLeftDelta).translationY(mTopDelta).setInterpolator(new AccelerateInterpolator())
				.setListener(listener);

		// Fade out background
		ObjectAnimator bgAnim = ObjectAnimator.ofInt(mColorDrawable, ALPHA, 0);
		bgAnim.setDuration(ANIM_DURATION);
		bgAnim.start();
	}

}
