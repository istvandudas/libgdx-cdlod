package org.cdlod.terrain.shader;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import lombok.Data;
import lombok.experimental.Accessors;

import static org.cdlod.terrain.shader.TerrainShaderUniform.*;

@Data
@Accessors(fluent = true, chain = false)
public class TerrainShaderUniformLocation {
	private int projViewTrans;
	private int heightScale;
	private int heightOffset;
	private int gridResolution;
	private int terrainSize;
	private int diffuseTiling;
	private int normalTiling;
	private int heightTexture;
	private int diffuseTexture;
	private int normalTexture;
	private int hasNormalMap;
	private int cameraNear;
	private int cameraFar;
	private int patchOffset;
	private int patchSize;
	private int morphFactor;
	private int lightDir;
	private int lightColor;
	private int ambientColor;
	private int scatterColor;
	private int scatterStrength;
	private int fogColor;
	private int fogDensity;

	public TerrainShaderUniformLocation(ShaderProgram shaderProgram) {
		updateLocations(shaderProgram);
	}

	public void updateLocations(ShaderProgram shaderProgram) {
		projViewTrans = shaderProgram.getUniformLocation(PROJ_VIEW_TRANS);
		heightScale = shaderProgram.getUniformLocation(HEIGHT_SCALE);
		heightOffset = shaderProgram.getUniformLocation(HEIGHT_OFFSET);
		gridResolution = shaderProgram.getUniformLocation(GRID_RESOLUTION);
		terrainSize = shaderProgram.getUniformLocation(TERRAIN_SIZE);
		diffuseTiling = shaderProgram.getUniformLocation(DIFFUSE_TILING);
		normalTiling = shaderProgram.getUniformLocation(NORMAL_TILING);
		heightTexture = shaderProgram.getUniformLocation(HEIGHT_TEXTURE);
		diffuseTexture = shaderProgram.getUniformLocation(DIFFUSE_TEXTURE);
		normalTexture = shaderProgram.getUniformLocation(NORMAL_TEXTURE);
		hasNormalMap = shaderProgram.getUniformLocation(HAS_NORMAL_MAP);
		cameraNear = shaderProgram.getUniformLocation(CAMERA_NEAR);
		cameraFar = shaderProgram.getUniformLocation(CAMERA_FAR);
		patchOffset = shaderProgram.getUniformLocation(PATCH_OFFSET);
		patchSize = shaderProgram.getUniformLocation(PATCH_SIZE);
		morphFactor = shaderProgram.getUniformLocation(MORPH_FACTOR);
		lightDir = shaderProgram.getUniformLocation(LIGHT_DIR);
		lightColor = shaderProgram.getUniformLocation(LIGHT_COLOR);
		ambientColor = shaderProgram.getUniformLocation(AMBIENT_COLOR);
		scatterColor = shaderProgram.getUniformLocation(SCATTER_COLOR);
		scatterStrength = shaderProgram.getUniformLocation(SCATTER_STRENGTH);
		fogColor = shaderProgram.getUniformLocation(FOG_COLOR);
		fogDensity = shaderProgram.getUniformLocation(FOG_DENSITY);
	}
}
