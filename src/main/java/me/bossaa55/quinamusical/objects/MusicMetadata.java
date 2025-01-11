package me.bossaa55.quinamusical.objects;

import javafx.scene.image.Image;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicMetadata {
    public static Image getCoverArt(String filePath) {
        try {
            Logger logger = Logger.getLogger("org.jaudiotagger");
            logger.setLevel(Level.SEVERE);
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            Tag tag = audioFile.getTag();
            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] coverArtData =  artwork.getBinaryData(); // Returns the cover art as a byte array
                    if(coverArtData!=null){
                        return new Image(new ByteArrayInputStream(coverArtData));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the metadata from a music file
     * @param filePath Path to the music file
     * @return returns a list as [songName, artist, album]
     */
    public static String[] getSongData(String filePath) {
        // Extract metadata
        try {
            Logger logger = Logger.getLogger("org.jaudiotagger");
            logger.setLevel(Level.SEVERE);
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            Tag tag = audioFile.getTag();

            if (tag != null) {
                // Get song name (title)
                String songName = tag.getFirst(FieldKey.TITLE);

                // Get artist (author)
                String artist = tag.getFirst(FieldKey.ARTIST);

                // You can also extract other metadata
                String album = tag.getFirst(FieldKey.ALBUM);

                return new String[] {songName,artist,album};
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
