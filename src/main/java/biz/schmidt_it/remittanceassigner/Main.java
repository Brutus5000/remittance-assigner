package biz.schmidt_it.remittanceassigner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Main extends Application {
    private static Properties properties;

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL langFile = getClass().getResource(MessageFormat.format("/i18n/lang_{0}.properties", Locale.getDefault().getLanguage()));

        // Language fallback
        if(langFile == null)
            langFile = getClass().getResource("/i18n/lang_en.properties");

        properties = new Properties();
        properties.load(langFile.openStream());
        ResourceBundle bundle = new PropertyResourceBundle(langFile.openStream());

        Font.loadFont(getClass().getResourceAsStream("/font/FontAwesome.otf"), 0);


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"), bundle);
        Parent root = fxmlLoader.load();
        primaryStage.setTitle(getProperty("application.Title", "Remittance Assigner"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
