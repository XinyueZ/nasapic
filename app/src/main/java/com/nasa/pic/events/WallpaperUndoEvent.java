package com.nasa.pic.events;

public final class WallpaperUndoEvent {
	private final CharSequence mMessage;

	public WallpaperUndoEvent(CharSequence message) {
		mMessage = message;
	}

	public CharSequence getMessage() {
		return mMessage;
	}
}
