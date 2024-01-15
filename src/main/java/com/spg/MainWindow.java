package com.spg;

import com.spg.bean.MetaInfo;
import com.spg.bean.Result;
import com.spg.service.RecordService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private RecordService recordService;
    private Text timerText = new Text();
    private Text safeLeftText = new Text();
    private ImageView[][] imageViews;
    private final Map<Integer, Image> imageMap = new HashMap<>();
    private boolean firstClick = true;
    private final Logger logger = LogManager.getLogger();
    // 毫秒
    private int elapsed;
    private Timeline timeline;
    private boolean fail = false;
    private List<Pos> lastPressed;
    private Pos lastEntered;
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
                throw new RuntimeException(entry.getValue() + " load error");
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
                imageViews[i][j].setOnMouseEntered(this::mouseEnered);
                imageViews[i][j].setOnMouseExited(this::mouseExited);
            }
        }
        game = new Game(cfg.getNumWidth(), cfg.getNumHeight(), cfg.getNumMine());
        auto = new Auto(game);
        recordService = new RecordService();
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
//        System.exit(-1);
        GridPane gridPane = new GridPane();
        for (int i = 0; i < cfg.getNumHeight(); i++) {
            for (int j = 0; j < cfg.getNumWidth(); j++) {
                gridPane.add(imageViews[i][j], j, i);
            }
        }

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
        Button resetButton = new Button("reset");
        resetButton.setPrefHeight(40);
        resetButton.setOnAction(actionEvent -> initial());
        Button historyButton = new Button("history");
        historyButton.setPrefHeight(40);
        historyButton.setOnAction(actionEvent -> {
            ListView<String> historyListView = getHistoryListView();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getDialogPane().setContent(historyListView);
            alert.show();
        });
        Button promptButton = new Button("prompt");
        promptButton.setPrefHeight(40);
        promptButton.setOnAction(actionEvent -> prompt());

        header.getChildren().add(timerText);
        header.getChildren().add(safeLeftText);
        header.getChildren().add(resetButton);
        header.getChildren().add(promptButton);
        header.getChildren().add(historyButton);
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
        //失败
        if (fail) {
            // 创建一个Alert对象
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Choose an option:");

            // 添加两个按钮
            ButtonType buttonMistake = new ButtonType("失误");
            ButtonType buttonBadluck = new ButtonType("运气不好");
            ButtonType passBadluck = new ButtonType("取消");

            alert.getButtonTypes().setAll(buttonMistake, buttonBadluck, passBadluck);
            alert.showAndWait().ifPresent(response -> {
                if (response == buttonMistake) {
                    recordService.addMistake();
                } else if (response == buttonBadluck) {
                    recordService.addBadluck();
                }
            });
            initial();
        } else if (game.success()) {
            int threeBV = game.computeThreeBV();
            Result result = new Result(threeBV, elapsed / 1000., LocalDate.now());
            String error = null;
            try {
                recordService.addSuccess(result);
            } catch (Exception e) {
                error = e.getMessage();
                logger.error("{}", e.getMessage(), e);
            }
            ListView<String> historyListView = getHistoryListView();
            historyListView.getItems().add(0, result.toString());
            historyListView.getItems().add(1, "");
            if (error != null) {
                historyListView.getItems().add(0, error);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("统计信息");
            alert.setHeaderText("header");

            alert.getDialogPane().setContent(historyListView);
            alert.showAndWait();
            initial();
        }
    }

    private void mouseEnered(MouseEvent event) {
        if (game.success() || fail)
            return;
        Pos pos = getPos(event);
        ClickStatus clickStatus = game.enter(pos);
        if (!clickStatus.getCell2update().isEmpty()) {
            lastEntered = pos;
            update(clickStatus);
        }
    }

    private void mouseExited(MouseEvent event) {
        if (game.success() || fail)
            return;
        if (lastEntered != null) {
            ClickStatus clickStatus = game.exit(lastEntered);
            update(clickStatus);
            lastEntered = null;
        }
    }

    private void mousePressed(MouseEvent event) {
        if (game.success() || fail)
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
        if (game.success() || fail)
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
        if (game.success() || fail)
            return;
        pressed2Up();
        Pos pos = getPos(event);
        ClickStatus clickStatus;

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
        } else {
            return;
        }

        fail = clickStatus.isFail();
        update(clickStatus);
        judge();
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


    private ListView<String> getHistoryListView() {
        List<Result> maxThreeBVPS = recordService.getMaxThreeBVPS();
        List<Result> minElapsed = recordService.getMinElapsed();
        MetaInfo metaInfo = recordService.getMetaInfo();

        ListView<String> listView = new ListView<>();
        listView.setMinWidth(400);

        listView.getItems().add("统计信息");
        int success = metaInfo.getSuccess();
        int mistake = metaInfo.getMistake();
        int badluck = metaInfo.getBadluck();
        int total = success + mistake + badluck;
        long successRatio = Math.round(100. * success / total);
        long mistakeRatio = Math.round(100. * mistake / total);
        long badluckRatio = Math.round(100. * badluck / total);
        listView.getItems().add("胜场/失误/运气(%d,%d,%d),(%d%%/%d%%/%d%%)".formatted(success, mistake, badluck, successRatio, mistakeRatio, badluckRatio));
        listView.getItems().add("最高连胜:%s,最高连败:%s,当前连胜:%s".formatted(metaInfo.getMaxWinStreak(), metaInfo.getMaxLossStreak(), metaInfo.getCurrStreak()));
        listView.getItems().add("");

        listView.getItems().add("最短时间");
        for (Result result : minElapsed) {
            listView.getItems().add(result.toString());
        }
        listView.getItems().add("");

        listView.getItems().add("最大3bv/s");
        for (Result result : maxThreeBVPS) {
            listView.getItems().add(result.toString());
        }
        listView.getItems().add("");
        return listView;
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        recordService.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
