package org.cdlod.terrain.material.addon;

import org.cdlod.terrain.material.TerrainMaterial;

public interface MaterialAddon {
	boolean enabled();
	void enabled(boolean enabled);
	void update(float deltaTime);
	void apply(TerrainMaterial material);
}
