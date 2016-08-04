package com.nasa.pic.events;

public final class WallpaperChangingEvent {

	private final String mMessage;

	public WallpaperChangingEvent(String message) {

		mMessage = message;
	}

	public String getMessage() {
		return mMessage;
	}
}
