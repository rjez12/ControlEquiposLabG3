package com.desarrollo.controlequiposlabg3;

import com.desarrollo.controlequiposlabg3.database.JPAUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LaboratorioApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                LaboratorioApplication.class.getResource("equipos-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 1080, 680);

        stage.setTitle("Control de equipos de laboratorio");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        JPAUtil.cerrar();
    }
}