package com.punkipunk.jade;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * <a href="https://www.baeldung.com/java-bill-pugh-singleton-implementation">Bill Pugh Singleton Implementation</a>
 */

public class Window {

    private static Scene currentScene;
    private final int width;
    private final int height;
    private final String title;
    public double r, g, b, a;
    private long glfwWindow; // Numero que representa la direccion de memoria en donde esta la ventana

    private Window() {
        this.width = 800;
        this.height = 600;
        this.title = "LWJGL " + Version.getVersion();
        r = 1;
        g = 1;
        b = 1;
        a = 1;
    }

    public static void changeScene(int newScene) {
        switch (newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                currentScene.init();
                break;
            case 1:
                currentScene = new LevelScene();
                currentScene.init();
                break;
            default:
                assert false : "Unknow scene '" + newScene + "'";
        }
    }

    public static Window getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        init();
        loop();

        /* Libera las devoluciones de llamadas de la ventana y destruye la ventana (libera la memoria). Aunque esto no es
         * necesario ya que el OS lo hara por nostros. */
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Termina GLFW y liberar la devolucion de llamada de error
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    private void loop() {

        double beginTime = glfwGetTime();
        double endTime;
        double dt = -1.0f;

        // Ejecuta el bucle de renderizado mientras el usuario no haya intentado cerrar la ventana o presionado la tecla ESCAPE
        while (!glfwWindowShouldClose(glfwWindow)) {

            glfwPollEvents(); // Obtiene los eventos de entrada (mouse, etc.)

            // Establece el color de limpieza
            glClearColor((float) r, (float) g, (float) b, (float) a);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpia el framebuffer

            // No actualiza el delta hasta despues de dos frames
            if (dt >= 0) currentScene.update(dt);

            glfwSwapBuffers(glfwWindow); // Intercambia los buffers de color

            endTime = glfwGetTime();
            // Calcula el tiempo transcurrido que tomo en hacer un ciclo (delta time), ideal hacerlo al final del bucle para detectar picos de lag
            dt = endTime - beginTime;
            beginTime = endTime;


        }
    }

    private void init() {
        /* Configurar una devolucion de llamada de error. La implementacion predeterminada imprimira el mensaje de error en System.err. */
        GLFWErrorCallback.createPrint(System.err).set();
        // Inicializa GLFW
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        // Configura GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // La ventana permanecera oculta despues de la creacion
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Crea la ventana
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);

        // Configura todos los oyenetes (mouse y teclado) para devolucion de llamda y asi recibir notificaciones cuando el cursor se mueva, etc.
        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouesButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        if (glfwWindow == NULL) throw new IllegalStateException("Fail to create the GLFW window");

        // Hace que el contexto OpenGL sea actual
        glfwMakeContextCurrent(glfwWindow);
        // Activa v-sync
        // glfwSwapInterval(1); // Bloquea los FPS a los hz del monitor
        glfwShowWindow(glfwWindow);

        /* Esta linea es fundamental para la interoperacion de LWJGL con el contexto OpenGL de GLFW o cualquier contexto que se
         * administre externamente. LWJGL detecta el contexto actual en el hilo actual, crea la instancia de GLCapabilities y hace
         * que los enlaces OpenGL esten disponibles para su uso. */
        GL.createCapabilities();

        changeScene(0);

    }

    private static class SingletonHolder {
        private static final Window INSTANCE = new Window();
    }

}
