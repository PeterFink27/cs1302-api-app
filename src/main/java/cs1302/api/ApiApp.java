package cs1302.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
/**
 * This application takes a user input of a recipe and prints out 10 ingredients in the recipe and
 * some new recipes that the ingredients could also be cooked in.
 */

public class ApiApp extends Application {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object
/** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    Stage stage;
    Scene scene;
    VBox root;

    Label instructions;

    HBox secondLayer;
    TextField recipe;
    Button search;

    Label display;

    String[] ingredients;
    String[] ingredientRecipes;
    String[] foodDisplay;


    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();

        this.instructions = new Label("Put in a meal and the program will print out up to 10 \n" +
                                      "of that recipe's ingredients and some new recipes that \n" +
                                      "can be made from those ingredients");

        secondLayer = new HBox();
        recipe = new TextField();
        search = new Button("Search");

        display = new Label("ingredients");
        display.setPrefWidth(500);
        display.setPrefHeight(200);

        ingredients = new String[10];
        ingredientRecipes = new String[10];
        foodDisplay = new String[10];

        search.setOnAction(event -> {
            Thread getRecipeThread = new Thread(() -> getRecipe());
            getRecipeThread.start();
        });



    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        // demonstrate how to load local asset using "file:resources/"
        Image bannerImage = new Image("file:resources/readme-banner.png");
        ImageView banner = new ImageView(bannerImage);
        banner.setPreserveRatio(true);
        banner.setFitWidth(640);

        // some labels to display information
        Label notice = new Label("Modify the starter code to suit your needs.");

        // setup scene
        root.getChildren().addAll(instructions, secondLayer, display);
        secondLayer.getChildren().addAll(recipe, search);
        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /**
     * Creates a list of ingredients based on the user input using the given API.
     * Then finds a new recipe that the ingredient can be used for with the second API.
     */
    public void getRecipe() {
        Platform.runLater(() -> display.setText("ingredients:"));
        String recipeSearched = recipe.getText();
        try {
            String term = URLEncoder.encode(recipeSearched, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.themealdb.com/api/json/v1/1/search.php?s=" + term))
                .build();
            HttpResponse<String> response = HTTP_CLIENT.send( request, BodyHandlers.ofString());
            String body = response.body();
            MealDBResponse mealDBResponse = GSON
                .fromJson(body, MealDBResponse.class);

            if (mealDBResponse.meals != null) {
                ingredients[0] = mealDBResponse.meals[0].strIngredient1;
                ingredients[1] = mealDBResponse.meals[0].strIngredient2;
                ingredients[2] = mealDBResponse.meals[0].strIngredient3;
                ingredients[3] = mealDBResponse.meals[0].strIngredient4;
                ingredients[4] = mealDBResponse.meals[0].strIngredient5;
                ingredients[5] = mealDBResponse.meals[0].strIngredient6;
                ingredients[6] = mealDBResponse.meals[0].strIngredient7;
                ingredients[7] = mealDBResponse.meals[0].strIngredient8;
                ingredients[8] = mealDBResponse.meals[0].strIngredient9;
                ingredients[9] = mealDBResponse.meals[0].strIngredient10;
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Search resulted in 0 recipes");
                alert.showAndWait();
            }
            for (int i = 0; i < 10; i++) {
                if (!ingredients[i].equals("")) {
                    try {
                        String ingredient = ingredients[i];
                        String term2 = URLEncoder.encode(ingredient, StandardCharsets.UTF_8);
                        HttpRequest request2 = HttpRequest.newBuilder()
                            .uri(URI.create("https://www.themealdb.com/api/json/v1/1/filter.php?i="
                                            + term2))
                            .build();
                        HttpResponse<String> response2 = HTTP_CLIENT.send(
                            request2, BodyHandlers.ofString());
                        String body2 = response2.body();
                        IngredientResponse ingredientResponse = GSON
                            .fromJson(body2, IngredientResponse.class);
                        foodDisplay[i] = ingredient + ": " + ingredientResponse.meals[0].strMeal;
                    } catch (IOException | InterruptedException e) {
                        System.out.println("IOException caught");
                    }
                } else {
                    foodDisplay[i] = "";
                }
            }
            display();
        } catch (IOException | InterruptedException e) {
            System.out.println("IOException caught");

        }
    } // ApiApp

    /**
     * Updates the display area with non-null entries from the ingredients array.
     */
    public void display() {
        for (int i = 0; i < 10; i++) {
            if (ingredients[i] != null) {
                final int j = i;
                Platform.runLater(() -> display.setText(display.getText() + "\n" + foodDisplay[j]));
            }
        }

    }
}
