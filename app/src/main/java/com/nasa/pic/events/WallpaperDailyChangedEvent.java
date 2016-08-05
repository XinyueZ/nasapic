package com.nasa.pic.events;

public final class WallpaperDailyChangedEvent {
	private final String message;

	public WallpaperDailyChangedEvent(String changed) {
		message = changed;
	}

	public String getMessage() {
		return message;
	}
}
