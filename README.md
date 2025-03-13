# Motor de Juegos 2D con OpenGL: Fundamentos y Arquitectura del Renderizado Moderno

## Índice
1. [Introducción al Renderizado en GPU](#introducción-al-renderizado-en-gpu)
2. [Pipeline Gráfico Moderno](#pipeline-gráfico-moderno)
3. [Programación de Shaders](#programación-de-shaders)
4. [Gestión de Datos: VBO, VAO y EBO](#gestión-de-datos-vbo-vao-y-ebo)
5. [Optimización de Renderizado con Index Buffers (EBO)](#optimización-de-renderizado-con-index-buffers-ebo)

## Introducción al Renderizado en GPU

### Evolución Histórica
En los años 80/90, los gráficos por computadora utilizaban la **rasterización**, un proceso donde las imágenes se representaban como una matriz de píxeles. Las pantallas tenían resoluciones fijas, y los píxeles se almacenaban en una estructura plana llamada **framebuffer**, conteniendo información RGB para cada píxel.

Los desarrolladores no necesitaban adaptar sus gráficos a diferentes resoluciones, ya que el hardware de destino era conocido. Por ejemplo, al desarrollar para la NES (Nintendo Entertainment System), los creadores sabían exactamente para qué especificaciones estaban diseñando, trabajando con un número fijo de píxeles.

### Transición al Renderizado Moderno
Hoy en día, el proceso de renderizado ha cambiado significativamente debido a:
1. La transición hacia gráficos 3D
2. El desarrollo de GPUs especializadas para procesamiento paralelo

En sistemas modernos como OpenGL, todo se procesa en un espacio 3D, incluso cuando el resultado final es 2D. El trabajo principal consiste en transformar coordenadas 3D en píxeles 2D que se ajusten a la pantalla, proceso gestionado por el pipeline gráfico.

## Pipeline Gráfico Moderno

El pipeline gráfico transforma coordenadas 3D en píxeles coloreados en pantalla, ejecutando múltiples etapas en paralelo, lo que explica por qué las GPUs modernas tienen miles de núcleos de procesamiento.

![Pipeline Gráfico](pipeline%20grafico.PNG)

### Etapas del Pipeline

#### 1. Datos de Entrada (`Vertex Data[]`)
El pipeline recibe una lista de vértices, donde cada vértice contiene datos como posición, color, coordenadas de textura y normales. OpenGL necesita saber qué primitivas gráficas formar con estos datos (puntos, líneas, triángulos) mediante indicadores como `GL_POINTS`, `GL_TRIANGLES` o `GL_LINE_STRIP`. A las GPUs modernas no se envian pixeles, se envian formas y, por lo general, estas formas son triangulos (lista de vertices).

#### 2. Vertex Shader
Primera etapa programable del pipeline. Toma un vértice como entrada y transforma sus coordenadas 3D mediante [matrices de transformación](https://www.scratchapixel.com/lessons/mathematics-physics-for-computer-graphics/geometry/matrices.html), realizando procesamiento básico en sus atributos.

Para empezar a dibujar algo, primero debemos proporcionar a OpenGL datos de vértices de entrada. OpenGL es una biblioteca de gráficos 3D, por lo que todas las coordenadas que especificamos en OpenGL son 3D (coordenadas x, y, z). OpenGL no transforma simplemente todas las coordenadas 3D a píxeles 2D en la pantalla; solo procesa las coordenadas 3D cuando se encuentran en un rango específico entre -1.0 y 1.0 en los tres ejes (x, y, z). Todas las coordenadas dentro de este rango, denominado **coordenadas del dispositivo normalizadas (NDC)**, serán visibles en la pantalla (pero no así las coordenadas fuera de esta región).

#### 3. Geometry Shader (Opcional)
Recibe una colección de vértices que forman una primitiva y puede generar nuevas formas emitiendo nuevos vértices.

#### 4. Shape Assembly
Ensambla todos los vértices procesados en la forma primitiva especificada (puntos, líneas, triángulos).

#### 5. Rasterización
Convierte las primitivas en fragmentos (potenciales píxeles) en la pantalla final. Antes del fragment shader, se realiza el recorte (clipping) que descarta fragmentos fuera del campo de visión.

#### 6. Fragment Shader
Calcula el color final de cada píxel. Aquí ocurren la mayoría de los efectos avanzados, utilizando datos de la escena 3D como luces, sombras y propiedades de materiales.

#### 7. Pruebas y Mezcla
Verifica valores de profundidad (depth) y plantilla (stencil) para determinar la visibilidad de cada fragmento, y aplica mezcla (blending) basada en valores alfa.

Para mas detalle de las estapas del pipeline ver [Hello Triangle](https://learnopengl.com/Getting-started/Hello-Triangle).

## Programación de Shaders

En el antiguo OpenGL con la tubería de función fija ([fixed-function pipeline](https://www.khronos.org/opengl/wiki/Fixed_Function_Pipeline)), existía una lista limitada de funciones predefinidas para controlar cómo la GPU renderizaba los objetos. Este enfoque era más simplificado y fácil de implementar, pero resultaba muy limitado y ofrecía poco margen de maniobra para los desarrolladores.

Como resultado de esta limitacion, surgieron los **shaders**. Los shaders son programas que se ejecutan directamente en la GPU y se escriben en [GLSL](https://en.wikipedia.org/wiki/OpenGL_Shading_Language) (OpenGL Shading Language). Reemplazan la funcionalidad fija de versiones anteriores de OpenGL, dando a los desarrolladores control total sobre el renderizado.

### Tipos Principales de Shaders

#### Vertex Shader
- Se ejecuta **una vez por vértice**
- Utiliza los datos de vértice del VAO
- Debe establecer la posición del vértice mediante `gl_Position`
- Procesa y prepara datos para el Fragment Shader

#### Fragment Shader
- Se ejecuta **una vez por píxel** cubierto por la geometría
- Utiliza datos interpolados del Vertex Shader
- Su salida es siempre un color (valor RGBA)
- Se ejecuta en paralelo para todos los píxeles, siendo la tarea principal de la GPU

### Interpolación entre Shaders

Para píxeles que no están exactamente en un vértice, OpenGL utiliza **interpolación lineal**:
- Un píxel exactamente en un vértice recibe el 100% del valor de ese vértice
- Un píxel en el punto medio entre dos vértices recibe una mezcla 50/50
- Un píxel dentro de un triángulo recibe una mezcla ponderada según su distancia a cada vértice

## Gestión de Datos: VBO, VAO y EBO

En OpenGL moderno, los datos se organizan en objetos especializados que reemplazan los métodos antiguos como las listas de visualización y el modo inmediato.

### Vertex Array Object (VAO)
Es un contenedor que almacena toda la información de estado relacionada con los datos de vértices. Cada VAO tiene un identificador único (ID) y múltiples espacios para almacenar atributos de vértices.

### Vertex Buffer Object (VBO)
Es un buffer que contiene datos específicos como posiciones, colores, normales o coordenadas de textura. Cada VBO se vincula a una lista de atributos específica en el VAO.

### Element Buffer Object (EBO)
También conocido como Index Buffer Object (IBO), almacena índices que indican a OpenGL qué vértices debe dibujar y en qué orden, permitiendo reutilizar vértices y mejorar la eficiencia.

## Optimización de Renderizado con Index Buffers (EBO)

### Sistema de Coordenadas OpenGL

Antes de entrar en detalle de como aplicar la optimizacion de renderizado utlizando indices para representar a los vertices, necesitamos saber que OpenGL utiliza un sistema de coordenadas cartesianas 3D conocido como "sistema de coordenadas diestro" (right-handed coordinate system), en donde el eje **x** positivo apunta hacia la derecha, el eje **y** positivo apunta hacia arriba y el eje **z** positivo apunta hacia fuera de la pantalla (hacia el observador). 

Como las coordenadas del dispositivo normalizadas van de -1.0 a 1.0, el vertice 0 (que se utiliza como ejemplo para especificar la esquina inferior derecha del cuadrado) normalizado se representa como `0.5f, -0.5f, 0.0f`, el vertice 1 como `-0.5f, 0.5f, 0.0f`, el vertice 2 como `0.5f, 0.5f, 0.0f` y el vertice 3 como `-0.5f, -0.5f, 0.0f`. Juntos forman un cuadrado en el centro del sistema de coordenadas de OpenGL:
   ```
    v1 x--------x v2
       |        |
       |        |
    v3 x--------x v0
   ```
Las coordenadas NDC se transformarán en coordenadas de espacio de pantalla (screen-space coordinates) mediante la transformación de la ventana gráfica (viewport transform), utilizando los datos proporcionados `glViewport`. Las coordenadas de espacio de pantalla resultantes se transforman en fragmentos como entrada para el sombreador de fragmentos.

Ahora que tenemos esto claro, veamos el problema de la duplicacion de vertices y como solucionarlo utilizando indices.

### Problema de la Duplicación de Vértices

Cuando renderizamos un cuadrado como dos triángulos sin usar índices, necesitaríamos especificar:
`(V2, V1, V0, V0, V1, V3)`

Esto implica que los vértices V0 y V1 se repiten innecesariamente.

### Solución con Element Buffer Object (EBO)

Usando un EBO, podemos definir los vértices **en orden antihorario** una sola vez y luego especificar cómo conectarlos:

```java
private final int[] elementArray = {
    2, 1, 0, // Triangulo superior derecho
    0, 1, 3  // Triangulo inferior izquierdo
};
```

Esto mantiene el orden antihorario necesario para determinar las caras frontales formadas por dos triángulos.

### Análisis de Eficiencia

Considerando que un vértice típico contiene:
- Posición (3 floats)
- Vector normal (3 floats)
- Coordenadas de textura (2 floats)

**Sin índices (para un cuadrado)**:
- 6 vértices × 8 floats = 48 floats = 192 bytes

**Con índices**:
- 4 vértices × 8 floats = 32 floats = 128 bytes
- 6 índices × 4 bytes = 24 bytes
- Total: 152 bytes (20% menos)

La eficiencia aumenta exponencialmente en modelos más complejos donde los vértices se comparten entre múltiples triángulos, pudiendo reducir el tamaño de datos hasta en un 50%.
