package com.nasa.pic.app.noactivities.wallpaper;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.events.WallpaperChangedEvent;
import com.nasa.pic.events.WallpaperChangingEvent;
import com.nasa.pic.events.WallpaperUndoEvent;

public abstract class WallpaperChangeDelegate {

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link WallpaperChangingEvent}.
	 * @param e Event {@link WallpaperChangingEvent}.
	 */
	public void onEventMainThread(WallpaperChangingEvent e) {
		Snackbar.make(getSnackBarAnchor(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
	}


	/**
	 * Handler for {@link WallpaperChangedEvent}.
	 * @param e Event {@link WallpaperChangedEvent}.
	 */
	public void onEventMainThread(WallpaperChangedEvent e) {
		Snackbar.make(getSnackBarAnchor(),e.getMessage(), Snackbar.LENGTH_LONG).setAction(R.string.wallpaper_change_undo, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SetWallpaperService.createService(App.Instance, null, true);
			}
		}).show();
	}


	/**
	 * Handler for {@link WallpaperUndoEvent}.
	 * @param e Event {@link WallpaperUndoEvent}.
	 */
	public void onEventMainThread(WallpaperUndoEvent e) {
		Snackbar.make(getSnackBarAnchor(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
	}

	//------------------------------------------------

	protected abstract View getSnackBarAnchor();
}
