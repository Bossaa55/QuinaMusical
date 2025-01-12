package me.bossaa55.quinamusical;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.bossaa55.quinamusical.items.creator.CreatorSongListItem;
import me.bossaa55.quinamusical.objects.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ControllerQuinaNova {
    private final String ROOT_DIRECTORY = Paths.get("").toAbsolutePath().toString();

    @FXML
    private AnchorPane root;

    @FXML
    private VBox songListContainer;

    @FXML
    private VBox songListItemContainer;

    @FXML
    private VBox playerContainer;

    @FXML
    private HBox loadingContainer;

    @FXML
    private ImageView ivPlay;

    @FXML
    private Label lbStatus;

    @FXML
    private Label lbStatusDescription;

    @FXML
    private Label lbTime;

    @FXML
    private Label quinaTitleLabel;

    @FXML
    private Slider playerSlider;

    @FXML
    private ScrollPane spSongList;

    @FXML
    private TextField tfQuinaTitle;

    private final FileChooser fileChooser=new FileChooser();
    private final ArrayList<String> songsPath = new ArrayList<>();
    private final ArrayList<Integer> songsStartTime = new ArrayList<>();
    private final ArrayList<CreatorSongListItem> songListItems = new ArrayList<>();
    private String lastOpenedDirectory = "";

    private MediaPlayer mediaPlayer;
    private Media media;
    private final Timeline playerTimeline = new Timeline();

    private int songSelected=-1;
    private boolean playing=false;
    private static boolean changesSaved =true;

    private File quina;

    public static boolean isChangesSaved(){
        return changesSaved;
    }


    /**
     * Checks if the quina can be saved. If positive, runs the saveFiles() method and sets the savedChanges to true.
     * Run by the save button.
     */
    @FXML
    void saveQuina() {
        if(!tfQuinaTitle.getText().isEmpty()) {
            if(!songsPath.isEmpty()){
                lbStatus.setText("Guardant arxius...");
                loadingContainer.setVisible(true);
                saveFiles();
                changesSaved =true;
                loadingContainer.setVisible(false);
            }
        }
    }

    /**
     * If changes haven't been saved, warns the user. If user agrees, the window closes.
     * Run by the close button.
     */
    @FXML
    void close() {
        if(!changesSaved) {
            Optional<ButtonType> result = Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Tancar",
                    "Segur que vol tancar? Els canvis no es guardaran.");
            if (result.get() == ButtonType.OK) {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
            }
        }else{
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Seeks the MediaPlayer to the time of the slider when this one is released.
     * Run by the player slider.
     */
    @FXML
    void sliderPlayerRelease(){
        mediaPlayer.seek(Duration.seconds(playerSlider.getValue()));
        lbTime.setText(Utils.formatarSegons((int) playerSlider.getValue()));
    }

    /**
     * Saves the seek time of the slider.
     * Run by the save button in the player.
     */
    @FXML
    void saveStartTime() {
        songsStartTime.set(songSelected, (int) playerSlider.getValue());
        changesSaved =false;
    }

    /**
     * Plays or pauses the MediaPlayer according to if the music is being played.
     * Run by the play/pause button.
     */
    @FXML
    void togglePlay(){
        if(playing){
            mediaPlayer.pause();
            playerTimeline.stop(); //Stop the timeline that moves the player Slider.
            ivPlay.setImage(Utils.getImageResource("play.png"));
            playing=false;
        }else{
            mediaPlayer.play();
            playerTimeline.play();
            ivPlay.setImage(Utils.getImageResource("pause.png"));
            playing=true;
        }
    }

    /**
     * Seeks the MediaPlayer and Slider to the stored start time.
     */
    @FXML
    void resetPlayer(){
        mediaPlayer.seek(Duration.seconds(songsStartTime.get(songSelected)));
        playerSlider.setValue(songsStartTime.get(songSelected));
        lbTime.setText(Utils.formatarSegons((int) playerSlider.getValue()));
    }

    /**
     * Loads the program. If a quina is passed, the files are loaded and the music shown.
     * @param quinaToEdit To edit a quina, the File object of the quina dir, else, to create a new one null.
     */
    public void start(File quinaToEdit){
        quina=quinaToEdit;
        //Define the music type files that javafx supports.
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tots", "*.mp3", "*.wav", "*.ogg", "*.flac"),
                new FileChooser.ExtensionFilter("MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("WAV", "*.wav"),
                new FileChooser.ExtensionFilter("OGG", "*.ogg"),
                new FileChooser.ExtensionFilter("FLAC", "*.flac")
        );

        //Reset the controllers
        loadingContainer.setVisible(false);
        songListContainer.getChildren().clear();

        if(quina!=null){ //Edit
            quinaTitleLabel.setText("Editar Quina");
            //Read the info.csv file and get its data.
            File info = new File(quina,"info.csv");
            if(info.exists()) {
                try {
                    Scanner scanner = new Scanner(info);
                    while(scanner.hasNextLine()){
                        String[] line = scanner.nextLine().split(";");
                        if(line.length==2){
                            songsPath.add(quina.getAbsolutePath()+"/music/"+line[0]);
                            songsStartTime.add(Utils.timeToSeconds(line[1]));
                            addItemList(line[0]); //Create an item for each song.
                        }
                    }
                    scanner.close();
                } catch (FileNotFoundException ignored) {
                }
            }
            //TODO: Warn the user when the file is not found, or it is corrupt.
            tfQuinaTitle.setText(quina.getName());
        }
        //Add an empty item to the list.
        addItemList();
        //Key frame for the player Slider animation.
        KeyFrame kayFramePlayer = new KeyFrame(Duration.millis(1000), ev ->{
            Duration currentTime = mediaPlayer.getCurrentTime();
            playerSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
            lbTime.setText(Utils.formatarSegons((int)currentTime.toSeconds()));
        });
        playerTimeline.getKeyFrames().add(kayFramePlayer);
        playerTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Creates an item for the song list.
     * @param text If text is empty, it creates an empty item, else it sets the song name of the text.
     */
    private void addItemList(String text){
        int number = songsPath.size();
        if(text.isEmpty()) number++;
        CreatorSongListItem creatorSongListItem = new CreatorSongListItem(songListItemContainer, text, number);

        creatorSongListItem.setSongOnClick(event ->{
            if(!creatorSongListItem.isSelected()) { //User has to choose one or multiple songs.
                try {
                    //If the FileChooser has been opened before, the last dir will be saved.
                    if (!lastOpenedDirectory.isEmpty()) {
                        //Check if the dir still exists
                        File d = new File(lastOpenedDirectory);
                        if (d.exists()) {
                            //If exists, set the initial directory to the last directory.
                            if (d.isDirectory()) fileChooser.setInitialDirectory(d);
                            else fileChooser.setInitialDirectory(null);
                        } else fileChooser.setInitialDirectory(null);
                    }
                    //Open the file explorer with the FileChooser.
                    List<File> files = fileChooser.showOpenMultipleDialog(root.getScene().getWindow());
                    if(files!=null) {
                        //Get the firs file and put it on the clicked button.
                        if(!files.isEmpty()) {
                            creatorSongListItem.setSong(files.get(0).getName());
                            songsPath.add(files.get(0).getAbsolutePath());
                            songsStartTime.add(0);
                            lastOpenedDirectory = files.get(0).getParentFile().getAbsolutePath();
                            creatorSongListItem.setSelected(true);
                            changesSaved = false;
                        }
                        //Get the rest of the files and create a list item for each one.
                        for (int i = 1; i < files.size(); i++) {
                            if (files.get(i).isFile()) {
                                addItemList(files.get(i).getName());
                                //Save the path and the default start time (0).
                                songsPath.add(files.get(i).getAbsolutePath());
                                songsStartTime.add(0);
                            }
                        }
                        //Open the player to the last song.
                        openPlayer(songListItems.get(songListItems.size()-1));
                        addItemList(); //Add an empty item.
                        //Pause for 100 mills to wait for the items to be added before seeking to the bottom of the ScrollPane.
                        PauseTransition pause = new PauseTransition(Duration.millis(100));
                        pause.setOnFinished(e -> spSongList.setVvalue(1));
                        pause.play();
                    }
                } catch (Exception ignored) {
                    //The user closed the FileChooser, ignored.
                }
            }else{ //If a song is already selected for the item, show the Player.
                openPlayer(creatorSongListItem);
            }
        });

        creatorSongListItem.setDeleteOnClick(event ->{
            //Get the index of the clicked song.
            int index= songListItems.indexOf(creatorSongListItem);
            //Remove the item from the lists.
            songsPath.remove(index);
            songListItems.remove(index);
            songListContainer.getChildren().remove(creatorSongListItem.getRoot());
            //Update the numeration of the items.
            for (int i = index; i < songListItems.size(); i++) {
                songListItems.get(i).setNumerator(i);
            }
            changesSaved =false;
        });

        //Add the item to the lists.
        songListContainer.getChildren().add(creatorSongListItem.getRoot());
        songListItems.add(creatorSongListItem);
    }

    /**
     * Creates an empty item in the song list
     */
    private void addItemList(){
        addItemList("");
    }

    /**
     * Opens the player to the item of the songs list passed.
     * @param songListItem Item to open the player (can not be null).
     */
    private void openPlayer(CreatorSongListItem songListItem){
        int index=songListItems.indexOf(songListItem);
        if(songSelected>=0){ //If the player already is in an item, it removes it.
            songListItems.get(songSelected).getRoot().getChildren().remove(playerContainer);
        }
        playerSlider.setValue(0);

        //Create the MediaPlayer wit the new Media.
        if(mediaPlayer!=null) mediaPlayer.stop();
        mediaPlayer=null;
        File f = new File(songsPath.get(index));

        if(f.exists()){ //Check if the file exists.
            media = new Media(f.toURI().toString());
            mediaPlayer=new MediaPlayer(media);
            //Wait for the MediaPlayer to be ready.
            mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
                for(String s : media.getMetadata().keySet()) System.out.println(s);
                if (newStatus == MediaPlayer.Status.READY) {
                    //Set up the Player.
                    mediaPlayer.seek(Duration.seconds(songsStartTime.get(songSelected)));
                    playerSlider.setMax(mediaPlayer.getCycleDuration().toSeconds());
                    playerSlider.setValue(songsStartTime.get(index));
                    lbTime.setText(Utils.formatarSegons(songsStartTime.get(index)));
                }
            });
        }//TODO: Warn the user when the file does not exist.

        //Put the player to the passed list item.
        songListItem.getRoot().getChildren().add(playerContainer);
        songSelected=index;
    }

    /**
     * Saves the unsaved files to the dir.
     */
    private void saveFiles(){
        //TODO: Warn the user if any of the operations goes wrong.
        File directori = new File(ROOT_DIRECTORY +"/quines/"+ tfQuinaTitle.getText()+"/music");
        if(quina!=null){
            if(!quina.getName().equals(tfQuinaTitle.getText())){
                File nouDirectori = new File(ROOT_DIRECTORY +"/quines/"+ tfQuinaTitle.getText());
                quina.renameTo(nouDirectori);
            }
            File[] files = directori.listFiles();
            if(files!=null) {
                for (File f : files) {
                    if (!songsPath.contains(f.getAbsolutePath())) f.delete();
                }
            }
        }

        //If the directories don't exist, create them.
        if(!directori.exists())directori.mkdirs();
        //Copy each file of the paths list.
        for(String r : songsPath){
            File f1 = new File(r);
            File f2 = new File(directori,f1.getName());
            //If the paths are different, copies the files. (If the paths are equal means the file is already saved).
            if(!f1.getAbsolutePath().equalsIgnoreCase(f2.getAbsolutePath())) {
                lbStatusDescription.setText(r);
                //Check if the file still exists.
                if(f1.exists()) {
                    //Check if a file with the same name exists.
                    if (f2.exists()) {
                        //If exists, ask the user if he wants to overwrite it.
                        Optional<ButtonType> result = Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Sobreescriure fitxer",
                                "El fitxer '" + f2.getAbsolutePath() + "' ja existeix. El vol sobreescriure?");
                        if (result.get() == ButtonType.OK) {
                            copiarFitxers(f1.toPath(), f2.toPath());
                        }
                    } else {
                        copiarFitxers(f1.toPath(), f2.toPath());
                    }
                }
            }
        }

        //Save the music and the start time in the info.csv file.
        try{
            File info = new File(ROOT_DIRECTORY +"/quines/"+ tfQuinaTitle.getText()+"/info.csv");
            if(!info.exists()) info.createNewFile();
            PrintStream ps = new PrintStream(info);
            for (int i = 0; i < songsPath.size(); i++) {
                File f = new File(songsPath.get(i));
                ps.println(f.getName()+";"+Utils.formatarSegons(songsStartTime.get(i)));
            }
            ps.close();
        }catch (Exception ignored){
        }
    }

    /**
     * Copies a file from path A to path B.
     * @param origin original file.
     * @param destination destination file.
     */
    private void copiarFitxers(Path origin, Path destination){
        try {
            Files.copy(origin,destination);
        } catch (IOException ignored) {
        }
    }
}
