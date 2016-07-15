package com.nasa.pic.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.animation.Interpolator;

import com.chopping.activities.RestfulActivity;
import com.chopping.application.BasicPrefs;
import com.chopping.application.LL;
import com.chopping.utils.DeviceUtils;
import com.chopping.utils.DeviceUtils.ScreenSize;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.fragments.AboutDialogFragment;
import com.nasa.pic.databinding.ItemBinding;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.events.CompleteShareEvent;
import com.nasa.pic.events.FBShareEvent;
import com.nasa.pic.events.OpenPhotoEvent;
import com.nasa.pic.events.ShareEvent;
import com.nasa.pic.transition.BakedBezierInterpolator;
import com.nasa.pic.transition.TransitCompat;
import com.nasa.pic.utils.Prefs;

import java.math.BigDecimal;

import io.realm.RealmObject;


public abstract class AppRestfulActivity extends RestfulActivity implements OnMenuItemClickListener {
	private int mCellSize;
	private ItemBinding mItemBinding;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.nasa.pic.events.FBShareEvent}.
	 *
	 * @param e Event {@link com.nasa.pic.events.FBShareEvent}.
	 */
	public void onEvent(FBShareEvent e) {
		com.nasa.pic.utils.Utils.facebookShare(this, (PhotoDB) e.getObject());
	}

	/**
	 * Handler for {@link com.nasa.pic.events.ShareEvent}.
	 *
	 * @param e Event {@link com.nasa.pic.events.ShareEvent}.
	 */
	public void onEvent(ShareEvent e) {
		PhotoDB photoDB = (PhotoDB) e.getObject();
		com.nasa.pic.utils.Utils.share(photoDB, e.getIntent());
	}


	/**
	 * Handler for {@link com.nasa.pic.events.CompleteShareEvent}.
	 *
	 * @param e Event {@link com.nasa.pic.events.CompleteShareEvent}.
	 */
	public void onEvent(CompleteShareEvent e) {
		ActivityCompat.startActivity(this, e.getIntent(), null);
	}

	/**
	 * Handler for {@link com.nasa.pic.events.OpenPhotoEvent}.
	 *
	 * @param e Event {@link com.nasa.pic.events.OpenPhotoEvent}.
	 */
	public void onEvent(OpenPhotoEvent e) {
		PhotoDB photoDB = (PhotoDB) e.getObject();
		if (e.getThumbnail() != null) {
			e.getThumbnail()
			 .setSource(photoDB.getUrls()
			                   .getNormal());
			PhotoViewActivity.showInstance(this,
			                               TextUtils.isEmpty(photoDB.getTitle()) ?
			                               "" :
			                               photoDB.getTitle(),
			                               photoDB.getDescription(),
			                               TextUtils.isEmpty(photoDB.getUrls()
			                                                        .getHd()) ?
			                               photoDB.getUrls()
			                                      .getNormal() :
			                               photoDB.getUrls()
			                                      .getHd(),
			                               photoDB.getUrls()
			                                      .getNormal(),
			                               photoDB.getDate(),
			                               photoDB.getType(),
			                               e.getThumbnail());
			mItemBinding = e.getItemBinding();
		} else {
			PhotoViewActivity.showInstance(this,
			                               TextUtils.isEmpty(photoDB.getTitle()) ?
			                               "" :
			                               photoDB.getTitle(),
			                               photoDB.getDescription(),
			                               TextUtils.isEmpty(photoDB.getUrls()
			                                                        .getHd()) ?
			                               photoDB.getUrls()
			                                      .getNormal() :
			                               photoDB.getUrls()
			                                      .getHd(),
			                               photoDB.getUrls()
			                                      .getNormal(),
			                               photoDB.getDate(),
			                               photoDB.getType());
		}
	}

	//------------------------------------------------

	protected abstract int getMenuRes();


	protected abstract void initMenu();

	@Override
	protected void onResume() {
		super.onResume();
		revertLastSelectedListItemView();
	}


	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				ActivityCompat.finishAfterTransition(this);
			case R.id.action_about:
				showDialogFragment(AboutDialogFragment.newInstance(this), null);
				break;
		}
		return true;
	}


	protected void buildListView(Context cxt, RecyclerView recyclerView) {
		ScreenSize screenSize = DeviceUtils.getScreenSize(App.Instance);
		int width = screenSize.Width - getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) * 2;
		LL.d("Screen width: " + width);
		float basic = cxt.getResources()
		                 .getDimension(R.dimen.basic_card_width);
		LL.d("Basic: " + basic);
		float div = width / basic;
		LL.d("Div: " + div);
		BigDecimal cardCount = new BigDecimal(div).setScale(0, BigDecimal.ROUND_HALF_UP);
		LL.d("CardCount: " + cardCount);
		recyclerView.setLayoutManager(new GridLayoutManager(cxt, cardCount.intValue()));
		mCellSize = (int) (width / div);
		LL.d("CardSize: " + mCellSize);
	}

	@Override
	protected Class<? extends RealmObject> getDataClazz() {
		return PhotoDB.class;
	}

	protected void initPull2Load(SwipeRefreshLayout swipeRefreshLayout) {
		int actionbarHeight = Utils.getActionBarHeight(App.Instance);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadList();
			}
		});
		swipeRefreshLayout.setColorSchemeResources(R.color.c_refresh_1, R.color.c_refresh_2, R.color.c_refresh_3, R.color.c_refresh_4);
		swipeRefreshLayout.setProgressViewEndTarget(true, actionbarHeight * 2);
		swipeRefreshLayout.setProgressViewOffset(false, 0, actionbarHeight * 2);
		swipeRefreshLayout.setRefreshing(true);
	}

	/**
	 * Show  {@link android.support.v4.app.DialogFragment}.
	 *
	 * @param _dlgFrg  An instance of {@link android.support.v4.app.DialogFragment}.
	 * @param _tagName Tag name for dialog, default is "dlg". To grantee that only one instance of {@link
	 *                 android.support.v4.app.DialogFragment} can been seen.
	 */
	protected void showDialogFragment(DialogFragment _dlgFrg, String _tagName) {
		try {
			if (_dlgFrg != null) {
				DialogFragment dialogFragment = _dlgFrg;
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				// Ensure that there's only one dialog to the user.
				Fragment prev = getSupportFragmentManager().findFragmentByTag("dlg");
				if (prev != null) {
					ft.remove(prev);
				}
				try {
					if (TextUtils.isEmpty(_tagName)) {
						dialogFragment.show(ft, "dlg");
					} else {
						dialogFragment.show(ft, _tagName);
					}
				} catch (Exception _e) {
				}
			}
		} catch (Exception _e) {
		}
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		com.nasa.pic.utils.Utils.fullScreen(this);
		super.onCreate(savedInstanceState);
		initMenu();
	}


	protected int getCellSize() {
		return mCellSize;
	}


	private void revertLastSelectedListItemView() {
		if (mItemBinding == null) {
			return;
		}
		ValueAnimatorCompat animator = AnimatorCompatHelper.emptyValueAnimator();
		animator.setDuration(TransitCompat.ANIM_DURATION * 4);
		animator.addUpdateListener(new AnimatorUpdateListenerCompat() {
			private float oldAlpha = 0;
			private float endAlpha = 1;
			private float oldEle = App.Instance.getResources()
			                                   .getDimension(R.dimen.cardElevationSelected);
			private float endEle = App.Instance.getResources()
			                                   .getDimension(R.dimen.cardElevationNormal);
			private Interpolator interpolator2 = new BakedBezierInterpolator();

			@Override
			public void onAnimationUpdate(ValueAnimatorCompat animation) {
				float fraction = interpolator2.getInterpolation(animation.getAnimatedFraction());
				//Set background alpha
				float alpha = oldAlpha + (fraction * (endAlpha - oldAlpha));
				ViewCompat.setAlpha(mItemBinding.thumbnailIv, alpha);
				//Set frame on cardview.
				float ele = oldEle + (fraction * (endEle - oldEle));
				mItemBinding.photoCv.setCardElevation(ele);
				mItemBinding.photoCv.setMaxCardElevation(ele);
			}
		});
		animator.start();
	}

}
