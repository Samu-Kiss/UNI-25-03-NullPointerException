package com.lapuj.demo.lwjgl;

import org.lwjgl.*;
import org.lwjgl.assimp.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.assimp.Assimp.*;

public class OBJ {

    private long window;
    private int shaderProgram;
    private int vao, vbo, ebo;
    private int vertexCount;
    private float rotation = 0.0f;

    // Camera/view matrix uniforms
    private int modelLoc, viewLoc, projLoc;

    // Vertex shader source
    private final String vertexShaderSource =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "layout (location = 1) in vec3 aNormal;\n" +
                    "uniform mat4 model;\n" +
                    "uniform mat4 view;\n" +
                    "uniform mat4 projection;\n" +
                    "out vec3 Normal;\n" +
                    "out vec3 FragPos;\n" +
                    "void main() {\n" +
                    "    FragPos = vec3(model * vec4(aPos, 1.0));\n" +
                    "    Normal = mat3(transpose(inverse(model))) * aNormal;\n" +
                    "    gl_Position = projection * view * vec4(FragPos, 1.0);\n" +
                    "}\0";

    // Fragment shader source
    private final String fragmentShaderSource =
            "#version 330 core\n" +
                    "out vec4 FragColor;\n" +
                    "in vec3 Normal;\n" +
                    "in vec3 FragPos;\n" +
                    "uniform vec3 lightPos;\n" +
                    "uniform vec3 lightColor;\n" +
                    "uniform vec3 objectColor;\n" +
                    "void main() {\n" +
                    "    // Ambient\n" +
                    "    float ambientStrength = 0.1;\n" +
                    "    vec3 ambient = ambientStrength * lightColor;\n" +
                    "    \n" +
                    "    // Diffuse\n" +
                    "    vec3 norm = normalize(Normal);\n" +
                    "    vec3 lightDir = normalize(lightPos - FragPos);\n" +
                    "    float diff = max(dot(norm, lightDir), 0.0);\n" +
                    "    vec3 diffuse = diff * lightColor;\n" +
                    "    \n" +
                    "    vec3 result = (ambient + diffuse) * objectColor;\n" +
                    "    FragColor = vec4(result, 1.0);\n" +
                    "}\0";

    public static void main(String[] args) {
        new OBJ().run();
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        System.out.println("Loading OBJ file: assets/BasketBall/BasketBall.obj");

        init();
        setupShaders();
        loadOBJ("assets/BasketBall/BasketBall.obj");
        loop();
        cleanup();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(1024, 768, "OBJ Loader Demo", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
    }

    private void setupShaders() {
        // Vertex shader
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        try (MemoryStack stack = stackPush()) {
            IntBuffer success = stack.mallocInt(1);
            glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
            if (success.get(0) == 0) {
                String infoLog = glGetShaderInfoLog(vertexShader);
                throw new RuntimeException("Vertex shader compilation failed: " + infoLog);
            }
        }

        // Fragment shader
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        try (MemoryStack stack = stackPush()) {
            IntBuffer success = stack.mallocInt(1);
            glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, success);
            if (success.get(0) == 0) {
                String infoLog = glGetShaderInfoLog(fragmentShader);
                throw new RuntimeException("Fragment shader compilation failed: " + infoLog);
            }
        }

        // Shader program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        try (MemoryStack stack = stackPush()) {
            IntBuffer success = stack.mallocInt(1);
            glGetProgramiv(shaderProgram, GL_LINK_STATUS, success);
            if (success.get(0) == 0) {
                String infoLog = glGetProgramInfoLog(shaderProgram);
                throw new RuntimeException("Shader program linking failed: " + infoLog);
            }
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        // Get uniform locations
        modelLoc = glGetUniformLocation(shaderProgram, "model");
        viewLoc = glGetUniformLocation(shaderProgram, "view");
        projLoc = glGetUniformLocation(shaderProgram, "projection");
    }

    private void loadOBJ(String filePath) {
        AIScene scene = aiImportFile(filePath,
                aiProcess_Triangulate |
                        aiProcess_FlipUVs |
                        aiProcess_CalcTangentSpace |
                        aiProcess_GenNormals);

        if (scene == null) {
            throw new RuntimeException("Failed to load OBJ file: " + aiGetErrorString());
        }

        if (scene.mNumMeshes() == 0) {
            throw new RuntimeException("No meshes found in OBJ file");
        }

        // Get the first mesh
        AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));

        // Extract vertices and normals
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        AIVector3D.Buffer vertexBuffer = mesh.mVertices();
        AIVector3D.Buffer normalBuffer = mesh.mNormals();

        for (int i = 0; i < mesh.mNumVertices(); i++) {
            AIVector3D vertex = vertexBuffer.get(i);
            vertices.add(vertex.x());
            vertices.add(vertex.y());
            vertices.add(vertex.z());

            if (normalBuffer != null) {
                AIVector3D normal = normalBuffer.get(i);
                vertices.add(normal.x());
                vertices.add(normal.y());
                vertices.add(normal.z());
            } else {
                vertices.add(0.0f);
                vertices.add(1.0f);
                vertices.add(0.0f);
            }
        }

        // Extract indices
        AIFace.Buffer faceBuffer = mesh.mFaces();
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace face = faceBuffer.get(i);
            IntBuffer faceIndices = face.mIndices();
            for (int j = 0; j < face.mNumIndices(); j++) {
                indices.add(faceIndices.get(j));
            }
        }

        vertexCount = indices.size();

        // Convert to arrays
        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }

        int[] indexArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }

        // Setup OpenGL buffers
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        System.out.println("Successfully loaded OBJ with " + (vertices.size() / 6) + " vertices and " + (indices.size() / 3) + " triangles");
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram);

            // Update rotation
            rotation += 0.01f;

            // Create matrices
            float[] model = createModelMatrix(rotation);
            float[] view = createViewMatrix();
            float[] projection = createProjectionMatrix();

            // Set uniforms
            glUniformMatrix4fv(modelLoc, false, model);
            glUniformMatrix4fv(viewLoc, false, view);
            glUniformMatrix4fv(projLoc, false, projection);

            // Set lighting uniforms
            int lightPosLoc = glGetUniformLocation(shaderProgram, "lightPos");
            int lightColorLoc = glGetUniformLocation(shaderProgram, "lightColor");
            int objectColorLoc = glGetUniformLocation(shaderProgram, "objectColor");

            glUniform3f(lightPosLoc, 2.0f, 2.0f, 2.0f);
            glUniform3f(lightColorLoc, 1.0f, 1.0f, 1.0f);
            glUniform3f(objectColorLoc, 0.5f, 0.7f, 1.0f);

            // Render
            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private float[] createModelMatrix(float rotation) {
        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);

        return new float[] {
                cos, 0, sin, 0,
                0, 1, 0, 0,
                -sin, 0, cos, 0,
                0, 0, 0, 1
        };
    }

    private float[] createViewMatrix() {
        return new float[] {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, -3, 1
        };
    }

    private float[] createProjectionMatrix() {
        float fov = (float) Math.toRadians(45.0f);
        float aspect = 1024.0f / 768.0f;
        float near = 0.1f;
        float far = 100.0f;

        float f = (float) (1.0 / Math.tan(fov / 2.0));

        return new float[] {
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (far + near) / (near - far), -1,
                0, 0, (2 * far * near) / (near - far), 0
        };
    }

    private void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteProgram(shaderProgram);
    }
}