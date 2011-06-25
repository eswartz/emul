
void main()
{   
    //gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_TexCoord[0] =  gl_MultiTexCoord0;
    gl_FrontColor = vec4(1,1,1,1);
    gl_Position = ftransform();
}
 