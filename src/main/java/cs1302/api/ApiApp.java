package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.Thread;
import java.lang.Runnable;
import java.io.IOException;
import java.lang.InterruptedException;
import javafx.geometry.Insets;
import java.lang.ArrayIndexOutOfBoundsException;
import com.google.gson.annotations.SerializedName;

/**
 * This app connects the SeatGeek and WeatherStacks apis. Search for an event
 * a performer, or a team, and get the venue of the event and the current weather.
 * The intent of this app is to help prepare for events like outdoor matches, but it
 * can be used to help for other events too!. When a user searches, a request is sent
 * to the SeatGeek api, and the response is used to determine the venue and city.
 * This information is then used to request weather from the WeatherStacks api.
 *
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
    Text intro;
    Text search;
    Text weather;
    private final String seatKey = "MzM0NDY0MTZ8MTY4MzE4MjI5Ny40OTIwNTU3";
    private final String weatherKey = "89ac7396c7a218e96385a7d538991741";
    TextField searchBar;
    Button results;
    HBox searchRow;
    String searchTerms;
    Image weatherImage;
    ImageView display;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {

        root = new VBox(5);
        intro = new Text("Search an event/performer/competition and get the weather at the venue!"
                         + " \n(try: Boston Celtics or Taylor Swift)");
        search = new Text("Search: ");
        weather = new Text("Please keep your searches in English and located in America.");
        searchBar = new TextField();
        results = new Button("Get Weather");
        searchRow = new HBox(5);
        searchTerms = "";
        display = new ImageView();
        display.setPreserveRatio(true);

    } // ApiApp

    /**
     * Sets up the search bar, instructions, and results for the users future search.
     */
    public void init() {

        /*
        // demonstrate how to load local asset using "file:resources/"
        Image bannerImage = new Image("file:resources/readme-banner.png");
        ImageView banner = new ImageView(bannerImage);
        banner.setPreserveRatio(true);
        banner.setFitWidth(640);

        // some labels to display information
        Label notice = new Label("Modify the starter code to suit your needs.");
        */

        results.setOnAction((e) -> search());
        searchRow.getChildren().addAll(search, searchBar, results);
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        VBox.setVgrow(display, Priority.ALWAYS);
        root.getChildren().addAll(intro, searchRow, weather, display);
        root.setPadding(new Insets (5, 5, 5, 5));
        scene = new Scene(root);

    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /**
     * Uses the text in the search bar to request event info from SeatGeek.
     * This info is then used in a request to WeatherStacks and the weather
     * is then displayed to the user.
     */
    public void search() {

        String temp = searchBar.getText();

        if (temp.equals(searchTerms)) {
            System.out.println("?");
            return;
        } // if

        try {
            String seatGeekURL = "https://api.seatgeek.com/2/events?client_id=" + seatKey +
                "&q=" + URLEncoder.encode(searchBar.getText(), StandardCharsets.UTF_8) + "'";
            HttpRequest seatRequest = HttpRequest.newBuilder().uri(URI.create(seatGeekURL)).build();
            HttpResponse<String> seatResponse = HTTP_CLIENT.send(seatRequest,
                                                                BodyHandlers.ofString());
            String seatBody = seatResponse.body();
            SeatGeekResponse sResponse = GSON.fromJson(seatBody, cs1302.api.SeatGeekResponse.class);
            String location = sResponse.events[0].venue.city;

            String weatherURL = "http://api.weatherstack.com/current?access_key=" + weatherKey +
                                "&units=f" + "&query=" + URLEncoder.encode
                                (location, StandardCharsets.UTF_8);
            HttpRequest weatherRequest = HttpRequest.newBuilder().
                                         uri(URI.create(weatherURL)).build();
            HttpResponse<String> weatherResponse = HTTP_CLIENT.send
                                                   (weatherRequest, BodyHandlers.ofString());
            String weatherBody = weatherResponse.body();
            WeatherStackResponse wResponse = GSON.fromJson
                                        (weatherBody, cs1302.api.WeatherStackResponse.class);
            String description = wResponse.current.weatherDescriptions[0];
            String tempWeather = "event: " + sResponse.events[0].title +
                                 "\nvenue: " + sResponse.events[0].venue.name +
                                 "\nlocation: " + location +
                                 "\nweather: " + description +
                                 "\ntemperature: " + wResponse.current.temperature +
                                 " F\nfeels like: " + wResponse.current.feelslike +
                                 " F\nhumidity: " + wResponse.current.humidity + "%";
            weather.setText(tempWeather);
            weatherImage = new Image (wResponse.current.weatherIcons[0]);
            display.setImage(weatherImage);
            stage.sizeToScene();
            searchTerms = searchBar.getText();

        } catch (IOException | InterruptedException | ArrayIndexOutOfBoundsException e) {

            String errorMsg = "Error, SeatGeek couldn't find your event!\n\n" +
                              " - try checking your spelling" +
                              "\n- make sure your perfomer/event actually exsists" +
                              "\n- remember, nfl games haven't started yet!" +
                              "\n- make sure you are typing in english" +
                              "\n- please keep you serches based in the USA" +
                              "\n- lastly, SeatGeek may not have access to your event, sorry!";
            weather.setText(errorMsg);
            weatherImage = new Image("file:resources/default.png");
            display.setImage(weatherImage);
            stage.sizeToScene();

        } // try

    } // search

} // ApiApp
