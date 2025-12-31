// Per‑vertex attributes from the mesh
attribute vec3 a_position;   // x,z = grid coordinates inside the patch, y unused
attribute vec2 a_texCoord;   // (not used here, CDLOD uses generated UVs)

// Camera + world transform
uniform mat4 u_projViewTrans;

// Heightmap + scaling
uniform sampler2D u_heightTex;
uniform float u_heightScale;     // scales heightmap values into world units
uniform float u_heightOffset;    // vertical offset (e.g., sea level)

// Patch placement + size
uniform vec2 u_patchOffset;      // world‑space offset of this patch (bottom‑left corner)
uniform float u_patchSize;       // world‑space size of this patch
uniform float u_gridResolution;  // number of vertices per side (e.g., 32, 64, 128)
uniform float u_terrainSize;     // full terrain size in world units

// Texture tiling
uniform float u_diffuseTiling;
uniform float u_normalTiling;

// CDLOD morphing
uniform float u_morphFactor;     // 0..1 morph amount between LOD levels
uniform float u_hasNormalMap;    // unused here but passed to fragment shader

// Outputs to fragment shader
varying vec2 v_uvDiffuse;
varying vec2 v_uvNormal;

void main() {

    // ------------------------------------------------------------
    // 1. Extract the grid coordinate inside the patch
    //    a_position.xz is in [0 .. gridResolution-1]
    // ------------------------------------------------------------
    vec2 grid = a_position.xz;

    // Maximum grid index (e.g., 32 for a 33×33 patch)
    float maxIndex = u_gridResolution - 1.0;

    // ------------------------------------------------------------
    // 2. Detect if this vertex is on the patch border
    //    Edge vertices must NOT morph (to avoid cracks)
    // ------------------------------------------------------------
    float isEdge =
    step(grid.x, 0.5) +                 // left edge
    step(grid.y, 0.5) +                 // top edge
    step(maxIndex - 0.5, grid.x) +      // right edge
    step(maxIndex - 0.5, grid.y);       // bottom edge

    // Clamp to 0 or 1
    isEdge = clamp(isEdge, 0.0, 1.0);

    // ------------------------------------------------------------
    // 3. Compute morph factor for this vertex
    //    - interior vertices morph normally
    //    - edge vertices force morph = 0
    // ------------------------------------------------------------
    float localMorph = mix(u_morphFactor, 0.0, isEdge);

    // ------------------------------------------------------------
    // 4. Compute parent LOD grid coordinate
    //    floor(grid * 0.5) * 2 snaps to even coordinates
    //    Example: 7 → floor(3.5)=3 → 3*2=6
    // ------------------------------------------------------------
    vec2 parentGrid = floor(grid * 0.5) * 2.0;

    // ------------------------------------------------------------
    // 5. Morph between current LOD grid and parent LOD grid
    // ------------------------------------------------------------
    vec2 morphedGrid = mix(grid, parentGrid, localMorph);

    // ------------------------------------------------------------
    // 6. Convert grid coordinate → local patch world coordinate
    //    Each grid step = patchSize / (gridResolution - 1)
    // ------------------------------------------------------------
    vec2 localPos = morphedGrid * (u_patchSize / (u_gridResolution - 1.0));

    // ------------------------------------------------------------
    // 7. Convert local patch coordinate → global world coordinate
    // ------------------------------------------------------------
    vec2 worldXZ = localPos + u_patchOffset;

    // ------------------------------------------------------------
    // 8. Compute UV for heightmap sampling
    //    Heightmap UVs are normalized across full terrain
    // ------------------------------------------------------------
    vec2 globaUV = worldXZ / u_terrainSize;

    // ------------------------------------------------------------
    // 9. Sample heightmap (RG16 encoded)
    //    height = r * 256*255 + g * 255
    // ------------------------------------------------------------
    vec2 rg = texture2D(u_heightTex, globaUV).rg;
    float h = rg.r * 255.0 * 256.0 + rg.g * 255.0;

    // Apply scale + offset to convert to world height
    float height = h * u_heightScale + u_heightOffset;

    // ------------------------------------------------------------
    // 10. Final world position of the vertex
    //     worldXZ.x = world X
    //     height    = world Y
    //     worldXZ.y = world Z
    // ------------------------------------------------------------
    vec3 worldPos = vec3(worldXZ.x, height, worldXZ.y);

    // ------------------------------------------------------------
    // 11. Output UVs for diffuse + normal maps
    // ------------------------------------------------------------
    v_uvDiffuse = globaUV * u_diffuseTiling;
    v_uvNormal  = globaUV * u_normalTiling;

    // ------------------------------------------------------------
    // 12. Final clip‑space position
    // ------------------------------------------------------------
    gl_Position = u_projViewTrans * vec4(worldPos, 1.0);
}