module org.example.demo_fund_dot_software {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.lwjgl.assimp;
    requires org.lwjgl.stb;
    exports com.lapuj.demo to com.almasb.fxgl.core;
    exports com.lapuj.demo.lwjgl to com.almasb.fxgl.core;
    exports com.lapuj.demo.fxgl to com.almasb.fxgl.core;
}
