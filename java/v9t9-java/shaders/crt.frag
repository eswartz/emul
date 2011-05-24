uniform sampler2D screenCanvas;
uniform ivec2 visible;
uniform ivec2 viewport;

void main()
{
    vec4 color = texture2D(screenCanvas, gl_TexCoord[0].st);
    
    float vline = viewport.y >= 384 ? min(1.0, fract((gl_TexCoord[0].t) * visible.y) + 0.4) : 1.0;
    float hline = viewport.x >= 512 ? min(1.0, fract((gl_TexCoord[0].s) * visible.x) + 0.75) : 1.0;
    
    // vertical lines have are strongest shading
     
    vec4 vvec = vec4(vline, vline, vline, 1.0);
    vec4 hvec = vec4(hline, hline, hline, 1.0);
    
    gl_FragColor = color * hvec * vvec;
}