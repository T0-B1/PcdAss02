package commons;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.Executors;

public class View {

    private final Controller controller;
    private final MyGraph graph;

    public View(Controller controller, MyGraph graph) {
        this.controller = controller;
        this.graph = graph;
    }

	private void setup(Stage primaryStage) {

        AnchorPane anchor = new AnchorPane();
        ToolBar toolbar = new ToolBar();
        Pane graphPane = graph.getPanel();

        Label lblWikiURL = new Label("Wikipedia URL:");
        TextField txtURL = new TextField("https://en.wikipedia.org/wiki/University_of_Bologna");
        txtURL.setPrefWidth(300.0);
        Label lblDepth = new Label("Depth:");
        TextField txtDepth = new TextField("3");
        txtDepth.setPrefWidth(25.0);
        Button btnStart = new Button("Start");
        Label lblInfo = new Label("Zoom: PgUp/PgDown Move: Arrows");
        btnStart.setOnAction(actionEvent -> {
            controller.start(txtURL.getText(), Integer.parseInt(txtDepth.getText()));
            graphPane.requestFocus();
        });

        toolbar.getItems().addAll(lblWikiURL, txtURL, lblDepth, txtDepth, lblInfo, btnStart);
        toolbar.setMinHeight(40.0);
        toolbar.setPrefHeight(40.0);

        EventHandler consume = event -> event.consume();
        anchor.setOnKeyPressed(consume);
        AnchorPane.setTopAnchor(toolbar, 0.0);
        AnchorPane.setLeftAnchor(toolbar, 0.0);
        AnchorPane.setRightAnchor(toolbar, 0.0);
        AnchorPane.setTopAnchor(graphPane, 40.0);
        AnchorPane.setBottomAnchor(graphPane, 0.0);
        AnchorPane.setLeftAnchor(graphPane, 0.0);
        AnchorPane.setRightAnchor(graphPane, 0.0);
        anchor.getChildren().addAll(toolbar, graphPane);
  		primaryStage.setScene(new Scene(anchor));
  		primaryStage.setOnCloseRequest(windowEvent -> controller.stop());
		primaryStage.show();
	}

	public void start() {
        PlatformImpl.startup(() -> {});
        Platform.runLater(() -> {
            try {
                setup(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
