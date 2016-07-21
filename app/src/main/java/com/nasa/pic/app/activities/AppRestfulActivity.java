package com.nasa.pic.app.activities;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.MenuItem;

import com.chopping.activities.RestfulActivity;
import com.chopping.application.BasicPrefs;
import com.nasa.pic.R;
import com.nasa.pic.app.fragments.AboutDialogFragment;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.events.CompleteShareEvent;
import com.nasa.pic.events.FBShareEvent;
import com.nasa.pic.events.ShareEvent;
import com.nasa.pic.utils.Prefs;

import io.realm.RealmObject;


public abstract class AppRestfulActivity extends RestfulActivity implements OnMenuItemClickListener {


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




	//------------------------------------------------

	protected abstract int getMenuRes();


	protected abstract void initMenu();



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




	@Override
	protected Class<? extends RealmObject> getDataClazz() {
		return PhotoDB.class;
	}



	/**
	 * Show  {@link android.support.v4.app.DialogFragment}.
	 *
	 * @param dialogFragment  An instance of {@link android.support.v4.app.DialogFragment}.
	 * @param _tagName Tag name for dialog, default is "dlg". To grantee that only one instance of {@link
	 *                 android.support.v4.app.DialogFragment} can been seen.
	 */
	protected void showDialogFragment(DialogFragment dialogFragment, String _tagName) {
		try {
			if (dialogFragment != null) {
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
				} catch (Exception ignored) {
				}
			}
		} catch (Exception ignored) {
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

}
