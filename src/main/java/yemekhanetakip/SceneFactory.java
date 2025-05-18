package yemekhanetakip;

import javafx.fxml.FXMLLoader;

public class SceneFactory {

    // Static FXML paths
    private static final String FAVORITES = "/yemekhanetakip/Favorites.fxml";
    private static final String FOODS = "/yemekhanetakip/Foods.fxml";
    private static final String PROFILE = "/yemekhanetakip/Profile.fxml";
    private static final String PRO_TEST = "/yemekhanetakip/Main.fxml";
    private static final String SETTINGS = "/yemekhanetakip/Settings.fxml";
    private static final String USER_PROFILE = "/yemekhanetakip/UserProfile.fxml";

    // Static scene loader method
    public static FXMLLoader getScene(String scene) {
        switch (scene) {
            case "FAVORITES":
                return new FXMLLoader(SceneFactory.class.getResource(FAVORITES));
            case "FOODS":
                return new FXMLLoader(SceneFactory.class.getResource(FOODS));
            case "PROFILE":
                return new FXMLLoader(SceneFactory.class.getResource(PROFILE));
            case "MAIN":
                return new FXMLLoader(SceneFactory.class.getResource(PRO_TEST));
            case "SETTINGS":
                return new FXMLLoader(SceneFactory.class.getResource(SETTINGS));
            case "USER_PROFILE":
                return new FXMLLoader(SceneFactory.class.getResource(USER_PROFILE));
            default:
                throw new IllegalArgumentException("Invalid scene: " + scene);
        }
    }
}