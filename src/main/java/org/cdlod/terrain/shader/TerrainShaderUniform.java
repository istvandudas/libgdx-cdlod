package org.cdlod.terrain.shader;

public interface TerrainShaderUniform {
	String PROJ_VIEW_TRANS = "u_projViewTrans";
	String HEIGHT_SCALE = "u_heightScale";
	String HEIGHT_OFFSET = "u_heightOffset";
	String GRID_RESOLUTION = "u_gridResolution";
	String TERRAIN_SIZE = "u_terrainSize";
	String DIFFUSE_TILING = "u_diffuseTiling";
	String NORMAL_TILING = "u_normalTiling";
	String HEIGHT_TEXTURE = "u_heightTex";
	String DIFFUSE_TEXTURE = "u_diffuseTex";
	String NORMAL_TEXTURE = "u_normalTex";
	String HAS_NORMAL_MAP = "u_hasNormalMap";
	String CAMERA_NEAR = "u_near";
	String CAMERA_FAR = "u_far";
	String PATCH_OFFSET = "u_patchOffset";
	String PATCH_SIZE = "u_patchSize";
	String MORPH_FACTOR = "u_morphFactor";
	String LIGHT_DIR = "u_lightDir";
	String LIGHT_COLOR = "u_lightColor";
	String AMBIENT_COLOR = "u_ambientColor";
	String SCATTER_COLOR = "u_scatterColor";
	String SCATTER_STRENGTH = "u_scatterStrength";
	String FOG_COLOR = "u_fogColor";
	String FOG_DENSITY = "u_fogDensity";
}