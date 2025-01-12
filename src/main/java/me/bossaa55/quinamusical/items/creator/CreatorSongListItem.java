package me.bossaa55.quinamusical.items.creator;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CreatorSongListItem {
    private final VBox mainContainer = new VBox();
    private final Label numerator = new Label();
    private final Button song = new Button("+");
    private final Button delete = new Button("X");
    private boolean selected = false;

    public CreatorSongListItem(VBox root, String text, int i){
        HBox originalSubContainer = (HBox) root.getChildren().get(0);
        Label originalNumerator = (Label) originalSubContainer.getChildren().get(0);
        Button originalSong = (Button) originalSubContainer.getChildren().get(1);
        Button originalDelete = (Button) originalSubContainer.getChildren().get(2);

        HBox subContainer = new HBox();
        subContainer.setAlignment(originalSubContainer.getAlignment());
        subContainer.setPrefWidth(originalSubContainer.getPrefWidth());
        subContainer.setSpacing(originalSubContainer.getSpacing());

        numerator.setText(String.format("%02d",i));
        numerator.setPrefWidth(originalNumerator.getPrefWidth());
        numerator.getStyleClass().addAll(originalNumerator.getStyleClass());

        song.setAlignment(originalSong.getAlignment());
        song.setPrefWidth(originalSong.getPrefWidth());
        song.getStyleClass().addAll(originalSong.getStyleClass());
        song.setPickOnBounds(false);

        delete.setPrefWidth(originalDelete.getPrefWidth());
        delete.getStyleClass().addAll(originalDelete.getStyleClass());
        delete.setPickOnBounds(false);

        if(!text.isEmpty()){
            song.setText(text);
            selected=true;
        }else{
            delete.setDisable(true);
        }

        subContainer.getChildren().addAll(numerator,song,delete);
        mainContainer.getChildren().add(subContainer);
    }

    public VBox getRoot(){
        return mainContainer;
    }

    public void setSelected(boolean b){
        selected=b;
        delete.setDisable(!b);
    }

    public boolean isSelected(){
        return selected;
    }

    public void setNumerator(int n){
        numerator.setText(String.format("%02d",n+1));
    }

    public void setSong(String title){
        song.setText(title);
    }

    public void setSongOnClick(EventHandler<MouseEvent> eventHandler){
        song.setOnMouseClicked(eventHandler);
    }

    public void setDeleteOnClick(EventHandler<MouseEvent> eventHandler){
        delete.setOnMouseClicked(eventHandler);
    }
}
