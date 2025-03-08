package com.punkipunk.renderer;

import com.punkipunk.util.ResourceLoader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Para que OpenGL pueda utilizar el shader, debe compilarlo dinamicamente en tiempo de ejecucion a partir de su codigo fuente.
 */

public class Shader {

    private final String vertexPath;
    private final String fragmentPath;
    private final String vertexSource;
    private final String fragmentSource;
    private int shaderProgramID;
    private boolean beingUsed = false;

    public Shader(String vertexPath, String fragmentPath) {
        this.vertexPath = vertexPath;
        this.fragmentPath = fragmentPath;
        // Cargar los archivos de shaders
        vertexSource = ResourceLoader.loadAsString(vertexPath);
        fragmentSource = ResourceLoader.loadAsString(fragmentPath);
        assert vertexSource != null && fragmentSource != null : "Error: No se pudo abrir los archivos de shader: " + vertexPath + " o " + fragmentPath;
    }

    public void compile() {
        int vertexID, fragmentID;

        // Primero carga y compila el vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Pasa el codigo del vertex shader a la GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // Verifica errores en compilacion
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + vertexPath + "'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // Carga y compila el fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Pasa el codigo del fragment shader a la GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        // Verifica errores en compilacion
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + fragmentPath + "'\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Vincula los shaders y verifica errores
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // Verifica errores de vinculacion
        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + vertexPath + "' or '" + fragmentPath + "'\n\tLinking of shaders failed.");
            System.out.println(glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }

        // Los shaders ya estan vinculados al programa, podemos eliminarlos
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
    }

    public void use() {
        if (!beingUsed) {
            // Vincula el programa de shader
            glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    public void detach() {
        glUseProgram(0);
        beingUsed = false;
    }

}
