package com.punkipunk.jade;

import com.punkipunk.renderer.Shader;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    /* Como queremos renderizar un solo cuadrado, especificamos un total de 4 vertices, cada uno con una posicion 3D y color. Los
     * definimos en coordenadas de dispositivo normalizadas (la region visible de OpenGL) en una matriz flotante: */
    private final float[] vertexArray = {
            // Posicion               // Color
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // Esquina inferior derecha (0)
            -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // Esquina superior izquierda (1)
            0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, // Esquina superior derecha (2)
            -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // Esquina inferior izquierda (3)
    };

    /* Dado que OpenGL funciona en 3D, renderizamos un cuadrado 2D con cada vertice en una coordenada z de 0.0. De esta forma, la
     * profundidad del cuadrado se mantiene, dandole la apariencia de un cuadrado 2D. */

    // IMPORTANTE: Los indices de los vertices deben estar en orden antihorario
    /*
                v1 x--------x v2
                   |        |
                   |        |
               v3 x--------x v0
    */
    private final int[] elementArray = {
            2, 1, 0, // Triangulo superior derecho
            0, 1, 3 // Triangulo inferior izquierdo
    };


    private Shader shader;
    private int vaoID, vboID, eboID;

    public LevelEditorScene() {

    }

    @Override
    public void init() {

        // Inicializa y compila los shaders
        shader = new Shader("shaders/vertexShader.glsl", "shaders/fragmentShader.glsl");
        shader.compile();

        // ============================================================
        // Genera VAO, VBO y EBO, y los envia a la GPU
        // ============================================================
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Crea un FloatBuffer de vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Crea un VBO y carga el buffer de vertices
        vboID = glGenBuffers();
        // Vincula el ID del VBO al GL_ARRAY_BUFFER
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        /* Copia los datos de vertice en la memoria del buffer en donde los datos se configuran solo una vez y se utilizan muchas
         * veces especificando GL_STATIC_DRAW para determinar como la GPU va a gestionar los datos. */
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Crea los indices y los carga
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Agregar los punteros de atributos de vertice
        int positionsSize = 3; // El atributo de vertice es un vec3, por lo que esta compuesto por 3 valores
        int colorSize = 4;
        int floatSizeBytes = Float.BYTES;
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;
        /* El primer parametro especifica que atributo de vertice queremos configurar. Recuerde que especificamos la ubicacion del
         * atributo de vertice de posicion en el vertex shader con layout (location = 0). Esto establece la ubicacion del atributo
         * de vertice en 0 y, dado que queremos pasar datos a este atributo de vertice, pasamos 0. El tercer argumento especifica
         * el tipo de datos que es GL_FLOAT (un vec* en GLSL consiste en valores de punto flotante).
         *
         * El quinto argumento se conoce como stride y nos indica el espacio entre atributos de vertice consecutivos. Dado que el
         * siguiente conjunto de datos de posicion se encuentra exactamente a 7 veces el tamaño de un punto flotante,
         * especificamos ese valor como paso. Como en este caso, la matriz no esta compacta, no podemos especificar el stride como
         * 0 para permitir que OpenGL determine el stride (esto solo funciona cuando los valores estan compactados). Siempre que
         * tengamos mas atributos de vertice, debemos definir cuidadosamente el espaciado entre cada atributo de vertice. */
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0); // Establece el puntero del primer layout (aPos)
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(double dt) {

        // System.out.println((int) (1.0f / dt) + " FPS");

        shader.use();

        // Vincula el VAO que estamos usando
        glBindVertexArray(vaoID);

        // Habilita los punteros de atributos de vertice (TODO No se repite?)
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Indica que queremos renderizar los triangulos desde un buffer de indice
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Desvincula todo
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        shader.detach();

    }

}
