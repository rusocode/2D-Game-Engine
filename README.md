# 2D Game Engine con OpenGL

## Renderizado en GPU: De la Rasterización Clásica al Pipeline Gráfico

### Evolución Histórica
En los años 80/90, los gráficos ya utilizaban rasterización, un proceso donde las imágenes se representaban como una matriz de píxeles. Las pantallas tenían un alto y un ancho fijos, donde se almacenaban los píxeles en una estructura plana conocida como "framebuffer", que contenía la información RGB para cada píxel (el RGB para el primer píxel, el RGB para el segundo píxel, etc.). Este buffer se copiaba directamente a la pantalla.

Los desarrolladores no tenían que preocuparse por adaptar los gráficos a diferentes resoluciones, ya que el tamaño de la pantalla era fijo según la plataforma. Por ejemplo, cuando se desarrollaba un juego como Super Mario Bros. para NES (Nintendo Entertainment System), los creadores sabían exactamente para qué hardware estaban diseñando. Lo máximo que tendrían que hacer era estirar los píxeles manteniendo su relación de aspecto, pero siempre trabajaban con un número fijo de píxeles que se mostraban en pantalla.

### Pipeline Gráfico
Hoy en día, el proceso de renderizado es significativamente diferente. Este cambio se debe principalmente a dos factores: la transición hacia los gráficos 3D y el desarrollo de tarjetas gráficas especializadas para procesar estos datos tridimensionales.

En sistemas modernos como OpenGL, todo se trabaja en un espacio 3D, incluso cuando el resultado final es una ventana o pantalla 2D (una matriz de píxeles). Por lo tanto, una gran parte del trabajo consiste en transformar coordenadas 3D en píxeles 2D que se ajusten a la pantalla. Este proceso es gestionado por el pipeline gráfico.

#### Etapas del Pipeline Gráfico
El pipeline gráfico toma como entrada un conjunto de coordenadas 3D y las transforma en píxeles 2D coloreados en la pantalla. Este pipeline se divide en varias etapas especializadas que pueden ejecutarse en paralelo, lo que explica por qué las tarjetas gráficas modernas tienen miles de pequeños núcleos de procesamiento para procesar datos rápidamente.

##### 1. Datos de Entrada (`Vertex Data[]`)
El pipeline recibe como entrada una lista de vértices (Vertex Data). Un vértice es una colección de datos por coordenada 3D, que puede incluir posición, color, coordenadas de textura, normales, etc. OpenGL necesita saber qué tipo de primitivas queremos formar con estos datos (puntos, líneas, triángulos) mediante indicadores como GL_POINTS, GL_TRIANGLES o GL_LINE_STRIP.

##### 2. Vertex Shader
Esta es la primera etapa programable del pipeline. Toma un vértice individual como entrada y su propósito principal es transformar coordenadas 3D en otras coordenadas 3D (mediante matrices de transformación) y realizar un procesamiento básico en los atributos del vértice.

##### 3. Geometry Shader (Opcional)
El geometry shader recibe como entrada una colección de vértices que forman una primitiva y tiene la capacidad de generar nuevas formas emitiendo nuevos vértices para formar nuevas primitivas.

##### 4. Shape Assembly
Esta etapa toma todos los vértices del shader anterior y los ensambla en la forma primitiva especificada (puntos, líneas, triángulos). En un juego típico, esto suele significar la formación de triángulos.

##### 5. Rasterización
Aquí es donde el sistema mapea las primitivas resultantes a los píxeles correspondientes en la pantalla final, generando fragmentos para el fragment shader. Antes de que se ejecute el fragment shader, se realiza el recorte (clipping), que descarta todos los fragmentos fuera del campo de visión para mejorar el rendimiento.

##### 6. Fragment Shader
El propósito principal del fragment shader es calcular el color final de un píxel. Esta es normalmente la etapa donde ocurren todos los efectos avanzados. El fragment shader contiene datos sobre la escena 3D que puede usar para calcular el color final (como luces, sombras, color de la luz, etc.).

##### 7. Pruebas y Mezcla
La etapa final verifica valores de profundidad y plantilla (depth and stencil) del fragmento para determinar si el fragmento está delante o detrás de otros objetos. También verifica los valores alfa (que definen la opacidad) y mezcla los objetos en consecuencia.

### Aplicaciones 2D en un Pipeline 3D
Incluso para aplicaciones 2D, como videojuegos con sprites, se sigue utilizando este pipeline. En estos casos, lo que hacemos es enviar cuadrados (formados por dos triángulos) con texturas aplicadas. Por lo tanto, estamos enviando vértices y coordenadas de textura, no píxeles directamente. Técnicas como el "sprite batching" permiten agrupar múltiples sprites en un solo envío a la GPU para mejorar el rendimiento.

### Conclusión
La evolución desde la rasterización simple de los años 80/90 hasta el complejo pipeline gráfico moderno representa un cambio fundamental en cómo se procesan y renderizar los gráficos. Esta arquitectura moderna permite mayor flexibilidad, mejores efectos visuales y un aprovechamiento más eficiente del hardware especializado, independientemente de si estamos creando gráficos 2D o 3D.

El pipeline gráfico es un todo bastante complejo y contiene muchas partes configurables. Sin embargo, para la mayoría de los casos, solo necesitamos trabajar con el vertex shader y el fragment shader. El aprendizaje inicial de sistemas como OpenGL puede ser difícil, ya que se requiere un gran conocimiento antes de poder renderizar incluso el triángulo más simple, pero este conocimiento proporciona una base sólida para la programación gráfica avanzada.

## Shaders
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
El VAO (Vertex Array Objects) es la forma moderna de almacenar y representar modelos en OpenGL, reemplazando la lista de visualización antigua y el modo inmediato. Un VAO es un objeto para almacenar datos sobre un modelo 3D.

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
2. Crear un EBO con los índices que indican cómo formar los dos triángulos, y es importante que **esten en orden antihorario**, por ejemplo, [2,1,0,0,1,3]
3. Crear un VAO y vincular tanto el VBO como el EBO
4. Al momento de renderizar, OpenGL utilizará los índices del EBO para determinar qué vértices dibujar

El VAO es fundamental porque mantiene todo el estado relacionado con los datos de los vértices, incluyendo qué VBOs están asignados a qué atributos y qué EBO está activo, facilitando enormemente el proceso de renderización y mejorando el rendimiento al minimizar la duplicación de datos.

## Index Buffers

### Problema de la representación directa de vértices
Supongamos que tenemos 4 vértices para representar un rectángulo, donde:
- V0 está ubicado en (0.5, -0.5, 0) - Esquina inferior derecha
- V1 en (-0.5, 0.5, 0) - Esquina superior izquierda
- V2 en (0.5, 0.5, 0) - Esquina superior derecha
- V3 en (-0.5, -0.5, 0) - Esquina inferior izquierda

Para dibujar este rectángulo, necesitamos dividirlo en dos triángulos:
1. El triángulo superior derecho, formado por los vértices V2, V1 y V0 (en ese orden)
2. El triángulo inferior izquierdo, formado por los vértices V0, V1 y V3 (en ese orden)

OpenGL requiere que especifiquemos los vértices en **sentido contrario a las agujas del reloj** (antihorario) para determinar correctamente qué cara del triángulo es la frontal.

### La ineficiencia de la duplicación de vértices
Si representáramos estos triángulos directamente con vértices, necesitaríamos especificar:

`(V2, V1, V0, V0, V1, V3)`

Como podemos observar, los vértices V0 y V1 se repiten dos veces, lo que resulta en una duplicación innecesaria de datos.

### Solución: Index Buffer (EBO)
Para evitar esta duplicación, utilizamos un Index Buffer (EBO), que especifica el orden en que deben conectarse los vértices:
```java
private final int[] elementArray = {
    2, 1, 0, // Triangulo superior derecho
    0, 1, 3  // Triangulo inferior izquierdo
};
```
Este array de índices nos indica:
1. Para el triángulo superior derecho: conectar los vértices en posiciones 2, 1 y 0 (V2, V1, V0)
2. Para el triángulo inferior izquierdo: conectar los vértices en posiciones 0, 1 y 3 (V0, V1, V3)

Ambos triángulos se especifican en orden antihorario, lo que determina correctamente la cara frontal según las convenciones de OpenGL.

### Análisis de eficiencia
La diferencia entre los dos métodos es significativa:

#### Método 1 (sin índices):
- 6 vértices × 3 floats (x,y,z) = 18 floats

#### Método 2 (con índices):
- 4 vértices × 3 floats = 12 floats
- 6 índices (enteros) = 6 ints

En escenarios reales, cada vértice contiene mucha más información. Considerando que un vértice típico puede tener:
- Posición (3 floats)
- Vector normal para iluminación (3 floats)
- Coordenadas de textura (2 floats)

Los cálculos serían:

#### Método 1 (sin índices):

- 6 vértices × 8 floats = 48 floats = 192 bytes (asumiendo 4 bytes por float)

#### Método 2 (con índices):

- 4 vértices × 8 floats = 32 floats = 128 bytes
- 6 índices × 4 bytes = 24 bytes
- Total: 152 bytes (un 20% menos)

### Ventajas en modelos complejos
La eficiencia de los Index Buffers aumenta exponencialmente en modelos más complejos, donde los vértices pueden ser compartidos entre múltiples triángulos. En estos casos, el uso de índices puede reducir el tamaño de los datos en un 30%, 40% o incluso 50%.

El Element Buffer Object (EBO) permite representar eficientemente la topología de un modelo utilizando simples enteros para indicar cómo se conectan los vértices, evitando la duplicación de datos y optimizando tanto el uso de memoria como el rendimiento de renderizado.
