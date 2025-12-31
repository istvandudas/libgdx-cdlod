package org.cdlod.terrain.material;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.cdlod.terrain.material.addon.MaterialAddon;
import org.cdlod.terrain.shader.TerrainShader;
import org.cdlod.terrain.shader.TerrainShaderUniformLocation;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureWrap.ClampToEdge;

@Data
@Accessors(fluent = true, chain = true)
public class TerrainMaterial {
	public static final Vector3 DEFAULT_LIGHT_DIR = new Vector3(0.0f, -1.0f, 0.0f);
	public static final Color DEFAULT_LIGHT_COLOR = new Color(1f, 1f, 0.95f, 1.0f);
	public static final Color DEFAULT_AMBIENT_COLOR = new Color(0.8f, 0.8f, 0.8f, 1.0f);
	public static final Color DEFAULT_SCATTER_COLOR = new Color(0.6f, 0.75f, 1.0f, 1.0f);
	public static final Color DEFAULT_FOG_COLOR = new Color(0.6f, 0.75f, 1.0f, 1.0f);
	public static final float DEFAULT_SCATTER_STRENGTH = 0.0002f;
	public static final float DEFAULT_FOG_DENSITY = 0.0005f;

	private final @NonNull TerrainShader shader;
	private Texture height;
	private Texture diffuse;
	private Texture normal;
	private final TerrainShaderUniformLocation uniformLocation;

	private float heightScale = 1.0f;
	private float heightOffset = 0.0f;
	private float gridResolution;
	private float terrainSize;
	private float diffuseTiling = 1.0f;
	private float normalTiling = 1.0f;
	private boolean hasNormalMap = true;

	private final Vector3 lightDir = DEFAULT_LIGHT_DIR.cpy();
	private final Color lightColor = DEFAULT_LIGHT_COLOR.cpy();
	private final Color ambientColor = DEFAULT_AMBIENT_COLOR.cpy();

	private final Color scatterColor = DEFAULT_SCATTER_COLOR.cpy();
	private float scatterStrength = DEFAULT_SCATTER_STRENGTH;

	private final Color fogColor = DEFAULT_FOG_COLOR.cpy();
	private float fogDensity = DEFAULT_FOG_DENSITY;

	private boolean distantFogEnabled;
	private boolean atmosphericScatterEnabled;

	private boolean updateTerrainSettings = true;
	private boolean updateLightSettings = true;

	private final List<MaterialAddon> addons = new ArrayList<>();

	public TerrainMaterial(
			@NonNull FileHandle heightHandle,
			@NonNull FileHandle diffuseHandle,
			FileHandle normalHandle) {
		shader = new TerrainShader();
		uniformLocation = new TerrainShaderUniformLocation(shader);
		height = loadHeightMapTexture(heightHandle);
		diffuse = loadTexture(diffuseHandle);
		if (normalHandle != null) {
			hasNormalMap = true;
			normal = loadTexture(normalHandle);
		} else {
			hasNormalMap = false;
		}
	}

	public void resetToDefault() {
		lightDir.set(DEFAULT_LIGHT_DIR);
		lightColor.set(DEFAULT_LIGHT_COLOR);
		ambientColor.set(DEFAULT_AMBIENT_COLOR);
		scatterColor.set(DEFAULT_SCATTER_COLOR);
		scatterStrength = DEFAULT_SCATTER_STRENGTH;
		fogColor.set(DEFAULT_FOG_COLOR);
		fogDensity = DEFAULT_FOG_DENSITY;
		updateLightSettings = true;
	}

	public void update(float deltaTime) {
		for (int i = 0; i < addons.size(); i++) {
			MaterialAddon addon = addons.get(i);
			if (addon.enabled()) {
				addon.update(deltaTime);
			}
		}
	}

	public void apply(Camera camera) {
		shader.bind();
		for (int i = 0; i < addons.size(); i++) {
			MaterialAddon addon = addons.get(i);
			if (addon.enabled()) {
				addon.apply(this);
			}
		}
		shader.setUniformMatrix(uniformLocation.projViewTrans(), camera.combined);
		shader.setUniformf(uniformLocation.cameraNear(), camera.near);
		shader.setUniformf(uniformLocation.cameraFar(), camera.far);
		height.bind(0);
		diffuse.bind(1);
		if (hasNormalMap) {
			normal.bind(2);
		}

		if (updateLightSettings) {
			shader.setUniformf(uniformLocation.lightDir(), lightDir);
			shader.setUniformf(uniformLocation.lightColor(), lightColor.r, lightColor.g, lightColor.b);
			shader.setUniformf(uniformLocation.ambientColor(), ambientColor.r, ambientColor.g, ambientColor.b);
			shader.setUniformf(uniformLocation.scatterColor(), scatterColor.r, scatterColor.g, scatterColor.b);
			if (atmosphericScatterEnabled) {
				shader.setUniformf(uniformLocation.scatterStrength(), scatterStrength);
			} else {
				shader.setUniformf(uniformLocation.scatterStrength(), 0.0f);
			}
			shader.setUniformf(uniformLocation.fogColor(), fogColor.r, fogColor.g, fogColor.b);
			if (distantFogEnabled) {
				shader.setUniformf(uniformLocation.fogDensity(), fogDensity);
			} else {
				shader.setUniformf(uniformLocation.fogDensity(), 0.0f);
			}
			updateLightSettings = false;
		}

		if (updateTerrainSettings) {
			shader.setUniformf(uniformLocation.heightScale(), heightScale);
			shader.setUniformf(uniformLocation.heightOffset(), heightOffset);
			shader.setUniformf(uniformLocation.gridResolution(), gridResolution);
			shader.setUniformf(uniformLocation.terrainSize(), terrainSize);
			shader.setUniformf(uniformLocation.diffuseTiling(), diffuseTiling);
			shader.setUniformf(uniformLocation.normalTiling(), normalTiling);
			shader.setUniformi(uniformLocation.heightTexture(), 0);
			shader.setUniformi(uniformLocation.diffuseTexture(), 1);
			shader.setUniformf(uniformLocation.hasNormalMap(), normal == null ? 0.0f : 1.0f);
			if (hasNormalMap) {
				shader.setUniformi(uniformLocation.normalTexture(), 2);
			}
			updateTerrainSettings = false;
		}
	}

	private Texture loadHeightMapTexture(FileHandle fileHandle) {
		Texture tex = new Texture(fileHandle);
		if (tex.getTextureData().getFormat() != Pixmap.Format.RGBA8888) {
			throw new RuntimeException("Height map texture must be " + Pixmap.Format.RGBA8888 +
					"! (not " + tex.getTextureData().getFormat() + ")");
		}
		tex.setFilter(Linear, Linear);
		tex.setWrap(ClampToEdge, ClampToEdge);
		return tex;
	}

	private Texture loadTexture(FileHandle fileHandle) {
		Texture tex = new Texture(fileHandle);
		tex.setFilter(Linear, Linear);
		return tex;
	}

	public void distantFogEnabled(boolean distantFogEnabled) {
		this.distantFogEnabled = distantFogEnabled;
		updateLightSettings = true;
	}

	public void atmosphericScatterEnabled(boolean atmosphericScatterEnabled) {
		this.atmosphericScatterEnabled = atmosphericScatterEnabled;
		updateLightSettings = true;
	}

	public static TerrainMaterialBuilder builder() {
		return new TerrainMaterialBuilder();
	}

}
