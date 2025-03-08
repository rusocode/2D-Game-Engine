package com.punkipunk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Cargador de recursos.
 */

public class ResourceLoader {

    /**
     * Carga un recurso como texto.
     *
     * @param resourcePath ruta al recurso relativa a la carpeta resources
     * @return contenido del archivo como string
     */
    public static String loadAsString(String resourcePath) {
        try {

            InputStream inputStream = getInputStream(resourcePath);

            /* Crea un InputStreamReader con codificacion UTF-8 para manejar correctamente caracteres internacionales. Lo envuelve
             * en un BufferedReader para leer lineas de texto de manera eficiente. Usa el metodo lines() para obtener un Stream de
             * todas las lineas del archivo. Usa collect(Collectors.joining("\n")) para unir todas las lineas en un solo String,
             * separandolas con saltos de linea. */
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }

        } catch (IOException e) {
            System.err.println("No se pudo cargar el recurso: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * El {@code ClassLoader} es el componente de la JVM responsable de cargar las clases y recursos.
     * <p>
     * El metodo {@code getResourceAsStream()} busca el recurso especificado en el <i>classpath</i> (que incluye la carpeta
     * resources en un proyecto Maven/Gradle) y devuelve un {@code InputStream} para leer su contenido. Si el recurso no existe,
     * devuelve null.
     *
     * @param resourcePath recurso especificado en el classpath
     * @return el recurso como un InputStream
     */
    private static InputStream getInputStream(String resourcePath) throws IOException {
        // Obtiene el ClassLoader asociado a la clase ResourceLoader
        ClassLoader classLoader = ResourceLoader.class.getClassLoader();
        // Obtiene el recurso como un InputStream
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        if (inputStream == null) throw new IOException("No se pudo encontrar el recurso: " + resourcePath);
        return inputStream;
    }

}
