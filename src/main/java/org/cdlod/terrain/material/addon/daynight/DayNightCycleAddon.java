package org.cdlod.terrain.material.addon.daynight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cdlod.terrain.material.TerrainMaterial;
import org.cdlod.terrain.material.addon.MaterialAddon;

import static org.cdlod.terrain.material.TerrainMaterial.DEFAULT_LIGHT_COLOR;
import static org.cdlod.terrain.material.TerrainMaterial.DEFAULT_SCATTER_COLOR;

@Data
@Accessors(fluent = true, chain = false)
public class DayNightCycleAddon implements MaterialAddon {
	private boolean enabled = true;

	public float timeOfDay = 0.25f; // start at sunrise
	public float speed = 0.01f;     // day speed multiplier

	private final Vector3 sunDir = new Vector3();
	private final Color sunColorColor = new Color();
	private final Color ambientColor = new Color();
	private final Color scatterColor = new Color();
	private final Color fogColor = new Color();

	private final Color noonColor = new Color(1f, 0.45f, 0.2f, 1.0f);
	private final Color sunsetColor = DEFAULT_LIGHT_COLOR.cpy();

	private final Color nightColor = new Color(0.05f, 0.07f, 0.1f, 1.0f);
	private final Color dayColor = new Color(0.35f, 0.35f, 0.4f, 1.0f);

	private final Color nightSkyColor = new Color(0.05f, 0.1f, 0.2f, 1.0f);
	private final Color daySkyColor = DEFAULT_SCATTER_COLOR.cpy();

	public void update(float delta) {
		timeOfDay = (timeOfDay + delta * speed) % 1f;
		float angle = timeOfDay * MathUtils.PI2;
		sunDir.set(MathUtils.cos(angle), MathUtils.sin(angle), 0f).nor();
		float h = Math.max(0f, sunDir.y);
		sunColorColor.set(sunsetColor).lerp(noonColor, h);
		ambientColor.set(nightColor).lerp(dayColor, h);
		scatterColor.set(nightSkyColor).lerp(daySkyColor, h);
		fogColor.set(scatterColor);
	}

	public void apply(TerrainMaterial material) {
		material.lightDir().set(sunDir);
		material.lightColor().set(sunColorColor);
		material.ambientColor().set(ambientColor);
		material.scatterColor().set(scatterColor);
		material.fogColor().set(fogColor);
		material.updateLightSettings(true);
	}

	public static DayNightCycleExtensionBuilder builder() {
		return new DayNightCycleExtensionBuilder();
	}
}
