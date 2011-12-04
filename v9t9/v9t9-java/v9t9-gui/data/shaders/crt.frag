uniform sampler2D canvasTexture;
uniform ivec2 visible;
uniform ivec2 viewport;

void main()
{
    vec4 color = texture2D(canvasTexture, gl_TexCoord[0].st);
    
    // only shade if scaling up
    vec2 lines;
    if (viewport.y > 384) {
        vec2 fract_center_dist = fract((vec2(0.25, 0.25) + gl_TexCoord[0].st) * vec2(visible.x, visible.y));
        vec2 center_dist = abs(step(vec2(0.5, 0.5), fract_center_dist) - fract_center_dist);
        // vertical lines have are strongest shading
        lines = min(vec2(1.0, 1.0), center_dist + vec2(0.75, 0.5));
        
        vec4 vvec = vec4(lines.y, lines.y, lines.y, 1.0);
        vec4 hvec;
        if (viewport.x > 256)
            hvec = vec4(lines.x, lines.x, lines.x, 1.0);
        else
            hvec = vec4(1,1,1,1);
        
        gl_FragColor = color * hvec * vvec * vec4(1.3, 1.3, 1.3, 1);
    } else {
        gl_FragColor = color;
    }
    
}