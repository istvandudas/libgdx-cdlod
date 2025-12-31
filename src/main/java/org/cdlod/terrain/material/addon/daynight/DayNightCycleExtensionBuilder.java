package org.cdlod.terrain.material.addon.daynight;

import com.badlogic.gdx.graphics.Color;

public class DayNightCycleExtensionBuilder {
	private final DayNightCycleAddon extension = new DayNightCycleAddon();

	public DayNightCycleExtensionBuilder enabled(boolean enabled) {
		extension.enabled(enabled);
		return this;
	}

	public DayNightCycleExtensionBuilder day(float timeOfDay, float speed) {
		extension.timeOfDay = timeOfDay;
		extension.speed = speed;
		return this;
	}

	public DayNightCycleExtensionBuilder color(
			Color noon, Color sunset, Color night,
			Color day, Color nightSky, Color daySky
	) {
		extension.noonColor().set(noon);
		extension.sunsetColor().set(sunset);
		extension.nightColor().set(night);
		extension.dayColor().set(day);
		extension.nightSkyColor().set(nightSky);
		extension.daySkyColor().set(daySky);
		return this;
	}

	public DayNightCycleAddon build() {
		return extension;
	}

}
