package me.bossaa55.quinamusical.items.manager;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ManagerQuinaListItem {
    private final VBox mainContainer = new VBox();
    private final Label label=new Label();

    public ManagerQuinaListItem(VBox root, String text){
        System.out.println(root.getChildren().size());
        Label originalLabel = (Label) root.getChildren().get(0);

        mainContainer.setPrefHeight(root.getPrefHeight());
        mainContainer.getStyleClass().addAll(root.getStyleClass());
        mainContainer.setPadding(root.getPadding());
        mainContainer.setAlignment(root.getAlignment());

        label.setText(text);
        label.setWrapText(true);
        label.getStyleClass().addAll(originalLabel.getStyleClass());

        mainContainer.getChildren().addAll(label);
    }

    public Pane getRoot() {
        return mainContainer;
    }

    public void select(){
        label.getStyleClass().add(0,"yellow-text");
        label.setStyle("-fx-text-fill: -fx-yellow;");
    }

    public void unselect(){
        label.getStyleClass().remove("yellow-text");
        label.setStyle(label.getStyle().replace("-fx-text-fill: -fx-yellow;",""));
    }

    public void setOnMouseClicked(EventHandler<MouseEvent> eventHandler) {
        mainContainer.setOnMouseClicked(eventHandler);
    }
}
