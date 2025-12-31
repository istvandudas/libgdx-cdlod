package org.cdlod.terrain;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.cdlod.terrain.material.TerrainMaterial;
import org.cdlod.terrain.shader.TerrainShaderUniformLocation;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true, chain = false)
public class Terrain {
	private final TerrainMaterial material;
	private final QuadTree quadTree;
	private final Mesh sharedMesh;
	private final List<QuadTreeNode> visibleNodes = new ArrayList<>();
	private final float[][] heights;

	public Terrain(TerrainMaterial material, int maxLOD) {
		this.material = material;
		sharedMesh = createMesh();
		int terrainSize = (int) material.terrainSize();
		quadTree = new QuadTree(terrainSize, maxLOD);
		heights = produceHeights(material.height(), material.heightScale(), material.heightOffset());
		quadTree.computeBoundingBoxes(this);
	}

	public void update(float deltaTime) {
		material.update(deltaTime);
	}

	public float getHeight(float x, float z) {
		float size = material.height().getWidth();
		// Convert world coords to normalized UVs
		float u = x / material.terrainSize();
		float v = z / material.terrainSize();

		// Clamp
		u = Math.max(0f, Math.min(1f, u));
		v = Math.max(0f, Math.min(1f, v));

		// Convert to texture pixel coordinates
		float fx = u * (size - 1);
		float fz = v * (size - 1);

		// Integer sample points
		int x0 = (int) fx;
		int z0 = (int) fz;
		int x1 = Math.min(x0 + 1, (int) size - 1);
		int z1 = Math.min(z0 + 1, (int) size - 1);

		// Fractional parts
		float tx = fx - x0;
		float tz = fz - z0;

		// ---------------------------------------------------------
		// IMPORTANT:
		// heights[][] is stored in IMAGE SPACE (top-left origin)
		// OpenGL texture sampling is BOTTOM-LEFT origin
		//
		// So we must FLIP Z when indexing the height array.
		// ---------------------------------------------------------
		int iz0 = (int) size - 1 - z0;
		int iz1 = (int) size - 1 - z1;

		// Bilinear sampling using flipped Z
		float h00 = heights[iz0][x0];
		float h10 = heights[iz0][x1];
		float h01 = heights[iz1][x0];
		float h11 = heights[iz1][x1];

		float hx0 = h00 * (1 - tx) + h10 * tx;
		float hx1 = h01 * (1 - tx) + h11 * tx;

		return hx0 * (1 - tz) + hx1 * tz;
	}

	private Mesh createMesh() {
		int gridSize = (int) material.gridResolution();
		int vertexCount = gridSize * gridSize;
		int indexCount = (gridSize - 1) * (gridSize - 1) * 6;

		float[] vertices = new float[vertexCount * 5];
		short[] index = new short[indexCount];

		int vIdx = 0;

		// ---------------------------------------------------------
		//  Vertex generation
		//    - World space: (0,0) bottom-left
		//    - Texture space: (0,0) bottom-left
		//    - Z increases forward
		// ---------------------------------------------------------
		for (int z = 0; z < gridSize; z++) {
			for (int x = 0; x < gridSize; x++) {

				float fx = (float) x / (gridSize - 1);  // U
				float fz = (float) z / (gridSize - 1);  // V

				vertices[vIdx++] = x;     // world X
				vertices[vIdx++] = 0f;    // world Y
				vertices[vIdx++] = z;    // world Z

				vertices[vIdx++] = fx;    // tex U
				vertices[vIdx++] = fz;    // tex V
			}
		}

		// ---------------------------------------------------------
		// Index generation (CCW winding)
		//    Quad:
		//      a ---- b
		//      |    / |
		//      |  /   |
		//      c ---- d
		//
		//    Triangles:
		//      a, c, b
		//      b, c, d
		// ---------------------------------------------------------
		int iIdx = 0;
		for (int z = 0; z < gridSize - 1; z++) {
			for (int x = 0; x < gridSize - 1; x++) {

				short a = (short) (z * gridSize + x);
				short b = (short) (z * gridSize + x + 1);
				short c = (short) ((z + 1) * gridSize + x);
				short d = (short) ((z + 1) * gridSize + x + 1);

				// CCW
				index[iIdx++] = a;
				index[iIdx++] = c;
				index[iIdx++] = b;

				index[iIdx++] = b;
				index[iIdx++] = c;
				index[iIdx++] = d;
			}
		}

		Mesh mesh = new Mesh(
				true,
				vertexCount,
				indexCount,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
		);

		mesh.setVertices(vertices);
		mesh.setIndices(index);

		return mesh;
	}

	public void render(Camera cam) {
		material.apply(cam);
		ShaderProgram shader = material.shader();
		TerrainShaderUniformLocation uniformLocation = material.uniformLocation();
		calculateVisibleNodes(cam, visibleNodes);
		for (int i = 0; i < visibleNodes.size(); i++) {
			QuadTreeNode p = visibleNodes.get(i);
			shader.setUniformf(uniformLocation.patchOffset(), p.worldX(), p.worldZ());
			shader.setUniformf(uniformLocation.patchSize(), p.size());
			shader.setUniformf(uniformLocation.morphFactor(), computeMorphFactor(cam, p));
			sharedMesh.render(shader, GL20.GL_TRIANGLES);
		}
	}

	private void calculateVisibleNodes(Camera cam, List<QuadTreeNode> out) {
		out.clear();
		collectNodes(cam, quadTree.root(), out);
	}

	private void collectNodes(Camera cam, QuadTreeNode node, List<QuadTreeNode> out) {
		if (!cam.frustum.boundsInFrustum(node.bounds())) {
			return;
		}
		float dx = cam.position.x - node.centerX();
		float dz = cam.position.z - node.centerZ();
		float dist = (float) Math.sqrt(dx * dx + dz * dz);
		float morphEnd = node.size() * 4f;
		if (dist > morphEnd || node.isLeaf()) {
			out.add(node);
		} else {
			for (int i = 0; i < 4; i++) {
				collectNodes(cam, node.children()[i], out);
			}
		}
	}

	private float computeMorphFactor(Camera cam, QuadTreeNode p) {
		float distance = cam.position.dst(p.centerX(), 0, p.centerZ());
		float morphStart = p.size() * 2f;
		float morphEnd = p.size() * 4f;
		return MathUtils.clamp((distance - morphStart) / (morphEnd - morphStart), 0f, 1f);
	}

	private float[][] produceHeights(@NonNull Texture heightMap, float heightScale, float heightOffset) {
		TextureData data = heightMap.getTextureData();
		if (!data.isPrepared()) {
			data.prepare();
		}
		Pixmap pixmap = data.consumePixmap();
		int w = pixmap.getWidth();
		int h = pixmap.getHeight();
		float[][] heights = new float[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int flippedY = pixmap.getHeight() - 1 - y;
				int pixel = pixmap.getPixel(x, flippedY);
				int height16 = (pixel >> 16) & 0xFFFF;
				heights[y][x] = (float) height16 * heightScale + heightOffset;
			}
		}
		return heights;
	}

	public static TerrainBuilder builder() {
		return new TerrainBuilder();
	}

}