# 2D Game Engine con OpenGL

## Shaders en OpenGL Moderno
Cuando ejecutas un juego, gran parte del procesamiento se realiza en la CPU, como los cálculos lógicos, gameplay, etc.; sin embargo, cuando se trata de renderizar la escena (por ejemplo, al llamar a `glDrawElements()`), la GPU entra en acción. La GPU presenta los objetos en pantalla y determina exactamente cómo se muestran: su color, textura, posición, escala, iluminación, sombras, efectos de niebla, entre otros aspectos.

### Evolución de OpenGL
En el antiguo OpenGL con la tubería de función fija ([fixed-function pipeline](https://www.khronos.org/opengl/wiki/Fixed_Function_Pipeline)), existía una lista limitada de funciones predefinidas para controlar cómo la GPU renderizaba los objetos. Este enfoque era más simplificado y fácil de implementar, pero resultaba muy limitado y ofrecía poco margen de maniobra para los desarrolladores.

En contraste, el OpenGL moderno utiliza una tubería programable donde esas funciones fijas han desaparecido, permitiéndonos programar la GPU directamente. Esto significa que debemos crear nuestras propias funciones de iluminación y otros efectos, lo que nos brinda total flexibilidad. Estos programas que creamos y ejecutamos en la GPU se denominan **shaders** y se escriben en el lenguaje [GLSL](https://en.wikipedia.org/wiki/OpenGL_Shading_Language) (OpenGL Shading Language).

### Tipos principales de Shaders
Hay dos tipos de shaders fundamentales que trabajan juntos para renderizar una escena:

#### 1. Vertex Shader
El Vertex Shader se ejecuta **una vez para cada vértice** en el objeto que se está renderizando. Utiliza como entrada los datos de vértice almacenados en el VAO (como posición, normales, coordenadas de textura, etc.) del vértice que está procesando actualmente.
El Vertex Shader debe realizar dos tareas principales:
- Determinar la posición donde el vértice se renderizará en la pantalla (mediante la variable especial `gl_Position`).
- Procesar cualquier otro dato requerido y preparar las salidas que servirán como entradas al Fragment Shader.

Las salidas del Vertex Shader pueden ser cualquier cosa que programemos: valores flotantes, vectores 2D o 3D, o cualquier combinación de vectores y matrices.

#### 2. Fragment Shader
El Fragment Shader se ejecuta **una vez por cada píxel** que cubre el objeto en la pantalla, lo que significa que se ejecuta muchísimas más veces que el Vertex Shader. Utiliza la salida del Vertex Shader para calcular el color final de cada píxel, y su salida es siempre un color (valor RGBA).

El Fragment Shader define la forma en que se ven los píxeles en la pantalla. Puede pensar en el Fragment Shader como un conjunto de programas procesados en paralelo y, básicamente, un programa que se ejecuta para todos los píxeles al mismo tiempo, siendo esta la tarea fundamental de la GPU. Esta capacidad de procesamiento paralelo es lo que hace que las GPUs sean tan eficientes para el renderizado gráfico.

### Interpolación entre Vertex y Fragment Shader
Cuando el Vertex Shader genera salidas (como colores) para cada vértice, ¿cómo determina el Fragment Shader qué valor usar para los píxeles que no están exactamente en un vértice?

La respuesta es la **interpolación lineal**. Para cada píxel, el Fragment Shader recibe una mezcla de las salidas del Vertex Shader correspondientes a los tres vértices que forman el triángulo donde se encuentra el píxel. Esta mezcla se calcula mediante interpolación lineal basada en la distancia del píxel a cada vértice:
- Un píxel ubicado exactamente en un vértice recibirá el 100% del valor de ese vértice.
- Un píxel ubicado en el punto medio entre dos vértices recibirá una mezcla 50/50 de los valores de ambos vértices.
- Un píxel ubicado dentro de un triángulo recibirá una mezcla ponderada de los valores de los tres vértices según su distancia relativa a cada uno.

### Ejemplo práctico
Consideremos un rectángulo con cuatro vértices almacenados en un VAO:
1. El Vertex Shader se ejecutará cuatro veces (una por cada vértice) y para cada uno:
   - Definirá dónde se renderizará ese vértice en la pantalla mediante `gl_Position`.
   - Calculará un color basado en la posición del vértice (por ejemplo, estableciendo el componente rojo como `position.x + 0.5`, el verde como `1.0` y el azul como `position.y + 0.5`).
2. El Fragment Shader se ejecutará miles de veces (una por cada píxel que cubre el rectángulo) y para cada ejecución:
   - Recibirá un color interpolado linealmente de los colores producidos por el Vertex Shader.
   - Procesará este color de entrada según su programación para determinar el color final del píxel.

Es importante destacar la diferencia en la frecuencia de ejecución: mientras el Vertex Shader se ejecuta solo cuatro veces para nuestro rectángulo, el Fragment Shader podría ejecutarse miles de veces, dependiendo del tamaño del rectángulo en pantalla.

Esta arquitectura programable es lo que hace que el renderizado moderno sea tan flexible y potente, permitiendo a los desarrolladores crear efectos visuales complejos y personalizados.

## VBO, VAO & EBO
Los objetos VAO (Vertex Array Objects), son la forma moderna de almacenar y representar modelos en OpenGL, reemplazando la lista de visualización antigua y el modo inmediato. Un VAO es un objeto para almacenar datos sobre un modelo 3D.

El VAO tiene múltiples espacios para almacenar estos datos, conocidos como listas de atributos. En uno de estos espacios puede almacenar todas las posiciones de los vértices, en otro puede almacenar todos los colores, los vectores normales, o las coordenadas de textura de cada vértice. Estos conjuntos de datos se almacenan en las listas de atributos como VBO (Vertex Buffer Objects).

Un VBO es simplemente un contenedor de datos, puede visualizarlo como un conjunto de números que pueden representar cualquier información: posiciones, colores, normales, etc. Cada VBO se puede vincular a una lista de atributos específica en el VAO.

Otro componente importante en el sistema de renderizado moderno de OpenGL es el EBO (Element Buffer Object), también conocido como IBO (Index Buffer Object). A diferencia de los VBOs que almacenan datos de atributos, un EBO almacena índices que indican a OpenGL qué vértices debe dibujar y en qué orden. Esto permite reutilizar vértices cuando se comparten entre diferentes primitivas (como triángulos), mejorando significativamente la eficiencia de memoria y rendimiento.

**¿Cómo accedemos a estos datos cuando los necesitamos?** Cada VAO tiene un identificador único (ID), por lo que podemos acceder a él en cualquier momento usando esta ID. Cuando vinculamos un VAO, también se vinculan automáticamente todos los VBOs y EBOs asociados, lo que simplifica enormemente el proceso de renderizado.

**¿Pero cómo representamos exactamente un modelo 3D como datos que podríamos almacenar en un VAO?** Cada modelo 3D con el que trabajamos está compuesto por múltiples triángulos, y cada triángulo tiene tres vértices o puntos en el espacio tridimensional. Cada vértice tiene una coordenada 3D (X, Y, Z), y si recopilamos las coordenadas de cada vértice de todos los triángulos del modelo, obtendremos una lista de datos que representa todas las posiciones de los vértices del modelo. Estos datos son precisamente lo que podemos colocar en un VBO y almacenar en una lista de atributos de un VAO.

Un ejemplo sencillo sería renderizar un rectángulo compuesto por dos triángulos. Sin usar EBO, necesitaríamos definir 6 vértices (3 para cada triángulo). El proceso sería:
1. Crear un VBO con las posiciones de los 6 vértices
2. Cargar los datos en el VBO
3. Crear un VAO
4. Vincular el VBO a una de las listas de atributos del VAO
5. Utilizar el ID del VAO para indicarle a OpenGL que renderice el rectángulo en pantalla

Pero con un EBO, podemos optimizar este proceso:
1. Crear un VBO con las posiciones de solo 4 vértices (las esquinas del rectángulo)
2. Crear un EBO con los índices que indican cómo formar los dos triángulos (por ejemplo, [0,1,2, 2,3,0])
3. Crear un VAO y vincular tanto el VBO como el EBO
4. Al momento de renderizar, OpenGL utilizará los índices del EBO para determinar qué vértices dibujar

El VAO es fundamental porque mantiene todo el estado relacionado con los datos de los vértices, incluyendo qué VBOs están asignados a qué atributos y qué EBO está activo, facilitando enormemente el proceso de renderización y mejorando el rendimiento al minimizar la duplicación de datos.
