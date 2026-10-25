
package application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    // --------------------------------------------------
    // CONFIGURATION
    // --------------------------------------------------

    private static final String DRIVE_PATH = "D:\\";

    private static final String METADATA_PATH = "movies.txt";

    private static final List<String> CATEGORIES = Arrays.asList(
        "Animated",
        "English",
        "Henti",
        "Malayalam"
    );

    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
        ".mp4", ".mkv", ".avi", ".mov", ".wmv",
        ".m4v", ".webm", ".mpg", ".mpeg"
    );

    private static final String RED = "#e50914";
    private static final String BACKGROUND = "#141414";
    private static final String PANEL = "#202020";
    private static final String MUTED = "#aaaaaa";

    private static final List<String> METADATA_CATEGORIES =
        Arrays.asList("Genre", "Mood", "Pacing", "Theme", "Language");

    // --------------------------------------------------
    // APPLICATION STATE
    // --------------------------------------------------

    private final List<Movie> movies = new ArrayList<>();
    private final Random random = new Random();

    private Stage stage;
    private Scene scene;
    private File libraryDirectory = new File(DRIVE_PATH);
    private MediaPlayer mediaPlayer;
    private Movie featuredMovie;

    private String currentProfile = "Main";
    private String searchQuery = "";
    private String selectedCategory = "All";

    private Label homeStatus;
    private VBox homeContent;
    private HBox categoryFilters;

    private boolean fallbackAttempted = false;

    // --------------------------------------------------
    // START APPLICATION
    // --------------------------------------------------

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;
        stage.setTitle("NetFX | Your Personal Cinema");
        stage.setMinWidth(950);
        stage.setMinHeight(650);

        scene = new Scene(
            new StackPane(),
            1200,
            780,
            Color.web(BACKGROUND)
        );
        scene.getStylesheets().add(
            Main.class.getResource("application.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.show();

        showIntro();
    }

    // --------------------------------------------------
    // INTRO ANIMATION
    // --------------------------------------------------

    private void showIntro() {

        Label logo = new Label("NETFX");
        logo.setStyle(
            "-fx-font-size: 82px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + RED + ";"
        );

        Label subtitle = new Label("YOUR PERSONAL CINEMA");
        subtitle.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #aaaaaa;" +
            "-fx-letter-spacing: 5px;"
        );

        VBox intro = new VBox(15, logo, subtitle);
        intro.setAlignment(Pos.CENTER);
        intro.setStyle("-fx-background-color: black;");

        scene.setRoot(intro);

        logo.setOpacity(0);
        logo.setScaleX(0.65);
        logo.setScaleY(0.65);

        FadeTransition fade =
            new FadeTransition(Duration.seconds(1.4), logo);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale =
            new ScaleTransition(Duration.seconds(1.4), logo);
        scale.setFromX(0.65);
        scale.setFromY(0.65);
        scale.setToX(1);
        scale.setToY(1);

        FadeTransition subtitleFade =
            new FadeTransition(Duration.seconds(1.2), subtitle);
        subtitleFade.setFromValue(0);
        subtitleFade.setToValue(1);
        subtitleFade.setDelay(Duration.seconds(0.6));

        fade.play();
        scale.play();
        subtitleFade.play();

        PauseTransition wait =
            new PauseTransition(Duration.seconds(2.8));

        wait.setOnFinished(event -> showProfiles());
        wait.play();
    }

    // --------------------------------------------------
    // PROFILE SELECTION
    // --------------------------------------------------

    private void showProfiles() {

        Label heading = new Label("Who's watching?");
        heading.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 34px;" +
            "-fx-font-weight: bold;"
        );

        VBox profiles = new VBox(25);
        profiles.setAlignment(Pos.CENTER);

        HBox profileRow = new HBox(30);
        profileRow.setAlignment(Pos.CENTER);

        String[] names = {"Main", "Family", "Kids", "Guest"};
        String[] colors = {
            "#7c3aed", "#2563eb", "#16a34a", "#525252"
        };
        String[] initials = {"M", "F", "K", "G"};

        for (int i = 0; i < names.length; i++) {

            String profileName = names[i];
            String profileColor = colors[i];
            String initial = initials[i];

            Label avatar = new Label(initial);
            avatar.setAlignment(Pos.CENTER);
            avatar.setMinSize(96, 96);
            avatar.setMaxSize(96, 96);

            avatar.setStyle(
                "-fx-background-color: " + profileColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 42px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 24;"
            );

            Label name = new Label(profileName);
            name.setStyle(
                "-fx-text-fill: #bbbbbb;" +
                "-fx-font-size: 16px;"
            );

            VBox card = new VBox(12, avatar, name);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(18));
            card.setMinWidth(140);
            card.setStyle(
                "-fx-cursor: hand;" +
                (profileName.equals(currentProfile)
                    ? "-fx-background-color: #222222;" +
                      "-fx-background-radius: 12;"
                    : "")
            );

            card.setOnMouseEntered(event ->
                avatar.setStyle(
                    "-fx-background-color: " + profileColor + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 42px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 24;" +
                    "-fx-border-color: white;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 24;"
                )
            );

            card.setOnMouseExited(event ->
                avatar.setStyle(
                    "-fx-background-color: " + profileColor + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 42px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 24;"
                )
            );

            card.setOnMouseClicked(event -> {
                currentProfile = profileName;
                scanMovies();
                showHome();
            });

            profileRow.getChildren().add(card);
        }

        Label hint = new Label("SELECT YOUR PROFILE");
        hint.setStyle(
            "-fx-text-fill: " + MUTED + ";" +
            "-fx-font-size: 11px;" +
            "-fx-letter-spacing: 3px;"
        );

        profiles.getChildren().addAll(profileRow, hint);

        Label current = new Label("Current profile: " + currentProfile);
        current.setStyle(
            "-fx-text-fill: " + MUTED + ";" +
            "-fx-font-size: 13px;"
        );

        VBox content = new VBox(35, heading, profiles, current);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: black;");

        scene.setRoot(content);

        FadeTransition fade =
            new FadeTransition(Duration.seconds(0.7), content);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    // --------------------------------------------------
    // SCAN MOVIES AND LOAD METADATA
    // --------------------------------------------------

    private void scanMovies() {

        movies.clear();

        File drive = libraryDirectory;

        if (!drive.exists() || !drive.isDirectory()) {
            featuredMovie = null;
            return;
        }

        for (String category : CATEGORIES) {
            scanFolder(new File(drive, category), category);
        }

        Map<String, MovieMetadata> metadataMap = loadMetadata();

        for (Movie movie : movies) {
            movie.setMetadata(findMetadata(movie, metadataMap));
        }

        movies.sort(
            Comparator.comparing(
                Movie::getTitle,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        if (featuredMovie == null || !movies.contains(featuredMovie)) {
            featuredMovie = movies.isEmpty()
                ? null
                : movies.get(random.nextInt(movies.size()));
        }
    }

    private void scanFolder(File folder, String category) {

        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {
                scanFolder(file, category);
            } else if (isVideo(file)) {
                movies.add(new Movie(file, category));
            }
        }
    }

    private boolean isVideo(File file) {

        String name = file.getName().toLowerCase(Locale.ROOT);

        for (String extension : VIDEO_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }

    // --------------------------------------------------
    // METADATA FILE READER
    // --------------------------------------------------

    private Map<String, MovieMetadata> loadMetadata() {

        Map<String, MovieMetadata> metadataMap =
            new LinkedHashMap<>();

        File metadataFile = new File(METADATA_PATH);

        if (!metadataFile.isFile()) {
            System.err.println(
                "Metadata file not found: "
                + metadataFile.getAbsolutePath()
            );
            return metadataMap;
        }

        try {

            List<String> lines =
                Files.readAllLines(metadataFile.toPath());

            MovieMetadata current = null;

            for (String rawLine : lines) {

                String line = rawLine.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int colon = line.indexOf(':');

                if (colon < 0) {
                    continue;
                }

                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();

                if (key.equalsIgnoreCase("Title")) {

                    current = new MovieMetadata();
                    current.title = value;

                    metadataMap.put(
                        normalizeTitle(value),
                        current
                    );

                    continue;
                }

                if (current == null) {
                    continue;
                }

                switch (key.toLowerCase(Locale.ROOT)) {

                    case "genres":
                        current.genres = parseValues(value);
                        break;

                    case "mood":
                        current.moods = parseValues(value);
                        break;

                    case "pacing":
                        current.pacing = value;
                        break;

                    case "themes":
                        current.themes = parseValues(value);
                        break;

                    case "language":
                        current.language = value;
                        break;

                    default:
                        break;
                }
            }

        } catch (IOException exception) {
            System.err.println(
                "Could not read movie metadata: "
                + exception.getMessage()
            );
        }

        System.out.println(
            "Loaded metadata for " + metadataMap.size() + " movies."
        );

        return metadataMap;
    }

    private List<String> parseValues(String value) {

        List<String> values = new ArrayList<>();

        for (String item : value.split(",")) {

            String cleaned = item.trim();

            if (!cleaned.isEmpty()) {
                values.add(cleaned);
            }
        }

        return values;
    }

    private String normalizeTitle(String title) {

        return title.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]", "");
    }

    private MovieMetadata findMetadata(
        Movie movie,
        Map<String, MovieMetadata> metadataMap
    ) {

        String movieTitle = normalizeTitle(movie.getTitle());

        MovieMetadata metadata = metadataMap.get(movieTitle);

        if (metadata != null) {
            return metadata;
        }

        // Allow minor differences in video filenames.
        for (Map.Entry<String, MovieMetadata> entry
                : metadataMap.entrySet()) {

            String metadataTitle = entry.getKey();

            if (movieTitle.length() >= 5
                && metadataTitle.length() >= 5
                && (movieTitle.contains(metadataTitle)
                    || metadataTitle.contains(movieTitle))) {

                return entry.getValue();
            }
        }

        return null;
    }

    // --------------------------------------------------
    // HOME SCREEN
    // --------------------------------------------------

    private void showHome() {

        Label logo = new Label("NETFX");
        logo.setStyle(
            "-fx-text-fill: " + RED + ";" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label profileLabel =
            new Label("Watching as " + currentProfile);
        profileLabel.setStyle(
            "-fx-text-fill: #bbbbbb;" +
            "-fx-font-size: 12px;"
        );

        Button switchProfile = new Button("Profiles");
        styleButton(switchProfile, false);
        switchProfile.setOnAction(event -> showProfiles());

        Button refreshButton = new Button("Refresh");
        styleButton(refreshButton, false);

        refreshButton.setOnAction(event -> {
            scanMovies();
            showHome();
        });

        Button chooseFolderButton = new Button("Choose Folder");
        styleButton(chooseFolderButton, false);
        chooseFolderButton.setOnAction(event -> chooseLibraryFolder());

        Button addDetailsButton = new Button("Add Details");
        styleButton(addDetailsButton, false);
        addDetailsButton.setOnAction(event -> showMetadataForm());

        TextField search = new TextField();
        search.setPromptText("Search movies...");
        search.setPrefWidth(230);
        search.setStyle(
            "-fx-background-color: #292929;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #888888;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 10;"
        );

        search.setText(searchQuery);

        search.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                searchQuery = newValue == null
                    ? ""
                    : newValue.trim().toLowerCase(Locale.ROOT);

                showHomeContent();
            }
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nav = new HBox(
            20, logo, search, spacer,
            profileLabel, switchProfile, chooseFolderButton,
            addDetailsButton, refreshButton
        );

        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(18, 30, 18, 30));
        nav.setStyle("-fx-background-color: #101010;");

        categoryFilters = createCategoryFilters();

        homeContent = new VBox();
        homeContent.setStyle(
            "-fx-background-color: " + BACKGROUND + ";"
        );

        ScrollPane scroll = new ScrollPane(homeContent);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("netfx-scroll");
        scroll.setStyle(
            "-fx-background: " + BACKGROUND + ";" +
            "-fx-background-color: " + BACKGROUND + ";"
        );
        VBox.setVgrow(scroll, Priority.ALWAYS);

        homeStatus = new Label();
        homeStatus.setPadding(new Insets(12, 30, 15, 30));
        homeStatus.setStyle(
            "-fx-text-fill: " + MUTED + ";" +
            "-fx-font-size: 12px;"
        );

        VBox page = new VBox(nav, categoryFilters, scroll, homeStatus);
        page.setStyle("-fx-background-color: " + BACKGROUND + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        scene.setRoot(page);
        showHomeContent();
    }

    private HBox createCategoryFilters() {

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(14, 30, 14, 30));
        filters.setStyle("-fx-background-color: #101010;");

        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.addAll(CATEGORIES);

        for (String category : categories) {
            Button filter = new Button(
                category.equals("All")
                    ? "All"
                    : category.toUpperCase(Locale.ROOT)
            );
            filter.setUserData(category);
            filter.setOnAction(event -> {
                selectedCategory = category;
                updateCategoryFilters();
                showHomeContent();
            });
            filters.getChildren().add(filter);
        }

        updateCategoryFilters();
        return filters;
    }

    private void updateCategoryFilters() {

        if (categoryFilters == null) {
            return;
        }

        for (javafx.scene.Node node : categoryFilters.getChildren()) {
            Button filter = (Button) node;
            boolean selected = selectedCategory.equals(filter.getUserData());
            filter.getStyleClass().setAll(
                selected ? "category-filter-selected" : "category-filter"
            );
        }
    }

    private void showHomeContent() {

        if (homeContent == null) {
            return;
        }

        homeContent.getChildren().clear();

        List<Movie> matchingMovies = new ArrayList<>();

        for (Movie movie : movies) {
            if (matchesSearch(movie)
                && (selectedCategory.equals("All")
                    || selectedCategory.equals(movie.getCategory()))) {
                matchingMovies.add(movie);
            }
        }

        if (matchingMovies.isEmpty()) {

            Label empty = new Label(
                movies.isEmpty()
                    ? "No movies found. Choose a library folder containing your category folders."
                    : searchQuery.isEmpty()
                        ? "No movies found in " + selectedCategory + "."
                        : selectedCategory.equals("All")
                            ? "No movies match your search."
                            : "No movies match your search and category."
            );

            empty.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 20px;"
            );
            empty.setPadding(new Insets(60, 35, 60, 35));
            homeContent.getChildren().add(empty);

        } else {

            Movie heroMovie = featuredMovie;

            if (heroMovie == null ||
                !matchingMovies.contains(heroMovie)) {
                heroMovie = matchingMovies.get(0);
            }

            homeContent.getChildren().add(createHero(heroMovie));

            if (!searchQuery.isEmpty() || !selectedCategory.equals("All")) {

                homeContent.getChildren().add(
                    createCarousel(
                        !searchQuery.isEmpty()
                            ? "SEARCH RESULTS"
                            : selectedCategory.toUpperCase(Locale.ROOT),
                        matchingMovies
                    )
                );

                if (searchQuery.isEmpty()) {
                    homeContent.getChildren().add(
                        createRandomPicker(matchingMovies)
                    );
                    homeContent.getChildren().add(
                        createMetadataPicker(matchingMovies)
                    );
                }

            } else {

                homeContent.getChildren().add(
                    createCarousel("ALL MOVIES", matchingMovies)
                );

                for (String category : CATEGORIES) {

                    List<Movie> categoryMovies = new ArrayList<>();

                    for (Movie movie : matchingMovies) {
                        if (movie.getCategory().equals(category)) {
                            categoryMovies.add(movie);
                        }
                    }

                    if (!categoryMovies.isEmpty()) {
                        homeContent.getChildren().add(
                            createCarousel(
                                category.toUpperCase(Locale.ROOT),
                                categoryMovies
                            )
                        );
                    }
                }

                homeContent.getChildren().add(
                    createRandomPicker(matchingMovies)
                );

                homeContent.getChildren().add(
                    createMetadataPicker(matchingMovies)
                );
            }
        }

        if (homeStatus != null) {
            homeStatus.setText(
                matchingMovies.size() + " movies available  |  " +
                currentProfile + "  |  " +
                (selectedCategory.equals("All")
                    ? "All categories"
                    : selectedCategory)
            );
        }
    }

    private void chooseLibraryFolder() {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select your movie library folder");

        if (libraryDirectory.isDirectory()) {
            chooser.setInitialDirectory(libraryDirectory);
        }

        File selectedDirectory = chooser.showDialog(stage);

        if (selectedDirectory != null) {
            libraryDirectory = selectedDirectory;
            scanMovies();
            showHome();
        }
    }

    private void showMetadataForm() {

        Label heading = new Label("Add movie details");
        heading.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label description = new Label(
            "Save details for a movie in your library. Use the same title " +
            "as its video filename so recommendations can match it."
        );
        description.setWrapText(true);
        description.setMaxWidth(680);
        description.setStyle("-fx-text-fill: " + MUTED + ";");

        TextField titleField = createMetadataField("Movie title (required)");
        TextField genresField = createMetadataField(
            "Genres, comma-separated"
        );
        TextField moodsField = createMetadataField(
            "Moods, comma-separated"
        );
        TextField pacingField = createMetadataField("Pacing");
        TextField themesField = createMetadataField(
            "Themes, comma-separated"
        );
        TextField languageField = createMetadataField("Language");

        VBox fields = new VBox(
            12,
            titleField,
            genresField,
            moodsField,
            pacingField,
            themesField,
            languageField
        );

        Label status = new Label();
        status.setWrapText(true);

        Button submit = new Button("Save Details");
        styleButton(submit, true);
        submit.setOnAction(event -> {
            String title = titleField.getText().trim();

            if (title.isEmpty()) {
                showFormError(status, "Enter a movie title.");
                titleField.requestFocus();
                return;
            }

            if (genresField.getText().trim().isEmpty()
                && moodsField.getText().trim().isEmpty()
                && pacingField.getText().trim().isEmpty()
                && themesField.getText().trim().isEmpty()
                && languageField.getText().trim().isEmpty()) {
                showFormError(status, "Enter at least one movie detail.");
                return;
            }

            try {
                appendMovieMetadata(
                    title,
                    genresField.getText(),
                    moodsField.getText(),
                    pacingField.getText(),
                    themesField.getText(),
                    languageField.getText()
                );
                scanMovies();

                Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                confirmation.setTitle("Movie details saved");
                confirmation.setHeaderText("Data entry successful!");
                confirmation.setContentText(
                    "Movie details were saved to " +
                    new File(METADATA_PATH).getAbsolutePath()
                );
                confirmation.showAndWait();
                showHome();
            } catch (IOException exception) {
                showFormError(
                    status,
                    "Could not save movie details: "
                    + exception.getMessage()
                );
            }
        });

        Button cancel = new Button("Cancel");
        styleButton(cancel, false);
        cancel.setOnAction(event -> showHome());

        HBox actions = new HBox(12, submit, cancel);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(
            18, heading, description, fields, actions, status
        );
        form.setMaxWidth(680);
        form.setPadding(new Insets(35));
        form.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-background-radius: 8;"
        );

        StackPane page = new StackPane(form);
        page.setPadding(new Insets(30));
        page.setStyle("-fx-background-color: " + BACKGROUND + ";");
        scene.setRoot(page);
    }

    private TextField createMetadataField(String prompt) {

        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-background-color: #292929;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #888888;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 10;"
        );
        return field;
    }

    private void showFormError(Label status, String message) {
        status.setText(message);
        status.setStyle("-fx-text-fill: #ff7777;");
    }

    private void appendMovieMetadata(
        String title,
        String genres,
        String moods,
        String pacing,
        String themes,
        String language
    ) throws IOException {

        List<String> record = new ArrayList<>();
        record.add("");
        record.add("Title: " + title.trim());

        addMetadataLine(record, "Genres", genres);
        addMetadataLine(record, "Mood", moods);
        addMetadataLine(record, "Pacing", pacing);
        addMetadataLine(record, "Themes", themes);
        addMetadataLine(record, "Language", language);

        File metadataFile = new File(METADATA_PATH);
        Files.write(
            metadataFile.toPath(),
            record,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }

    private void addMetadataLine(
        List<String> record,
        String key,
        String value
    ) {
        if (!value.trim().isEmpty()) {
            record.add(key + ": " + value.trim());
        }
    }

    private boolean matchesSearch(Movie movie) {

        if (searchQuery.isEmpty()) {
            return true;
        }

        return movie.getTitle().toLowerCase(Locale.ROOT)
                    .contains(searchQuery)
            || movie.getCategory().toLowerCase(Locale.ROOT)
                    .contains(searchQuery)
            || movie.getFile().getName().toLowerCase(Locale.ROOT)
                    .contains(searchQuery)
            || (movie.getMetadata() != null
                && movie.getMetadata().title
                    .toLowerCase(Locale.ROOT).contains(searchQuery));
    }

    // --------------------------------------------------
    // FEATURED HERO
    // --------------------------------------------------

    private VBox createHero(Movie movie) {

        StackPane background = new StackPane();

        Rectangle backdrop = new Rectangle();
        backdrop.setFill(Color.web("#292020"));
        backdrop.widthProperty().bind(scene.widthProperty());
        backdrop.setHeight(300);

        ImageView backdropImage = loadPoster(movie, 1100, 300);

        if (backdropImage != null) {
            backdropImage.setFitWidth(1100);
            backdropImage.setFitHeight(300);
            backdropImage.setPreserveRatio(false);
            background.getChildren().add(backdropImage);
        } else {
            background.getChildren().add(backdrop);
        }

        VBox details = new VBox(13);

        Label eyebrow = new Label("YOUR PERSONAL COLLECTION");
        eyebrow.setStyle(
            "-fx-text-fill: " + RED + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 2px;"
        );

        Label title = new Label(movie.getTitle());
        title.setWrapText(true);
        title.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 34px;" +
            "-fx-font-weight: bold;"
        );

        Label category = new Label(
            movie.getCategory() + "  •  Local library"
        );
        category.setStyle(
            "-fx-text-fill: #dddddd;" +
            "-fx-font-size: 13px;"
        );

        Button play = new Button("▶  Play");
        styleButton(play, true);
        play.setOnAction(event -> playMovie(movie));

        Button shuffle = new Button("Surprise Me");
        styleButton(shuffle, false);

        shuffle.setOnAction(event -> {
            List<Movie> eligible = new ArrayList<>();

            for (Movie candidate : movies) {
                if (matchesSearch(candidate)) {
                    eligible.add(candidate);
                }
            }

            if (!eligible.isEmpty()) {
                Movie choice = eligible.get(
                    random.nextInt(eligible.size())
                );
                featuredMovie = choice;
                playMovie(choice);
            }
        });

        HBox buttons = new HBox(12, play, shuffle);

        details.getChildren().addAll(
            eyebrow, title, category, buttons
        );
        details.setPadding(new Insets(30));
        details.setAlignment(Pos.CENTER_LEFT);
        details.setMaxWidth(650);

        background.getChildren().add(details);
        StackPane.setAlignment(details, Pos.CENTER_LEFT);

        background.setMinHeight(300);
        background.setMaxHeight(300);

        VBox hero = new VBox(background);
        hero.setPadding(new Insets(0, 0, 20, 0));

        return hero;
    }

    // --------------------------------------------------
    // HORIZONTAL CAROUSELS
    // --------------------------------------------------

    private VBox createCarousel(String heading, List<Movie> list) {

        Label title = new Label(heading);
        title.setPadding(new Insets(0, 30, 0, 30));
        title.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 19px;" +
            "-fx-font-weight: bold;"
        );

        HBox cards = new HBox(15);
        cards.setPadding(new Insets(14, 36, 18, 36));
        cards.setAlignment(Pos.TOP_LEFT);

        for (Movie movie : list) {
            cards.getChildren().add(createMovieCard(movie));
        }

        ScrollPane horizontal = new ScrollPane(cards);
        horizontal.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontal.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontal.setFitToHeight(true);
        horizontal.setPannable(true);
        horizontal.setPrefViewportHeight(350);
        horizontal.setMinHeight(350);
        horizontal.setMaxHeight(350);
        horizontal.getStyleClass().add("netfx-scroll");
        horizontal.setStyle(
            "-fx-background: " + BACKGROUND + ";" +
            "-fx-background-color: " + BACKGROUND + ";"
        );

        Button previous = new Button("‹");
        Button next = new Button("›");
        previous.getStyleClass().add("carousel-arrow");
        next.getStyleClass().add("carousel-arrow");
        StackPane.setAlignment(previous, Pos.CENTER_LEFT);
        StackPane.setAlignment(next, Pos.CENTER_RIGHT);
        previous.setOnAction(event ->
            scrollCarousel(horizontal, cards, -1)
        );
        next.setOnAction(event ->
            scrollCarousel(horizontal, cards, 1)
        );

        StackPane carousel = new StackPane(horizontal, previous, next);
        VBox row = new VBox(3, title, carousel);
        row.setPadding(new Insets(10, 0, 12, 0));

        return row;
    }

    private void scrollCarousel(
        ScrollPane scrollPane,
        HBox cards,
        double direction
    ) {

        double scrollableWidth =
            cards.getWidth() - scrollPane.getViewportBounds().getWidth();

        if (scrollableWidth <= 0) {
            return;
        }

        double step = scrollPane.getViewportBounds().getWidth()
            * 0.8 / scrollableWidth;
        scrollPane.setHvalue(Math.max(
            0,
            Math.min(1, scrollPane.getHvalue() + direction * step)
        ));
    }

    // --------------------------------------------------
    // MOVIE CARDS AND POSTERS
    // --------------------------------------------------

    private VBox createMovieCard(Movie movie) {

        ImageView poster = loadPoster(movie, 160, 240);

        StackPane posterBox = new StackPane();

        if (poster != null) {
            poster.setFitWidth(160);
            poster.setFitHeight(240);
            poster.setPreserveRatio(false);
            posterBox.getChildren().add(poster);
        } else {
            posterBox.getChildren().add(
                createPlaceholder(movie, 160, 240)
            );
        }

        posterBox.setMinSize(160, 240);
        posterBox.setMaxSize(160, 240);

        Label title = new Label(movie.getTitle());
        title.setWrapText(true);
        title.setMaxWidth(160);
        title.setMaxHeight(38);
        title.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        Label category = new Label(movie.getCategory());
        category.setStyle(
            "-fx-text-fill: " + MUTED + ";" +
            "-fx-font-size: 11px;"
        );

        VBox card = new VBox(7, posterBox, title, category);
        card.setPrefWidth(160);
        card.setPadding(new Insets(6));
        card.getStyleClass().add("movie-card");
        card.setStyle("-fx-cursor: hand;");

        card.setOnMouseEntered(event -> {
            card.setStyle(
                "-fx-cursor: hand;" +
                "-fx-background-color: #292929;" +
                "-fx-background-radius: 6;"
            );

            ScaleTransition scale = new ScaleTransition(
                Duration.millis(130), posterBox
            );
            scale.setToX(1.04);
            scale.setToY(1.04);
            scale.play();
        });

        card.setOnMouseExited(event -> {
            card.setStyle("-fx-cursor: hand;");

            ScaleTransition scale = new ScaleTransition(
                Duration.millis(130), posterBox
            );
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
        });

        card.setOnMouseClicked(event -> playMovie(movie));

        return card;
    }

    private StackPane createPlaceholder(
        Movie movie,
        double width,
        double height
    ) {

        String[] colors = {
            "#402020", "#202d43", "#353020",
            "#252525", "#33203b", "#20352c"
        };

        String color = colors[
            Math.floorMod(movie.getTitle().hashCode(), colors.length)
        ];

        Label category = new Label(
            movie.getCategory().toUpperCase(Locale.ROOT)
        );
        category.setWrapText(true);
        category.setAlignment(Pos.CENTER);
        category.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 2px;"
        );

        Label initial = new Label(
            movie.getTitle().isEmpty()
                ? "?"
                : movie.getTitle().substring(0, 1)
                    .toUpperCase(Locale.ROOT)
        );
        initial.setStyle(
            "-fx-text-fill: " + RED + ";" +
            "-fx-font-size: 48px;" +
            "-fx-font-weight: bold;"
        );

        VBox content = new VBox(12, initial, category);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(12));

        StackPane poster = new StackPane(content);
        poster.setMinSize(width, height);
        poster.setMaxSize(width, height);
        poster.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " +
            color + ", #111111);" +
            "-fx-background-radius: 5;"
        );

        return poster;
    }

    private ImageView loadPoster(
        Movie movie,
        double width,
        double height
    ) {

        File video = movie.getFile();
        File parent = video.getParentFile();

        String filename = video.getName();
        int dot = filename.lastIndexOf('.');
        String base = dot > 0
            ? filename.substring(0, dot)
            : filename;

        List<File> candidates = Arrays.asList(
            new File(parent, base + ".jpg"),
            new File(parent, base + ".jpeg"),
            new File(parent, base + ".png"),
            new File(parent, "poster.jpg"),
            new File(parent, "poster.png"),
            new File(parent, "cover.jpg"),
            new File(parent, "cover.png")
        );

        for (File candidate : candidates) {

            if (!candidate.isFile()) {
                continue;
            }

            try {

                Image image = new Image(
                    candidate.toURI().toString(),
                    0,
                    0,
                    false,
                    true
                );

                if (!image.isError()) {
                    ImageView view = new ImageView(image);
                    view.setFitWidth(width);
                    view.setFitHeight(height);
                    view.setPreserveRatio(false);
                    image.widthProperty().addListener(
                        (observable, oldWidth, newWidth) ->
                            updatePosterViewport(view, image, width, height)
                    );
                    image.heightProperty().addListener(
                        (observable, oldHeight, newHeight) ->
                            updatePosterViewport(view, image, width, height)
                    );
                    updatePosterViewport(view, image, width, height);
                    return view;
                }

            } catch (Exception ignored) {
                // Try the next candidate.
            }
        }

        return null;
    }

    private void updatePosterViewport(
        ImageView view,
        Image image,
        double width,
        double height
    ) {

        if (image.getWidth() > 0 && image.getHeight() > 0) {
            view.setViewport(createCenterCrop(
                image.getWidth(),
                image.getHeight(),
                width,
                height
            ));
        }
    }

    private Rectangle2D createCenterCrop(
        double imageWidth,
        double imageHeight,
        double targetWidth,
        double targetHeight
    ) {

        double targetRatio = targetWidth / targetHeight;
        double imageRatio = imageWidth / imageHeight;

        if (imageRatio > targetRatio) {
            double cropWidth = imageHeight * targetRatio;
            return new Rectangle2D(
                (imageWidth - cropWidth) / 2,
                0,
                cropWidth,
                imageHeight
            );
        }

        double cropHeight = imageWidth / targetRatio;
        return new Rectangle2D(
            0,
            (imageHeight - cropHeight) / 2,
            imageWidth,
            cropHeight
        );
    }

    // --------------------------------------------------
    // SURPRISE ME
    // --------------------------------------------------

    private VBox createRandomPicker(List<Movie> list) {

        Label heading = new Label("Can't decide what to watch?");
        heading.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 21px;" +
            "-fx-font-weight: bold;"
        );

        Label description = new Label(
            "Let NetFX choose something from your collection."
        );
        description.setStyle("-fx-text-fill: " + MUTED + ";");

        Button surprise = new Button("Surprise Me");
        styleButton(surprise, true);

        surprise.setOnAction(event -> {
            if (!list.isEmpty()) {
                Movie choice = list.get(random.nextInt(list.size()));
                featuredMovie = choice;
                playMovie(choice);
            }
        });

        VBox box = new VBox(12, heading, description, surprise);
        box.setPadding(new Insets(30));
        box.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-background-radius: 8;"
        );

        VBox wrapper = new VBox(box);
        wrapper.setPadding(new Insets(20, 30, 35, 30));

        return wrapper;
    }

    // --------------------------------------------------
    // METADATA-BASED RECOMMENDATIONS
    // --------------------------------------------------

    private VBox createMetadataPicker(List<Movie> list) {

        Label heading = new Label("DISCOVER YOUR NEXT MOVIE");
        heading.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;"
        );

        Label description = new Label(
            "Choose a category and find something to watch."
        );
        description.setStyle("-fx-text-fill: " + MUTED + ";");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(METADATA_CATEGORIES);
        categoryBox.setValue("Mood");
        categoryBox.setPrefWidth(180);

        ComboBox<String> valueBox = new ComboBox<>();
        valueBox.setPromptText("Choose a preference");
        valueBox.setPrefWidth(220);

        styleComboBox(categoryBox);
        styleComboBox(valueBox);

        Label categoryLabel = new Label("Category");
        Label valueLabel = new Label("Preference");

        categoryLabel.setStyle("-fx-text-fill: white;");
        valueLabel.setStyle("-fx-text-fill: white;");

        populateMetadataValues(categoryBox, valueBox, list);

        categoryBox.setOnAction(event ->
            populateMetadataValues(categoryBox, valueBox, list)
        );

        Button recommendButton = new Button("Find My Movie");
        styleButton(recommendButton, true);

        VBox resultArea = new VBox(12);
        resultArea.setAlignment(Pos.CENTER_LEFT);

        recommendButton.setOnAction(event -> {

            String selectedCategory = categoryBox.getValue();
            String selectedValue = valueBox.getValue();

            if (selectedCategory == null || selectedValue == null) {
                resultArea.getChildren().setAll(
                    createMessageLabel(
                        "Choose a category and preference first."
                    )
                );
                return;
            }

            List<Movie> matches = new ArrayList<>();

            for (Movie movie : list) {

                MovieMetadata metadata = movie.getMetadata();

                if (metadata == null) {
                    continue;
                }

                for (String value :
                        metadata.getValues(selectedCategory)) {

                    if (value.equalsIgnoreCase(selectedValue)) {
                        matches.add(movie);
                        break;
                    }
                }
            }

            if (matches.isEmpty()) {
                resultArea.getChildren().setAll(
                    createMessageLabel(
                        "No matching movies found in your library."
                    )
                );
                return;
            }

            Movie choice = matches.get(
                random.nextInt(matches.size())
            );

            Label resultHeading = new Label("YOUR RECOMMENDATION");
            resultHeading.setStyle(
                "-fx-text-fill: " + RED + ";" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-letter-spacing: 2px;"
            );

            Label matchCount = new Label(
                matches.size() + " matching movie(s) found"
            );
            matchCount.setStyle("-fx-text-fill: " + MUTED + ";");

            Label reason = new Label(
                "Matched by " + selectedCategory + ": " + selectedValue
            );
            reason.setStyle("-fx-text-fill: #dddddd;");

            // The card is clickable and uses the existing player.
            VBox movieCard = createMovieCard(choice);

            resultArea.getChildren().setAll(
                resultHeading,
                matchCount,
                reason,
                movieCard
            );
        });

        VBox categoryField = new VBox(8, categoryLabel, categoryBox);
        VBox valueField = new VBox(8, valueLabel, valueBox);

        HBox selectors = new HBox(
            20, categoryField, valueField, recommendButton
        );
        selectors.setAlignment(Pos.BOTTOM_LEFT);

        VBox panel = new VBox(
            20,
            heading,
            description,
            selectors,
            resultArea
        );

        panel.setPadding(new Insets(25));
        panel.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-background-radius: 8;"
        );

        VBox wrapper = new VBox(panel);
        wrapper.setPadding(new Insets(20, 30, 35, 30));

        return wrapper;
    }

    private void populateMetadataValues(
        ComboBox<String> categoryBox,
        ComboBox<String> valueBox,
        List<Movie> list
    ) {

        valueBox.getItems().clear();
        valueBox.setValue(null);

        String category = categoryBox.getValue();

        if (category == null) {
            return;
        }

        for (Movie movie : list) {

            MovieMetadata metadata = movie.getMetadata();

            if (metadata == null) {
                continue;
            }

            for (String value : metadata.getValues(category)) {

                boolean exists = false;

                for (String existing : valueBox.getItems()) {
                    if (existing.equalsIgnoreCase(value)) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    valueBox.getItems().add(value);
                }
            }
        }

        valueBox.getItems().sort(
            String.CASE_INSENSITIVE_ORDER
        );

        if (!valueBox.getItems().isEmpty()) {
            valueBox.getSelectionModel().selectFirst();
        }
    }

    private Label createMessageLabel(String message) {

        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle(
            "-fx-text-fill: " + MUTED + ";" +
            "-fx-font-size: 13px;"
        );

        return label;
    }

    private void styleComboBox(ComboBox<String> comboBox) {

        comboBox.getStyleClass().add("netfx-combo");
        comboBox.setStyle(
            "-fx-background-color: #303030;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;"
        );
    }

    // --------------------------------------------------
    // VIDEO PLAYBACK: JAVAFX FIRST
    // --------------------------------------------------

    private void playMovie(Movie movie) {

        File file = movie.getFile();

        if (!file.isFile()) {
            showPlayerError(movie, "The video file no longer exists.");
            return;
        }

        stopPlayer();
        fallbackAttempted = false;

        Label title = new Label(movie.getTitle());
        title.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;"
        );

        Button back = new Button("← Back to NetFX");
        styleButton(back, false);

        back.setOnAction(event -> {
            stopPlayer();
            showHome();
        });

        HBox top = new HBox(20, back, title);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(18));
        top.setStyle("-fx-background-color: #101010;");

        StackPane videoArea = new StackPane();
        videoArea.setStyle("-fx-background-color: black;");

        Label loading = new Label("Loading video...");
        loading.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;"
        );
        videoArea.getChildren().add(loading);

        BorderPane playerRoot = new BorderPane();
        playerRoot.setTop(top);
        playerRoot.setCenter(videoArea);
        playerRoot.setStyle("-fx-background-color: black;");

        scene.setRoot(playerRoot);

        try {

            Media media = new Media(file.toURI().toString());
            MediaPlayer newPlayer = new MediaPlayer(media);
            mediaPlayer = newPlayer;

            MediaView mediaView = new MediaView(newPlayer);
            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(videoArea.widthProperty());
            mediaView.fitHeightProperty().bind(videoArea.heightProperty());

            media.setOnError(() -> {
                if (mediaPlayer == newPlayer) {
                    String error = media.getError() == null
                        ? "JavaFX could not read this media file."
                        : media.getError().getMessage();

                    handlePlaybackFailure(movie, error);
                }
            });

            newPlayer.setOnError(() -> {
                if (mediaPlayer == newPlayer) {
                    String error = newPlayer.getError() == null
                        ? "JavaFX could not play this media file."
                        : newPlayer.getError().getMessage();

                    handlePlaybackFailure(movie, error);
                }
            });

            newPlayer.setOnReady(() -> {
                if (mediaPlayer != newPlayer) {
                    return;
                }

                videoArea.getChildren().setAll(mediaView);
                buildPlayerControls(playerRoot, newPlayer, movie);
                newPlayer.play();
            });

            newPlayer.setOnEndOfMedia(() -> {
                if (mediaPlayer == newPlayer) {
                    newPlayer.stop();
                }
            });

        } catch (Exception exception) {
            handlePlaybackFailure(movie, exception.getMessage());
        }
    }

    // --------------------------------------------------
    // VLC FALLBACK
    // --------------------------------------------------

    private void handlePlaybackFailure(Movie movie, String message) {

        if (fallbackAttempted) {
            return;
        }

        fallbackAttempted = true;
        stopPlayer();

        if (launchVLC(movie.getFile())) {
            showExternalPlaybackMessage(movie);
        } else {
            showPlayerError(
                movie,
                "JavaFX could not play this file, and VLC could not " +
                "be launched automatically.\n\n" +
                "Install VLC or update the VLC path.\n\n" +
                "JavaFX error: " +
                (message == null
                    ? "Unknown playback error."
                    : message)
            );
        }
    }

    private boolean launchVLC(File file) {

        String[] vlcPaths = {
            "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
            "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"
        };

        for (String path : vlcPaths) {

            File vlc = new File(path);

            if (vlc.isFile()) {
                try {
                    new ProcessBuilder(
                        vlc.getAbsolutePath(),
                        file.getAbsolutePath()
                    ).start();

                    return true;

                } catch (IOException ignored) {
                    // Try the next installation location.
                }
            }
        }

        try {
            new ProcessBuilder(
                "vlc",
                file.getAbsolutePath()
            ).start();

            return true;

        } catch (IOException ignored) {
            return false;
        }
    }

    private void showExternalPlaybackMessage(Movie movie) {

        Label heading = new Label("Opening in VLC");
        heading.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label details = new Label(
            "\"" + movie.getTitle() + "\" is opening in VLC.\n\n" +
            "This file could not be played by JavaFX."
        );
        details.setWrapText(true);
        details.setStyle(
            "-fx-text-fill: #bbbbbb;" +
            "-fx-font-size: 15px;"
        );

        Button back = new Button("Back to NetFX");
        styleButton(back, true);
        back.setOnAction(event -> showHome());

        VBox screen = new VBox(20, heading, details, back);
        screen.setAlignment(Pos.CENTER);
        screen.setPadding(new Insets(40));
        screen.setStyle("-fx-background-color: black;");

        scene.setRoot(screen);
    }

    // --------------------------------------------------
    // PLAYER CONTROLS
    // --------------------------------------------------

    private void buildPlayerControls(
        BorderPane root,
        MediaPlayer player,
        Movie movie
    ) {

        Button playPause = new Button("❚❚ Pause");
        styleButton(playPause, true);
        player.statusProperty().addListener(
            (observable, oldStatus, newStatus) ->
                playPause.setText(
                    newStatus == MediaPlayer.Status.PLAYING
                        ? "❚❚ Pause"
                        : "▶ Play"
                )
        );

        playPause.setOnAction(event -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                playPause.setText("▶ Play");
            } else {
                player.play();
                playPause.setText("❚❚ Pause");
            }
        });

        Slider seek = new Slider(0, 100, 0);
        seek.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(seek, Priority.ALWAYS);

        Label elapsed = new Label("00:00");
        Label duration = new Label(formatTime(player.getTotalDuration()));

        elapsed.setStyle("-fx-text-fill: white;");
        duration.setStyle("-fx-text-fill: white;");

        Slider volume = new Slider(0, 1, 0.7);
        volume.setPrefWidth(110);
        player.volumeProperty().bind(volume.valueProperty());

        Label volumeLabel = new Label("Volume");
        volumeLabel.setStyle("-fx-text-fill: white;");

        player.currentTimeProperty().addListener(
            (observable, oldTime, newTime) -> {

                Duration total = player.getTotalDuration();

                if (!seek.isValueChanging()
                    && total != null
                    && !total.isUnknown()
                    && total.toMillis() > 0) {

                    seek.setValue(
                        newTime.toMillis() / total.toMillis() * 100
                    );
                }

                elapsed.setText(formatTime(newTime));
            }
        );

        seek.setOnMouseReleased(event -> {
            Duration total = player.getTotalDuration();

            if (total != null
                && !total.isUnknown()
                && total.toMillis() > 0) {

                player.seek(
                    total.multiply(seek.getValue() / 100.0)
                );
            }
        });

        seek.setOnKeyReleased(event -> {
            Duration total = player.getTotalDuration();

            if (total != null
                && !total.isUnknown()
                && total.toMillis() > 0) {

                player.seek(
                    total.multiply(seek.getValue() / 100.0)
                );
            }
        });

        Button stop = new Button("Stop");
        styleButton(stop, false);

        stop.setOnAction(event -> {
            stopPlayer();
            showHome();
        });

        Button fullScreen = new Button("Full screen");
        styleButton(fullScreen, false);
        fullScreen.setOnAction(event -> {
            stage.setFullScreen(!stage.isFullScreen());
            fullScreen.setText(
                stage.isFullScreen() ? "Exit full screen" : "Full screen"
            );
        });

        HBox controls = new HBox(
            10, playPause, elapsed, seek, duration,
            volumeLabel, volume, fullScreen, stop
        );

        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(15));
        controls.setStyle("-fx-background-color: #181818;");

        root.setBottom(controls);
    }

    private String formatTime(Duration time) {

        if (time == null || time.isUnknown()) {
            return "00:00";
        }

        int totalSeconds = Math.max(0, (int) time.toSeconds());

        return String.format(
            Locale.ROOT,
            "%02d:%02d",
            totalSeconds / 60,
            totalSeconds % 60
        );
    }

    // --------------------------------------------------
    // PLAYBACK ERROR SCREEN
    // --------------------------------------------------

    private void showPlayerError(Movie movie, String message) {

        stopPlayer();

        Label heading = new Label("Unable to play this video");
        heading.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 25px;" +
            "-fx-font-weight: bold;"
        );

        Label details = new Label(
            movie.getTitle() + "\n\n" +
            (message == null
                ? "Unknown playback error."
                : message)
        );

        details.setWrapText(true);
        details.setMaxWidth(650);
        details.setStyle(
            "-fx-text-fill: #bbbbbb;" +
            "-fx-font-size: 14px;"
        );

        Button back = new Button("Back to NetFX");
        styleButton(back, true);
        back.setOnAction(event -> showHome());

        VBox errorScreen = new VBox(20, heading, details, back);
        errorScreen.setAlignment(Pos.CENTER);
        errorScreen.setPadding(new Insets(40));
        errorScreen.setStyle("-fx-background-color: black;");

        scene.setRoot(errorScreen);
    }

    private void stopPlayer() {

        MediaPlayer oldPlayer = mediaPlayer;
        mediaPlayer = null;

        if (oldPlayer != null) {
            try {
                oldPlayer.stop();
            } catch (Exception ignored) {
                // Player may already have failed.
            }

            try {
                oldPlayer.dispose();
            } catch (Exception ignored) {
                // Player may already have been disposed.
            }
        }
    }

    // --------------------------------------------------
    // BUTTON STYLING
    // --------------------------------------------------

    private void styleButton(Button button, boolean primary) {

        button.setStyle(
            "-fx-background-color: " +
            (primary ? RED : "#303030") + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 16;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;"
        );
    }

    // --------------------------------------------------
    // MOVIE METADATA MODEL
    // --------------------------------------------------

    private static class MovieMetadata {

        String title = "";
        List<String> genres = new ArrayList<>();
        List<String> moods = new ArrayList<>();
        List<String> themes = new ArrayList<>();
        String pacing = "";
        String language = "";

        List<String> getValues(String category) {

            switch (category) {

                case "Genre":
                    return genres;

                case "Mood":
                    return moods;

                case "Pacing":
                    return pacing.isEmpty()
                        ? new ArrayList<>()
                        : Arrays.asList(pacing);

                case "Theme":
                    return themes;

                case "Language":
                    return language.isEmpty()
                        ? new ArrayList<>()
                        : Arrays.asList(language);

                default:
                    return new ArrayList<>();
            }
        }
    }

    // --------------------------------------------------
    // MOVIE MODEL
    // --------------------------------------------------

    private static class Movie {

        private final File file;
        private final String category;
        private MovieMetadata metadata;

        Movie(File file, String category) {
            this.file = file;
            this.category = category;
        }

        public File getFile() {
            return file;
        }

        public String getCategory() {
            return category;
        }

        public MovieMetadata getMetadata() {
            return metadata;
        }

        public void setMetadata(MovieMetadata metadata) {
            this.metadata = metadata;
        }

        public String getTitle() {

            String name = file.getName();

            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                name = name.substring(0, dot);
            }

            name = name.replaceAll(
                "(?i)\\b(480p|720p|1080p|2160p|4k|8k|" +
                "web-dl|webrip|bluray|brrip|x264|x265|" +
                "h264|h265|hevc|aac|dts)\\b",
                " "
            );

            name = name.replace('.', ' ')
                       .replace('_', ' ')
                       .replace('-', ' ');

            return name.replaceAll("\\s+", " ").trim();
        }
    }

    // --------------------------------------------------
    // SHUTDOWN
    // --------------------------------------------------

    @Override
    public void stop() {
        stopPlayer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
