package com.tuacy.slidemenu;

public enum SlideMenuAction {
	SCROLL(0x0),
	REVEAL(0x1);

	static SlideMenuAction int2Value(final int modeInt) {
		for (SlideMenuAction value : SlideMenuAction.values()) {
			if (modeInt == value.getValue()) {
				return value;
			}
		}
		// If not, return default
		return getDefault();
	}

	public static SlideMenuAction getDefault() {
		return SCROLL;
	}

	private int mValue;

	SlideMenuAction(int value) {
		mValue = value;
	}

	int getValue() {
		return mValue;
	}
}
