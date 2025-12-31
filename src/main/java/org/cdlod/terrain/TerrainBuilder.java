package org.cdlod.terrain;

import org.cdlod.terrain.material.TerrainMaterial;

import java.util.Objects;

public class TerrainBuilder {
	private TerrainMaterial material;
	private int lod;

	private boolean distantFogEnabled;
	private boolean atmosphericScatterFogEnabled;

	public TerrainBuilder material(TerrainMaterial material) {
		this.material = material;
		return this;
	}

	public TerrainBuilder fog(boolean distant, boolean atmosphericScatter) {
		distantFogEnabled = distant;
		atmosphericScatterFogEnabled = atmosphericScatter;
		return this;
	}

	public TerrainBuilder maxLOD(int maxLOD) {
		lod = maxLOD;
		return this;
	}

	public Terrain build() {
		Objects.requireNonNull(material, "Can't construct terrain without TerrainMaterial!");
		material.distantFogEnabled(distantFogEnabled);
		material.atmosphericScatterEnabled(atmosphericScatterFogEnabled);
		return new Terrain(material, lod);
	}

}
