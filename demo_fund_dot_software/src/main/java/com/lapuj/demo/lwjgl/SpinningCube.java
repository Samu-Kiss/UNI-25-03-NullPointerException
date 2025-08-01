package com.lapuj.demo.lwjgl;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SpinningCube {

    // The window handle
    private long window;

    // Cube rendering data
    private int vao, vbo, ebo;
    private float rotation = 0.0f;

    // Cube vertices (position only, we'll usar colores directos en el renderizado)
    private final float[] vertices = {
            // Front face
            -0.5f, -0.5f, 0.5f,  // 0
            0.5f, -0.5f, 0.5f,  // 1
            0.5f, 0.5f, 0.5f,  // 2
            -0.5f, 0.5f, 0.5f,  // 3

            // Back face
            -0.5f, -0.5f, -0.5f,  // 4
            0.5f, -0.5f, -0.5f,  // 5
            0.5f, 0.5f, -0.5f,  // 6
            -0.5f, 0.5f, -0.5f   // 7
    };

    // Cube indices for triangles
    private final int[] indices = {
            // Front face
            0, 1, 2, 2, 3, 0,
            // Back face
            4, 5, 6, 6, 7, 4,
            // Left face
            7, 3, 0, 0, 4, 7,
            // Right face
            1, 5, 6, 6, 2, 1,
            // Top face
            3, 2, 6, 6, 7, 3,
            // Bottom face
            0, 1, 5, 5, 4, 0
    };

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Cleanup
        cleanup();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "Spinning Cube Demo!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void setupCube() {
        // Generate VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Generate VBO
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Upload vertex data
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Generate EBO
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        // Upload index data
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Position attribute (location 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color to black
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Enable depth testing
        glEnable(GL_DEPTH_TEST);

        // Set up 3D perspective
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        // Simple perspective projection
        float aspect = 800.0f / 600.0f;
        float fovy = 45.0f;
        float zNear = 0.1f;
        float zFar = 100.0f;

        // Manual perspective calculation
        float f = (float) (1.0 / Math.tan(Math.toRadians(fovy) / 2.0));
        glFrustum(-aspect * zNear / f, aspect * zNear / f, -zNear / f, zNear / f, zNear, zFar);

        setupCube();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            // Update rotation
            rotation += 1.0f;
            if (rotation >= 360.0f) {
                rotation = 0.0f;
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Set up model-view matrix
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            // Move back and rotate
            glTranslatef(0.0f, 0.0f, -3.0f);
            glRotatef(rotation, 1.0f, 1.0f, 0.0f);

            // Render the cube
            glBindVertexArray(vao);
            glColor3f(1.0f, 0.0f, 0.0f); // Red
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            glColor3f(0.0f, 1.0f, 0.0f); // Green
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 6 * Integer.BYTES);
            glColor3f(0.0f, 0.0f, 1.0f); // Blue
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 12 * Integer.BYTES);
            glColor3f(1.0f, 1.0f, 0.0f); // Yellow
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 18 * Integer.BYTES);
            glColor3f(1.0f, 0.0f, 1.0f); // Magenta
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 24 * Integer.BYTES);
            glColor3f(0.0f, 1.0f, 1.0f); // Cyan
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 30 * Integer.BYTES);
            glBindVertexArray(0);

            // Disable vertex arrays
            glDisableClientState(GL_VERTEX_ARRAY);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }

    public static void main(String[] args) {
        new SpinningCube().run();
    }

}