package me.bossaa55.quinamusical;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import me.bossaa55.quinamusical.items.manager.ManagerQuinaListItem;
import me.bossaa55.quinamusical.items.manager.ManagerSongListItem;
import me.bossaa55.quinamusical.objects.Song;
import me.bossaa55.quinamusical.objects.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ControllerQuinesManager implements Initializable {

    private final String ROOT_DIR = "quines";

    @FXML
    private AnchorPane root;

    @FXML
    private VBox songsContainer;

    @FXML
    private VBox quinaDataContainer;

    @FXML
    private VBox quinaListItemContainer;

    @FXML
    private Label labelQuinaData;

    @FXML
    private Label labelQuinaTitle;

    @FXML
    private VBox quinaListContainer;

    @FXML
    private HBox songListItemContainer;

    @FXML
    private HBox containerNSongs;

    @FXML
    private Label lbNSongs;

    private ArrayList<File> quines = new ArrayList<>();
    private int selectedQuina = -1;
    private ArrayList<ManagerQuinaListItem> quinaListItems = new ArrayList<>();


    /**
     * Run by the Afegir Quina button. Opens a new windows with the ControllerQuinaNova controller.
     */
    @FXML
    void crearQuina() {
        openNewQuinaScreen(-1);
    }

    @FXML
    void editarQuina(ActionEvent event) {
        openNewQuinaScreen(selectedQuina);
    }

    /**
     * Opens a new windows with the ControllerQuinaNova controller.
     * If a quina is specified, the new widow will load the quina to be edited.
     * @param nQuina To edit a quina, the index of the quina, to create a new one, -1
     */
    private void openNewQuinaScreen(int nQuina){
        try {
            // Carregar la pantalla
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CrearQuina.fxml"));
            Parent quinaScreen = loader.load();

            // Carregar el controlador de la nova pantalla
            ControllerQuinaNova quinaController = loader.getController();

            // Obrir la pantalla
            Stage stage = new Stage();
            stage.setScene(new Scene(
                    quinaScreen,
                    1000,800));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/icon.png"))));
            stage.setOnCloseRequest(e ->{
                if(!ControllerQuinaNova.isCanvisGuardats()) {
                    Optional<ButtonType> result = Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Tancar",
                            "Segur que vol tancar? Els canvis no es guardaran.");
                    if (result.get() == ButtonType.OK) {
                        stage.close();
                    } else e.consume();
                }
            });

            if(nQuina>=0){
                stage.setTitle("Editar Quina");
                // Passar la quina a obrir
                quinaController.iniciar(quines.get(nQuina));
            }else{
                stage.setTitle("Crear Quina");
                quinaController.iniciar(null);
            }

            stage.showAndWait();
            updateQuines();
        }catch (IOException e){e.printStackTrace();}
    }

    /**
     * If a quina is selected, it will open a new window with the QuinaPlayer controller.
     */
    @FXML
    void startQuinaPlayer() {
        File quina = quines.get(selectedQuina);
        if(quina!=null){
            try {
                // Carregar la pantalla
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Player.fxml"));
                Parent quinaScreen = loader.load();

                // Carregar el controlador de la nova pantalla
                ControllerPlayer quinaController = loader.getController();
                // Passar la quina a obrir
                quinaController.start(quina);

                // Obrir la pantalla
                Stage stage = new Stage();
                stage.setScene(new Scene(
                        quinaScreen,
                        Screen.getPrimary().getVisualBounds().getWidth()*0.9,
                        Screen.getPrimary().getVisualBounds().getHeight()*0.9));
                stage.setMaximized(true);
                stage.setTitle("Quina Musical - "+quina.getName());
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/icon.png"))));
                stage.show();
                Stage currentStage = (Stage) root.getScene().getWindow();
                currentStage.close();
            }catch (IOException e){}
        }
    }

    //private File quinaSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Posar els elements per defecte
        quinaDataContainer.setVisible(false);
        labelQuinaData.setVisible(true);
        containerNSongs.setVisible(false);

        updateQuines();
    }

    private void updateQuines(){
        quines.clear();
        //Llegir les quines que hi ha guardades
        File root = new File(ROOT_DIR);
        if(root.exists()){
            if(root.isDirectory()){
                //Llistar el contingut del directori
                File[] items=root.listFiles();
                assert items != null;
                for(File item : items){
                    //Si és un directori l'afegim a quines
                    if(item.isDirectory())quines.add(item);
                }
            }
        }

        //Posar les quines a la pantalla
        quinaListContainer.getChildren().clear();
        quinaListItems.clear();
        for (int i = 0; i < quines.size(); i++) {
            ManagerQuinaListItem managerQuinaListItem = new ManagerQuinaListItem(quinaListItemContainer, quines.get(i).getName());
            final int j=i;
            managerQuinaListItem.setOnMouseClicked(event ->{
                if(selectedQuina>=0)quinaListItems.get(selectedQuina).unselect();
                selectQuina(j);
                managerQuinaListItem.select();
            });

            quinaListContainer.getChildren().add(managerQuinaListItem.getRoot());
            quinaListItems.add(managerQuinaListItem);
        }
    }

    private void selectQuina(int index){
        File quina = quines.get(index);
        //Validar l'estructura de la quina
        File musicDir = new File(quina, "music");
        File infoFile = new File(quina, "info.csv");
        ArrayList<Song> songs = new ArrayList<>();
        if(musicDir.exists()){
            if(musicDir.isDirectory()){
                if(infoFile.exists()){
                    if(infoFile.isFile()){
                        selectedQuina=index;
                        try {
                            Scanner read = new Scanner(infoFile);
                            while(read.hasNextLine()){
                                String[] linia = read.nextLine().split(";");
                                songs.add(new Song(linia[0], Utils.timeToSeconds(linia[1])));
                            }
                            read.close();
                        } catch (FileNotFoundException ignored) {} //Already checked

                        //Show the songs to the user
                        songsContainer.getChildren().clear();
                        for(int i = 0; i < songs.size(); i++){ //Create an item for each song
                            ManagerSongListItem managerSongListItem = new ManagerSongListItem(songListItemContainer, i+1, songs.get(i).getTitle(), Utils.formatarSegons(songs.get(i).getStart()));
                            songsContainer.getChildren().add(managerSongListItem.getRoot());
                        }

                        labelQuinaTitle.setText(quina.getName());
                        lbNSongs.setText(String.valueOf(songs.size()));

                        labelQuinaData.setVisible(false);
                        containerNSongs.setVisible(true);
                        quinaDataContainer.setVisible(true);
                    } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "'info.csv' hauria de ser un fitxer");
                } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "El fitxer 'info.csv' no existeix");
            } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "'music' hauria de ser un directori");
        } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "El directori 'music' no existeix");
    }
}
