module org.example.demo_fund_dot_software {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    exports com.lapuj.demo to com.almasb.fxgl.core;
}
