package me.bossaa55.quinamusical.items.manager;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ManagerSongListItem {
    private final HBox mainContainer = new HBox();

    public ManagerSongListItem(HBox root, int numeroP, String titolP, String puntIniciP){
        Label originalNumber = (Label) root.getChildren().get(0);
        HBox originalSubContainer = (HBox) root.getChildren().get(1);
        Label originalTitol = (Label) originalSubContainer.getChildren().get(0);
        Label originalPuntInici = (Label) root.getChildren().get(2);

        Label number = new Label();
        HBox subContainer = new HBox();
        Label titol = new Label();
        Label puntInici = new Label();

        mainContainer.setSpacing(root.getSpacing());
        mainContainer.setPadding(root.getPadding());

        number.setText(String.format("%02d",numeroP));
        number.getStyleClass().addAll(originalNumber.getStyleClass());

        HBox.setHgrow(subContainer, Priority.ALWAYS);

        titol.setText(titolP);
        titol.getStyleClass().addAll(originalTitol.getStyleClass());
        titol.prefWidthProperty().bind(subContainer.widthProperty().subtract(10));

        subContainer.getChildren().addAll(titol);

        puntInici.setText(puntIniciP);
        puntInici.getStyleClass().addAll(originalPuntInici.getStyleClass());

        mainContainer.getChildren().addAll(number,subContainer,puntInici);
    }

    public HBox getRoot(){
        return mainContainer;
    }

    public void setOnMouseClicked(EventHandler<MouseEvent> eventHandler) {
        mainContainer.setOnMouseClicked(eventHandler);
    }
}
