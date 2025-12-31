package org.cdlod.terrain;

import com.badlogic.gdx.math.Vector3;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = false)
public class QuadTree {
	public static final int NORTH_WEST_QUAD = 0;
	public static final int NORTH_EAST_QUAD = 1;
	public static final int SOUTH_WEST_QUAD = 2;
	public static final int SOUTH_EAST_QUAD = 3;

	private final QuadTreeNode root;
	private final int maxLOD;

	public QuadTree(int terrainSize, int maxLOD) {
		this.maxLOD = maxLOD;
		this.root = buildNode(0, 0, terrainSize, 0);
	}

	private QuadTreeNode buildNode(int x, int z, int size, int lod) {
		if (lod >= maxLOD) {
			return new QuadTreeNode(x, z, size, lod);
		}
		QuadTreeNode[] children = new QuadTreeNode[4];
		QuadTreeNode node = new QuadTreeNode(x, z, size, lod, children);
		int half = size / 2;
		children[NORTH_WEST_QUAD] = buildNode(x, z, half, lod + 1);
		children[NORTH_EAST_QUAD] = buildNode(x + half, z, half, lod + 1);
		children[SOUTH_WEST_QUAD] = buildNode(x, z + half, half, lod + 1);
		children[SOUTH_EAST_QUAD] = buildNode(x + half, z + half, half, lod + 1);
		return node;
	}

	public void computeBoundingBoxes(Terrain terrain) {
		computeBoundingBox(root, terrain);
	}

	private void computeBoundingBox(QuadTreeNode node, Terrain terrain) {
		if (node.isLeaf()) {
			float minH = 0.0f;
			float maxH = 0.0f;
			boolean first = true;
			for (int z = node.worldZ(); z < node.worldZ() + node.size(); z++) {
				for (int x = node.worldX(); x < node.worldX() + node.size(); x++) {
					float height = terrain.getHeight(x, z);
					if (first) {
						minH = height;
						maxH = height;
						first = false;
					} else {
						minH = Math.min(minH, height);
						maxH = Math.max(maxH, height);
					}
				}
			}
			node.bounds().clr().set(
					new Vector3(node.worldX(), minH, node.worldZ()),
					new Vector3(node.worldX() + node.size(), maxH, node.worldZ() + node.size())
			);
		} else {
			float minH = Float.MAX_VALUE;
			for (int i = 0; i < 4; i++) {
				QuadTreeNode child = node.children()[i];
				computeBoundingBox(child, terrain);
				minH = Math.min(minH, child.bounds().min.y);
			}
			Vector3 min = new Vector3(node.worldX(), minH, node.worldZ());
			node.bounds().set(min, min);
			for (int i = 0; i < 4; i++) {
				QuadTreeNode child = node.children()[i];
				node.bounds().ext(child.bounds());
			}
		}
	}
}
