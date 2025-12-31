#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// UVs generated in the vertex shader
varying vec2 v_uvDiffuse;   // UVs for diffuse texture
varying vec2 v_uvNormal;    // UVs for normal map (if used)

// Terrain parameters
uniform float u_hasNormalMap;   // >0.5 → use normal map, else derive normals from heightmap
uniform float u_terrainSize;    // world‑space size of the terrain (used for texel size)

// Heightmap + textures
uniform sampler2D u_heightTex;  // RG16 heightmap
uniform sampler2D u_diffuseTex; // diffuse/albedo texture
uniform sampler2D u_normalTex;  // normal map (optional)

// Lighting
uniform vec3 u_lightDir;        // direction TO the light (should be normalized)
uniform vec3 u_lightColor;      // color/intensity of directional light
uniform vec3 u_ambientColor;    // ambient light contribution

// Fog
uniform vec3  u_fogColor;       // fog color
uniform float u_fogDensity;     // exponential fog density
uniform float u_near;           // camera near plane
uniform float u_far;            // camera far plane

// Atmospheric scattering
uniform vec3  u_scatterColor;     // horizon/sky tint
uniform float u_scatterStrength;  // how quickly scattering increases with distance

void main() {

    // ------------------------------------------------------------
    // 1. Sample diffuse/albedo texture
    // ------------------------------------------------------------
    vec4 diffuseColor = texture2D(u_diffuseTex, v_uvDiffuse);

    vec3 normal;

    // ------------------------------------------------------------
    // 2. Compute surface normal
    //    Option A: Use normal map
    //    Option B: Derive normal from heightmap (cheap fallback)
    // ------------------------------------------------------------
    if (u_hasNormalMap > 0.5) {

        // Normal map is stored in [0..1], convert to [-1..1]
        normal = texture2D(u_normalTex, v_uvNormal).xyz * 2.0 - 1.0;

    } else {

        // --- Derive normal from heightmap ---
        // Compute texel size in UV space
        float texel = 1.0 / u_terrainSize;

        // Sample heightmap around the current pixel
        float hL = texture2D(u_heightTex, v_uvDiffuse + vec2(-texel, 0.0)).r;
        float hR = texture2D(u_heightTex, v_uvDiffuse + vec2( texel, 0.0)).r;
        float hD = texture2D(u_heightTex, v_uvDiffuse + vec2(0.0, -texel)).r;
        float hU = texture2D(u_heightTex, v_uvDiffuse + vec2(0.0,  texel)).r;

        // Construct normal from height differences
        // X = left-right slope
        // Y = vertical scale (2.0 = exaggeration)
        // Z = down-up slope
        normal = normalize(vec3(hL - hR, 2.0, hD - hU));
    }

    // Ensure normal is normalized
    normal = normalize(normal);

    // ------------------------------------------------------------
    // 3. Lighting (Lambert + ambient)
    // ------------------------------------------------------------
    float NdotL = max(dot(normal, normalize(u_lightDir)), 0.0);

    vec3 directional = u_lightColor * NdotL;  // diffuse directional light
    vec3 ambient     = u_ambientColor;        // ambient light

    vec3 litColor = diffuseColor.rgb * (ambient + directional);

    // ------------------------------------------------------------
    // 4. Convert depth buffer value → linear depth
    //    gl_FragCoord.z is non-linear in OpenGL
    // ------------------------------------------------------------
    float depth = gl_FragCoord.z;

    // Convert depth to NDC space [-1..1]
    float z_ndc = depth * 2.0 - 1.0;

    // Convert NDC depth → linear depth (world units)
    float linearDepth =
    (2.0 * u_near * u_far) /
    (u_far + u_near - z_ndc * (u_far - u_near));

    // ------------------------------------------------------------
    // 5. Atmospheric scattering (distance-based horizon tint)
    // ------------------------------------------------------------
    float scatter = 1.0 - exp(-u_scatterStrength * linearDepth);
    scatter = clamp(scatter, 0.0, 1.0);

    vec3 scatteredColor = mix(litColor, u_scatterColor, scatter);

    // ------------------------------------------------------------
    // 6. Exponential fog
    // ------------------------------------------------------------
    float fogFactor = exp(-u_fogDensity * linearDepth);
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    // Fog blends over the scattered lighting
    vec3 finalColor = mix(u_fogColor, scatteredColor, fogFactor);

    // ------------------------------------------------------------
    // 7. Output final color
    // ------------------------------------------------------------
    gl_FragColor = vec4(finalColor, diffuseColor.a);
}