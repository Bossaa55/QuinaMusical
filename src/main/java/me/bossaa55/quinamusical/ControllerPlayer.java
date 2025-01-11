package me.bossaa55.quinamusical;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.util.Duration;
import me.bossaa55.quinamusical.objects.Song;
import me.bossaa55.quinamusical.objects.Utils;

import java.text.Normalizer;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class ControllerPlayer {

    @FXML
    private HBox infoContainer;

    @FXML
    private Label lbSongName;

    @FXML
    private Label lbSongAuthor;

    @FXML
    private ImageView pauseButton;

    @FXML
    private ImageView reiniciarButton;

    @FXML
    private Button btNewGame;

    @FXML
    private Button btNewSong;

    @FXML
    private VBox songListContainer;

    @FXML
    private Label songListItem;

    @FXML
    private TextField tfSearchSong;

    @FXML
    private ImageView ivSongCover;

    //Saves all the songs.
    private final ArrayList<Song> songs = new ArrayList<>();

    //Saves the number of songs that have been played
    private int nSongsPlayed=0;

    private MediaPlayer mediaPlayer;

    //Know if the music is playing or not (for the play/pause button).
    private boolean musicIsPlaying=false;
    //Stores the index of the song that is being played.
    private int playing=0;

    /**
     * Initializes the program. Reads the info.csv file and loads the files.
     * @param quina Quina root dir to get the data from
     */
    public void start(File quina) {
        //Load the info.csv file
        File musicInfoFile = new File(quina, "info.csv");
        try{
            //The file exists
            if(musicInfoFile.exists()){
                Scanner scanner = new Scanner(musicInfoFile);
                //Read the file content
                int nLine=0;
                ArrayList<String> filesNotFound = new ArrayList<>();
                while (scanner.hasNext()){
                    String line = scanner.nextLine();
                    String[] info = line.split(";");
                    if(info.length==2) { //Line format is valid
                        int duration = Utils.timeToSeconds(info[1]);
                        if(duration>=0) {//Duration is valid
                            //Load the music file
                            File f = new File(quina, "music/"+ info[0].trim());
                            if(f.exists()){
                                //If exists load it into the song list
                                songs.add(new Song(f, duration));
                            }
                            else {
                                filesNotFound.add(info[0].trim());
                            }
                            nLine++;
                        }else{
                            //The file format is not valid
                            Utils.raiseAlert(Alert.AlertType.ERROR, "Error",
                                    "El format del fitxer no és correcte\n" +
                                            "Linia "+nLine+": "+line);
                            Platform.exit();
                        }
                    }else{
                        //The file format is not valid
                        Utils.raiseAlert(Alert.AlertType.ERROR, "Error",
                                "El format del fitxer no és correcte\n" +
                                        "Linia "+nLine+": "+line);
                        Platform.exit();
                    }
                }

                //If there are files not found, notify the user
                if(!filesNotFound.isEmpty()){
                    StringBuilder sortida= new StringBuilder("No s'han trobat els següents fitxers:");
                    for(String s : filesNotFound){
                        sortida.append("\n").append(s);
                    }
                    Utils.raiseAlert(Alert.AlertType.WARNING, "Lost Media",sortida.toString());
                }

            }else{
                //The info.csv file was not found. Notify the user.
                Utils.raiseAlert(Alert.AlertType.ERROR, "Error","No s'ha trobat el fitxer: "+musicInfoFile.getAbsolutePath());
                Platform.exit();
            }
        }catch (FileNotFoundException ignored){
        }

        //Set the controllers to the default value
        lbSongName.setVisible(false);
        songListContainer.getChildren().remove(songListItem);
        pauseButton.setVisible(false);
        reiniciarButton.setVisible(false);
        btNewGame.setDisable(true);
    }

    /**
     * Sets and plays the music. Also displays the name, the author and the cover of the song if
     * exists in the song file metadata. If metadata is not found, the name of the file will be shown.
     * @param index The index of the song to be played in the song list.
     */
    private void setMedia(int index){
        //Stop the player if exists
        if(mediaPlayer!=null) mediaPlayer.stop();
        musicIsPlaying=false;
        //Create a new media with the song file.
        Media media = new Media(songs.get(index).getFile().toURI().toString());
        //Set the media to the media payer
        mediaPlayer = new MediaPlayer(media);
        //Wait for the player to be ready before seeking and playing.
        mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
            if (newStatus == MediaPlayer.Status.READY) {
                mediaPlayer.seek(Duration.seconds(songs.get(index).getStart())); //Seek to the start time
                //Play the music.
                playFadeIn();
            }
        });
        //Update the playing variable.
        playing=index;

        //Show the song data.
        lbSongName.setVisible(true);
        Image songCover = songs.get(index).getCover();
        if(songCover!=null) ivSongCover.setImage(songCover);
        else ivSongCover.setImage(Utils.getImageResource("disc.png"));
        lbSongName.setText(songs.get(index).getTitle());
        lbSongAuthor.setText(songs.get(index).getAuthor());
    }

    /**
     * Generates a new song to be played.
     * It is run by the newSong button.
     */
    @FXML
    void cancoNova() {
        //There's still music to be played.
        //This check is not really necessary because when all the songs have been played
        //the button is disabled. This is just for redundancy in case something goes wrong
        //and the user can still press the button.
        if(nSongsPlayed < songs.size()){
            Random r = new Random();
            int indx=r.nextInt(songs.size());
            while(songs.get(indx).isPlayed())indx=r.nextInt(songs.size());

            //Play the song.
            setMedia(indx);

            //Add the song to the played song list.
            addItemToList(indx);

            songs.get(indx).setPlayed(true);
            nSongsPlayed++;
            pauseButton.setVisible(true);
            reiniciarButton.setVisible(true);
            //If the maximum of songs is reached, the button is disabled.
            if(nSongsPlayed == songs.size()) btNewSong.setDisable(true);
            btNewGame.setDisable(false);
            //Clear the search text field.
            tfSearchSong.setText("");
            searchSong();
        }
    }

    /**
     * Pauses a song if is being played or plays it if it is paused.
     * Run by the play/pause button.
     */
    @FXML
    void pausarCanco() {
        if(musicIsPlaying){
            musicIsPlaying=false;
            mediaPlayer.pause();
            pauseButton.setImage(Utils.getImageResource("play.png"));
        }else{
            if(mediaPlayer!=null){
                playFadeIn();
            }
        }
    }

    /**
     * Warns the user before resetting, if user agrees, resets all the values
     * and controllers to the default values.
     * Run by the new game button.
     */
    @FXML
    void partidaNova() {
        Optional<ButtonType> result= Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Partida Nova",
                "Segur que vols començar una partida nova? " +
                        "Tots els valors es posen per defecte, no es pot desfer.");
        if(result.get() == ButtonType.OK) {
            mediaPlayer.stop();
            musicIsPlaying = false;
            mediaPlayer = null;
            lbSongName.setVisible(false);
            pauseButton.setVisible(false);
            reiniciarButton.setVisible(false);
            btNewSong.setDisable(false);
            btNewGame.setDisable(true);
            songListContainer.getChildren().removeAll(songList);
            songList.clear();
            tfSearchSong.setText("");
            nSongsPlayed =0;
            for(Song m : songs) m.setPlayed(false);
        }
    }

    /**
     * Seeks the song to the start time.
     * Run by the reload button.
     */
    @FXML
    void reiniciarCanco() {
        Duration d = Duration.seconds(songs.get(playing).getStart());
        mediaPlayer.seek(d);
        playFadeIn();
    }

    List<Label> songList= new ArrayList<>();

    /**
     * Creates an item to put in the list.
     * If metadata exists, it puts the song name, else it puts the file name as the song title.
     * @param index The index of the song to be put in the list.
     */
    private void addItemToList(int index){
        Label label = new Label((songList.size()+1)+". "+ songs.get(index).getTitle());
        label.getStyleClass().addAll(songListItem.getStyleClass());
        final int i=index;
        label.setOnMouseClicked(event ->{
            setMedia(i);
        });
        songList.add(0,label);
        songListContainer.getChildren().add(0,label);
    }

    /**
     * Gets the search text field text and applies the filter
     * to the list deleting or creating elements.
     */
    @FXML
    void searchSong() {
        songListContainer.getChildren().removeAll(songList);
        for(Label l : songList){
            if(stringContains(l.getText(), tfSearchSong.getText())){
                songListContainer.getChildren().add(l);
            }
        }
    }

    /**
     * Compares two strings, looks if a sequence appears in a string.
     * It ignores case and accents and stuff.
     * @param string The string to look in to
     * @param sequence The string to be found in the first string
     * @return Returns true if the sequence is found, else false.
     */
    private boolean stringContains(String string, String sequence){
        String normalized1 = normalize(string);
        String normalized2 = normalize(sequence);
        for (int i = 0; i < string.length()-sequence.length(); i++) {
            if(normalized1.substring(i,i+sequence.length()).equalsIgnoreCase(normalized2))return true;
        }
        return false;
    }

    /**
     * Normalizes a string removing accents and stuff.
     * @param input String to be normalized
     * @return Normalized string
     */
    private static String normalize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    /**
     * Plays the song with a fade in effect.
     * MusicPlayer can't be null (it is not checked)
     */
    private void playFadeIn() {
        musicIsPlaying=true;
        pauseButton.setImage(Utils.getImageResource("pause.png"));
        mediaPlayer.setVolume(0);
        mediaPlayer.play();
        Timeline fadeInTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(50), event -> {
            if (mediaPlayer.getVolume() < 1) {
                mediaPlayer.setVolume(mediaPlayer.getVolume() + 0.1);
            } else {
                mediaPlayer.setVolume(1);
                fadeInTimeline.stop();
            }
        });
        fadeInTimeline.getKeyFrames().add(keyFrame);
        fadeInTimeline.setCycleCount(Timeline.INDEFINITE);
        fadeInTimeline.play();
    }

    /*
    INFO LINKS
     */
    @FXML
    void openInfo(MouseEvent event) {
        infoContainer.setVisible(true);
    }

    @FXML
    void closeInfo(ActionEvent event) {
        infoContainer.setVisible(false);
    }

    @FXML
    void obrirDocumentacio(ActionEvent event) {
        openWebpage("https://github.com/Bossaa55/QuinaMusical");
    }

    @FXML
    void obrirGithub(ActionEvent event) {
        openWebpage("https://github.com/Bossaa55");
    }

    @FXML
    void obrirLinkedin(ActionEvent event) {
        openWebpage("https://www.linkedin.com/in/jordi-bossacoma-frigola-9b2054224/");
    }

    private void openWebpage(String urlString) {
        try {
            Desktop.getDesktop().browse(new URI(urlString));
        } catch (IOException | URISyntaxException ignored) {
        }
    }
}