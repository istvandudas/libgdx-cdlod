package org.cdlod.terrain.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TerrainShader extends ShaderProgram {
	public TerrainShader() {
		super(
				Gdx.files.classpath("shader/cdlod_terrain.vert"),
				Gdx.files.classpath("shader/cdlod_terrain.frag")
		);
		if (!isCompiled()) {
			throw new GdxRuntimeException("Shader compile error: " + getLog());
		}
	}
}
