package com.tuacy.slidemenu;

public enum SlideMenuMode {

	NONE(0x0),
	LEFT(0x1),
	RIGHT(0x2),
	BOTH(0x3);

	static SlideMenuMode int2Value(final int modeInt) {
		for (SlideMenuMode value : SlideMenuMode.values()) {
			if (modeInt == value.getValue()) {
				return value;
			}
		}
		// If not, return default
		return getDefault();
	}

	static SlideMenuMode getDefault() {
		return NONE;
	}

	private int mValue;

	SlideMenuMode(int value) {
		mValue = value;
	}

	int getValue() {
		return mValue;
	}

}
