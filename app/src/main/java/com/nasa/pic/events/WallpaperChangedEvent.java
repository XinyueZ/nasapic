package com.nasa.pic.events;

public final class WallpaperChangedEvent {
	private final CharSequence mMessage;

	public WallpaperChangedEvent(CharSequence message) {

		mMessage = message;
	}

	public CharSequence getMessage() {
		return mMessage;
	}
}
