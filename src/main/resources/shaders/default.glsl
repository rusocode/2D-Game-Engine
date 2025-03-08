#version 330 core

layout (location = 0) in vec3 aPos; // Posicion de vertice
layout (location = 1) in vec4 aColor;

out vec4 fColor;

void main() {
    fColor = aColor;
    // Para configurar la salida del vertex shader, tenemos que asignar los datos de posicipn a la variable predefinida gl_Position
    gl_Position = vec4(aPos, 1.0);
}

#version 330 core

in vec4 fColor;

out vec4 color;

void main() {
    color = fColor;
}