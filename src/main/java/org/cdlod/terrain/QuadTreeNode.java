package org.cdlod.terrain;

import com.badlogic.gdx.math.collision.BoundingBox;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true, chain = true)
public class QuadTreeNode {
	private final int worldX;
	private final int worldZ;
	private final int size;
	private final int lod;
	private final int centerX;
	private final int centerZ;
	private final QuadTreeNode[] children;
	private final BoundingBox bounds = new BoundingBox();

	public QuadTreeNode(int worldX, int worldZ, int size, int lod) {
		this(worldX, worldZ, size, lod, null);
	}

	public QuadTreeNode(int worldX, int worldZ, int size, int lod, QuadTreeNode[] children) {
		this.worldX = worldX;
		this.worldZ = worldZ;
		this.size = size;
		this.lod = lod;
		int half = size / 2;
		centerX = this.worldX + half;
		centerZ = this.worldZ + half;
		this.children = children;
	}

	public boolean isLeaf() {
		return children == null;
	}
}
