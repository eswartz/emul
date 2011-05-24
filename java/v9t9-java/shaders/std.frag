uniform sampler2D screenCanvas;

void main()
{
    vec4 color = texture2D(screenCanvas, gl_TexCoord[0].st);
    
    gl_FragColor = color;
}