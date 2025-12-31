package org.cdlod.terrain.material;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import lombok.NonNull;
import org.cdlod.terrain.material.addon.MaterialAddon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.cdlod.terrain.material.TerrainMaterial.*;

public class TerrainMaterialBuilder {
	private FileHandle heightHandle;
	private FileHandle diffuseHandle;
	private FileHandle normalHandle;
	private float heightScale = 1.0f;
	private float heightOffset = 0.0f;
	private float gridResolution;
	private float terrainSize;
	private float diffuseTiling = 1.0f;
	private float normalTiling = 1.0f;

	private final Vector3 lightDir = DEFAULT_LIGHT_DIR.cpy();
	private final Color lightColor = DEFAULT_LIGHT_COLOR.cpy();
	private final Color ambientColor = DEFAULT_AMBIENT_COLOR.cpy();

	private final Color scatterColor = DEFAULT_SCATTER_COLOR.cpy();
	private float scatterStrength = DEFAULT_SCATTER_STRENGTH;

	private final Color fogColor = DEFAULT_FOG_COLOR.cpy();
	private float fogDensity = DEFAULT_FOG_DENSITY;

	private final List<MaterialAddon> addons = new ArrayList<>();

	public TerrainMaterialBuilder textures(@NonNull FileHandle heightHandle, @NonNull FileHandle diffuseHandle) {
		this.heightHandle = heightHandle;
		this.diffuseHandle = diffuseHandle;
		return this;
	}

	public TerrainMaterialBuilder textures(
			@NonNull FileHandle heightHandle,
			@NonNull FileHandle diffuseHandle,
			FileHandle normalHandle
	) {
		this.heightHandle = heightHandle;
		this.diffuseHandle = diffuseHandle;
		this.normalHandle = normalHandle;
		return this;
	}

	public TerrainMaterialBuilder terrain(
			float terrainSize,
			float heightScale,
			float heightOffset,
			float gridResolution
	) {
		this.terrainSize = terrainSize;
		this.heightScale = heightScale;
		this.heightOffset = heightOffset;
		this.gridResolution = gridResolution;
		return this;
	}

	public TerrainMaterialBuilder tiling(float diffuseTiling, float normalTiling) {
		this.diffuseTiling = diffuseTiling;
		this.normalTiling = normalTiling;
		return this;
	}

	public TerrainMaterialBuilder directionalLight(@NonNull Vector3 lightDir, @NonNull Color lightColor) {
		this.lightDir.set(lightDir);
		this.lightColor.set(lightColor);
		return this;
	}

	public TerrainMaterialBuilder directionalLight(float x, float y, float z, float r, float g, float b) {
		this.lightDir.set(x, y, z);
		this.lightColor.set(r, g, b, 1.0f);
		return this;
	}

	public TerrainMaterialBuilder ambientLight(Color color) {
		ambientColor.set(color);
		return this;
	}

	public TerrainMaterialBuilder ambientLight(float r, float g, float b) {
		ambientColor.set(r, g, b, 1.0f);
		return this;
	}

	public TerrainMaterialBuilder atmosphericScatter(float strength, Color color) {
		scatterColor.set(color);
		scatterStrength = strength;
		return this;
	}

	public TerrainMaterialBuilder atmosphericScatter(float strength, float r, float g, float b) {
		scatterColor.set(r, g, b, 1.0f);
		scatterStrength = strength;
		return this;
	}

	public TerrainMaterialBuilder scatterStrength(float density, Color color) {
		fogDensity = density;
		fogColor.set(color);
		return this;
	}


	public TerrainMaterialBuilder fog(float density, Color color) {
		fogDensity = density;
		fogColor.set(color);
		return this;
	}

	public TerrainMaterialBuilder fog(float density, float r, float g, float b) {
		fogDensity = density;
		fogColor.set(r, g, b, 1.0f);
		return this;
	}

	public TerrainMaterialBuilder fogDensity(float density) {
		fogDensity = density;
		return this;
	}

	public TerrainMaterialBuilder addon(MaterialAddon addon) {
		addons.add(addon);
		return this;
	}

	public TerrainMaterial build() {
		Objects.requireNonNull(heightHandle, "Can't construct TerrainMaterial without Height Map!");
		Objects.requireNonNull(heightHandle, "Can't construct TerrainMaterial without Diffuse Map!");
		TerrainMaterial material = new TerrainMaterial(heightHandle, diffuseHandle, normalHandle);
		material.heightScale(heightScale);
		material.heightOffset(heightOffset);
		material.gridResolution(gridResolution);
		material.terrainSize(terrainSize);
		material.diffuseTiling(diffuseTiling);
		material.normalTiling(normalTiling);
		material.lightDir().set(lightDir);
		material.lightColor().set(lightColor);
		material.ambientColor().set(ambientColor);
		material.scatterColor().set(scatterColor);
		material.scatterStrength(scatterStrength);
		material.fogColor().set(fogColor);
		material.fogDensity(fogDensity);
		material.addons().addAll(addons);
		return material;
	}


}
