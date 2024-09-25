package com.optionpricing.MainApp;

import com.optionpricing.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main entry point for the Option Pricing Application.
 * This class initializes the JavaFX application and sets up the primary stage.
 */
public class MainApp extends Application {
    private static final String WINDOW_TITLE = "Option Pricing Application";
    private static final double WINDOW_WIDTH = 1020;
    private static final double WINDOW_HEIGHT = 1800;
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    /**
     * The main entry point for all JavaFX applications.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize the main view of the application
            MainView mainView = new MainView();
            // Create a new scene with the main pane from MainView
            Scene scene = new Scene(mainView.getMainPane(), WINDOW_WIDTH, WINDOW_HEIGHT);

            // Set the title of the window
            primaryStage.setTitle(WINDOW_TITLE);
            // Set the scene to the primary stage
            primaryStage.setScene(scene);
            // Display the primary stage
            primaryStage.show();
        } catch (Exception e) {
            // Log the exception if the application fails to start
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            // Exit the application gracefully
            Platform.exit();
        }
    }

    /**
     * The main method is ignored in JavaFX applications.
     * It serves only as fallback in case the application is launched without JavaFX support.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
