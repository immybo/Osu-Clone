import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Handles playing all sounds for MyOsu, including
 * - Music
 * - Game sounds e.g. hit sounds
 * 
 * Call init() before trying to use any sounds.
 * 
 * @author Robert Campbell
 */
public class AudioPlayer {
	// The currently playing long audio
	private static MediaPlayer currentAudio = null;
	
	// The currently playing audio clip
	private static Clip currentClip = null;
	
	/**
	 * Initialises AudioPlayer to be used.
	 */
	public static void init(){
		new JFXPanel();	// To initialise the class - this sometimes freezes it for a few seconds,
						// to which I haven't been able to find a solution.
	}
	
	/**
	 * Plays an audio file until stopLongAudio(fname) is called.
	 * Should be used only for long audio files.
	 * 
	 * @param fname The filename to load the audio from.
	 * @param startTime The time within the audio file to start at (ms).
	 * @param volume The volume of the audio; 0.5 is a reasonable volume.
	 */
	public static void playLongAudio(String fname, int startTime, double volume){
		// If no filename is given, don't do anything...
		if(fname == null) return;
		
		// If there is already a long audio currently playing, stop it
		if(currentAudio != null)
			currentAudio.stop();
		
		// Build the media player and add it to the map
		File file = new File(fname);
		Media media = new Media(file.toURI().toString());
		MediaPlayer audioPlayer = new MediaPlayer(media);
		
		currentAudio = audioPlayer;
		
		audioPlayer.setVolume(0.1);
		audioPlayer.setStartTime(new Duration(startTime));
		audioPlayer.play();
	}
	
	/**
	 * Pauses the long audio file which is currently being played.
	 * If no long audio file is currently being played, does nothing
	 * but returns false.
	 * 
	 * @return Whether or not pausing the audio succeeded.
	 */
	public static boolean pauseLongAudio(){
		// Check to see if the audio is currently playing:
		
		// If not, don't do anything but return false
		if(currentAudio == null){
			return false;
		}
		
		// If so, pause it
		currentAudio.pause();
		return true;
	}
	
	/**
	 * Resumes the audio file if it has been paused by 
	 * pauseLongAudio. If it has not, or if there is
	 * no long audio currently playing, does nothing.
	 */
	public static void resumeLongAudio(){
		// Check to see if there is any audio active;
		// if not, just don't do anything.
		if(currentAudio == null)
			return;
		
		// This will not do anything if the audio is not paused.
		currentAudio.play();
	}
	
	/**
	 * Stops the currently playing long audio file.
	 * Does nothing if no audio file is currently playing.
	 * 
	 * @return Whether or not stopping the audio succeeded.
	 */
	public static boolean stopLongAudio(String fname){
		// If there is no audio currently playing, don't do anything
		if(currentAudio == null)
			return false;
		
		currentAudio.stop();
		return true;
	}
	
	/**
	 * Plays a *short* audio file. This should not be used
	 * for audio over a few seconds long.
	 * 
	 * @param fname The filename of the audio to be played.
	 * @return Whether or not the audio clip was played.
	 */
	public static boolean playClip(String fname){
		if(fname == null) return false;
		
		// Stop the current clip if there is one
		if(currentClip != null){
			currentClip.stop();
			currentClip = null;
		}
		
		// Attempt to initialise the audio clip
		AudioInputStream inputStream = null;
		try{
			inputStream = AudioSystem.getAudioInputStream(new File(fname));
			AudioFormat decodedFormat = inputStream.getFormat();

			DataLine.Info audioInfo = new DataLine.Info(Clip.class, decodedFormat);
			currentClip = (Clip) AudioSystem.getLine(audioInfo);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Could not get input stream for audio clip. " + e);
		}

		try{
			if(inputStream == null) return false;
			currentClip.open(inputStream);
			currentClip.start();
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Couldn't play audio clip! " + e);
		}
		
		return true;
	}
	
	/**
	 * Terminates any audio currently playing
	 */
	public static void terminate(){
		if(currentClip != null)
			currentClip.stop();
		if(currentAudio != null)
			currentAudio.stop();
	}
}
