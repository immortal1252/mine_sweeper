package com.spg;

import com.spg.bean.Result;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindow extends Application {
    private Configure cfg;
    private Game game;
    private Auto auto;
    private Text timerText = new Text();
    private Text safeLeftText = new Text();
    private ImageView[][] imageViews;
    private final Map<Integer, Image> imageMap = new HashMap<>();
    private boolean firstClick = true;
    // 毫秒
    private int elapsed;
    private Timeline timeline;
    private boolean fail = false;
    private List<Pos> lastPressed;
    private boolean leftAvailable = false;
    // 当且仅当左键按下后,再抬起才触发 单点
    // 按下双键,之后抬起右键,再抬起左键这一次是什么都不触发的

    @Override
    public void init() throws Exception {
        super.init();
        // load config
        Yaml yaml = new Yaml();
        this.cfg = yaml.loadAs(getClass().getClassLoader().getResourceAsStream("config.yaml"), Configure.class);
        // init images
        for (Map.Entry<Integer, String> entry : cfg.getId2name().entrySet()) {
            URL resource = getClass().getClassLoader().getResource(entry.getValue());
            if (resource == null) {
                System.out.println("load error");
                return;
            }
            imageMap.put(entry.getKey(), new Image(resource.toURI().toString()));
        }
        // init imageViews
        imageViews = new ImageView[cfg.getNumHeight()][cfg.getNumWidth()];
        for (int i = 0; i < cfg.getNumHeight(); i++) {
            for (int j = 0; j < cfg.getNumWidth(); j++) {
                imageViews[i][j] = new ImageView();
                imageViews[i][j].setFitWidth(cfg.getCellSize());
                imageViews[i][j].setPreserveRatio(true);
                imageViews[i][j].setOnMouseDragged(this::mouseDragged);
                imageViews[i][j].setOnMouseReleased(this::mouseReleased);
                imageViews[i][j].setOnMousePressed(this::mousePressed);
            }
        }
        game = new Game(cfg.getNumWidth(), cfg.getNumHeight(), cfg.getNumMine());
        auto = new Auto(game);
    }

    private void setImage(Pos pos, int id) {
        Image image = imageMap.get(id);
        if (image == null) {
            System.out.println(id + "not eixst");
            return;
        }
        imageViews[pos.r][pos.c].setImage(image);
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = createGridPane();
        VBox body = new VBox(20);
        body.setPadding(new Insets(20, 20, 20, 20));
        HBox header = new HBox(50);
        // timer
        timerText = new Text();
        timerText.setFill(Color.RED);
        timerText.setFont(new Font(20));
        safeLeftText = new Text();
        timerText.setFill(Color.BLACK);
        timerText.setFont(new Font(20));
        // reset button
        Button resetButton = new Button();
        resetButton.setPrefWidth(40);
        resetButton.setOnAction(actionEvent -> {
            initial();
        });
        Button promptButton = new Button();
        promptButton.setPrefWidth(40);
        promptButton.setOnAction(actionEvent -> {
            prompt();
        });
        Button autoButton = new Button();
        autoButton.setPrefWidth(40);
        autoButton.setOnAction(actionEvent -> {
            initial();
            autoPlay();
        });

        header.getChildren().add(timerText);
        header.getChildren().add(safeLeftText);
        header.getChildren().add(resetButton);
        header.getChildren().add(promptButton);
        header.getChildren().add(autoButton);
        body.getChildren().add(header);
        body.getChildren().add(gridPane);
        timeline = new Timeline(new KeyFrame(Duration.millis(100), actionEvent -> {
            elapsed += 100;
            timerText.setText("" + elapsed / 1000);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        initial();
        Scene scene = new Scene(body, cfg.getNumWidth() * cfg.getCellSize() + 40, cfg.getNumHeight() * cfg.getCellSize() + 80);
        scene.setFill(Color.GRAY);
        primaryStage.setTitle("Scalable GridPane spg");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Pos getPos(MouseEvent event) {
        ImageView node = (ImageView) event.getTarget();
        Point2D point2D = node.localToParent(event.getX(), event.getY());
        double globalX = point2D.getX();
        double globalY = point2D.getY();
        int row = (int) globalY / cfg.getCellSize();
        int col = (int) globalX / cfg.getCellSize();
        return new Pos(row, col);
    }

    private void update(ClickStatus clickStatus) {
        if (clickStatus == null)
            return;
        Map<Pos, Integer> cell2Update = clickStatus.getCell2update();
        for (Map.Entry<Pos, Integer> entry : cell2Update.entrySet()) {
            setImage(entry.getKey(), entry.getValue());
        }
    }

    private void pressed2Up() {
        // 按下之后,松开还原的特效
        if (lastPressed != null) {
            for (Pos pos : lastPressed) {
                setImage(pos, 10);
            }
        }
        lastPressed = null;
    }

    private void judge() {
        safeLeftText.setText(String.valueOf(game.getSafeGridLeft()));
        if (!game.success())
            return;
        Result result = new Result(1, elapsed / 1000., LocalDate.now());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("统计信息");
        alert.setHeaderText("header");
        alert.setContentText(result.toString());
        alert.show();
    }

    private void mousePressed(MouseEvent event) {
        if (game.success())
            return;
        Pos pos = getPos(event);
        ClickStatus clickStatus;
        if (event.isSecondaryButtonDown() && !event.isPrimaryButtonDown()) {
            // right press
            clickStatus = game.flag(pos);
            update(clickStatus);
            return;
        }

        // press
        if (event.isPrimaryButtonDown() && !event.isSecondaryButtonDown()) {
            // left press
            leftAvailable = true;
            clickStatus = game.pressOne(pos);
        } else {
            // double press
            clickStatus = game.pressNine(pos);
        }
        if (!clickStatus.getCell2update().isEmpty()) {
            lastPressed = new ArrayList<>(clickStatus.getCell2update().keySet());
        }
        update(clickStatus);
    }

    private void mouseDragged(MouseEvent event) {
        if (game.success())
            return;
        pressed2Up();
        Pos pos = getPos(event);
        ClickStatus clickStatus = null;
        if (event.isPrimaryButtonDown() && event.isSecondaryButtonDown()) {
            // double dragged
            clickStatus = game.pressNine(pos);
        } else if (event.isPrimaryButtonDown()) {
            // left dragged
            clickStatus = game.pressOne(pos);
        }

        if (clickStatus != null && !clickStatus.getCell2update().isEmpty()) {
            lastPressed = new ArrayList<>(clickStatus.getCell2update().keySet());
        }
        update(clickStatus);
    }

    private void mouseReleased(MouseEvent event) {
        if (game.success())
            return;
        pressed2Up();
        Pos pos = getPos(event);
        ClickStatus clickStatus = null;

        if (event.isPrimaryButtonDown() || event.isSecondaryButtonDown()) {
            // double release
            System.out.println("double");
            leftAvailable = false;
            clickStatus = game.openNine(pos);
        } else if (event.getButton().name().equals("PRIMARY") && leftAvailable) {
            // left release
            System.out.println("left");
            if (firstClick) {
                game.init(pos);
                firstClick = false;
                timeline.play();
            }
            clickStatus = game.openOne(pos);
        }
        judge();
        update(clickStatus);
    }

    private void initial() {
        timerText.setText("0");
        safeLeftText.setText("381");
        timeline.stop();
        elapsed = 0;
        fail = false;
        firstClick = true;
        game = new Game(cfg.getNumWidth(), cfg.getNumHeight(), cfg.getNumMine());
        auto.setGame(game);
        for (int i = 0; i < cfg.getNumHeight(); i++) {
            for (int j = 0; j < cfg.getNumWidth(); j++) {
                setImage(new Pos(i, j), 10);
            }
        }
    }

    private void prompt() {
        List<Pos> check = auto.check();
        ClickStatus clickStatus = new ClickStatus();
        for (Pos posT : check) {
            clickStatus.put(posT, 12);
        }
        update(clickStatus);
    }

    private void autoPlay() {
        while (true) {
            List<Pos> check = auto.check();
            if (firstClick) {
                check = auto.randomClick();
                game.init(check.get(0));
                firstClick = false;
                timeline.play();
            }
            if (check.isEmpty()) {
                check = auto.randomClick();
            }
            for (Pos posT : check) {
                ClickStatus clickStatus = game.openOne(posT);
                // TODO ui没法立刻刷新
                update(clickStatus);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (clickStatus.isFail())
                    return;
            }
//            return;
//            if (game.success())
//                return;
        }
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        for (int i = 0; i < cfg.getNumHeight(); i++) {
            for (int j = 0; j < cfg.getNumWidth(); j++) {
                gridPane.add(imageViews[i][j], j, i);
            }
        }
        return gridPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
