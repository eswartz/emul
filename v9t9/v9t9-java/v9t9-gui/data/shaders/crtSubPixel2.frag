uniform sampler2D canvasTexture;
uniform ivec2 visible;
uniform ivec2 viewport;

const ivec2 monSize = ivec2(320, 240);

void main()
{
    vec4 color = texture2D(canvasTexture, gl_TexCoord[0].st);
    
    vec2 lines;
    
    // physical pixel
    vec2 xy = gl_TexCoord[0].st * vec2(viewport);
    
    int channel = int(mod(xy.x, 3.));
    vec4 colorBase = color / 4.;
    vec4 rgba;
    if (channel == 0) {
    	rgba = vec4(color.r, colorBase.g, colorBase.b, 1.0);
    }
    else if (channel == 1) {
    	rgba = vec4(colorBase.r, color.g, colorBase.b, 1.0);
    }
    else {
    	rgba = vec4(colorBase.r, colorBase.g, color.b, 1.0);
    }

	float yline = max(0.75, fract(xy.y / 5.));
	float lum = (rgba.r+rgba.g+rgba.b)/3.;
	
	rgba += sin(lum*3.14159/2.) * .75;
    gl_FragColor = (rgba) * yline ;
}