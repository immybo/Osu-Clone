import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

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
	// A map of audio filenames to mediaPlayers for audio which is currently playing
	private static Map<String, MediaPlayer> currentAudio;
	
	// A map of audio filenames to short clips that can be played
	private static Map<String, Clip> audioClips;
	// A map of audio filesnames to audio streams for clips that can be played
	private static Map<String, AudioInputStream> audioStreams;
	
	/**
	 * Initialises AudioPlayer to be used.
	 */
	public static void init(){
		currentAudio = new HashMap<String, MediaPlayer>();
		audioClips = new HashMap<String, Clip>();
		audioStreams = new HashMap<String, AudioInputStream>();
		new JFXPanel(); // Just to initialise the class, it's a bit buggy
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
		
		// Build the media player and add it to the map
		File file = new File(fname);
		Media media = new Media(file.toURI().toString());
		MediaPlayer audioPlayer = new MediaPlayer(media);
		currentAudio.put(fname, audioPlayer);
		
		audioPlayer.setVolume(0.1);
		audioPlayer.setStartTime(new Duration(startTime));
		audioPlayer.play();
	}
	
	/**
	 * Pauses an audio file previously started by playLongAudio
	 * if a filename which is currently playing is given.
	 * 
	 * @param fname The filename of the audio to pause.
	 * @return Whether or not pausing the audio succeeded.
	 */
	public static boolean pauseLongAudio(String fname){
		// If no filename is given, don't do anything
		if(fname == null) return false;
		
		// Check to see if the audio is currently playing
		if(currentAudio.containsKey(fname)){
			// If it is, pause the audio
			currentAudio.get(fname).pause();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Resumes an audio file previously paused by pauseLongAudio
	 * if a filename which is currently playing/paused is given.
	 * Does nothing if the audio file is currently playing.
	 * 
	 * @param fname The filename of the audio to resume.
	 */
	public static void resumeLongAudio(String fname){
		// If no filename is given, don't do anything
		if(fname == null) return;
		
		// Check to see if the audio is currently playing or paused
		if(currentAudio.containsKey(fname)){
			// If it is, resume it (does nothing if paused)
			currentAudio.get(fname).play();
		}
	}
	
	/**
	 * Stops an audio file previously started by playLongAudio
	 * if a filename which is currently playing is given.
	 * 
	 * @param fname The filename of the audio to stop.
	 * @return Whether or not stopping the audio succeeded.
	 */
	public static boolean stopLongAudio(String fname){
		// If no filename is given, don't do anything
		if(fname == null) return false;
		
		// Check to see if the audio is currently playing
		if(currentAudio.containsKey(fname)){
			// If it is, stop the audio
			currentAudio.get(fname).stop();
			// And remove it from the map
			currentAudio.remove(fname);
			
			return true;
		}
		
		return false;
	}
	
	/*
	/**
	 * Initialises an audio file to be played.
	 * This should be used for audio files that are short
	 * and will be played a lot.
	 * It must be called before invoking playAudioClip on
	 * the audio file.
	 * 
	 * @param fname The filename of the audio to load.
	 *
	public static void initAudio(String fname){
		// This is different to using a mediaplayer because it loads
		// the entire sound clip into memory when playing.
		File audioFile = new File(fname);
		try{
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			AudioFormat audioFormat = audioStream.getFormat();
			// I don't really understand what we're doing here,
			// But it has something to do with that Java doesn't
			// natively support mp3 and we're using mp3spi...
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(),16,audioFormat.getChannels(),audioFormat.getChannels()*2,audioFormat.getSampleRate(),false);
			
			DataLine.Info audioInfo = new DataLine.Info(Clip.class, decodedFormat);
			Clip audioClip = (Clip) AudioSystem.getLine(audioInfo);
			
			audioClips.put(fname, audioClip);
			audioStreams.put(fname, audioStream);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Couldn't load from audio file! " + e);
		}
	}
	*/
	
	/**
	 * Allows AudioPlayer to stop audio when necessary
	 */
	class Listener implements LineListener{
		private Clip clip;
		public void setClip(Clip clip){
			this.clip = clip;
		}
		
		@Override
		public void update(LineEvent event){
			LineEvent.Type type = event.getType();
			
			if(type == LineEvent.Type.STOP){
				clip.stop();
				clip.close();
			}
		}
	}
	
	/**
	 * We need a constructor to create our own version of LineListener
	 */
	private AudioPlayer(){}
	
	/**
	 * Plays an audio file that has previously been loaded
	 * by initAudio. Begins at the start of the audio file,
	 * and goes until the end before stopping. This should 
	 * be used for short audio clips.
	 * 
	 * @param fname The filename of the audio to be played.
	 * @return Whether or not the audio clip was played.
	 */
	public static boolean playClip(String fname){
		if(fname == null) return false;
		
		// Unfortunately, we have to reset the clip every time so that we can get an open line.
		// Or something like that, I don't really understand this audio process.
		Clip clip = null;
		AudioInputStream inputStream = null;
		try{
			inputStream = AudioSystem.getAudioInputStream(new File(fname));
			AudioFormat audioFormat = inputStream.getFormat();
			// I don't really understand what we're doing here,
			// But it has something to do with that Java doesn't
			// natively support mp3 and we're using mp3spi...
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(),16,audioFormat.getChannels(),audioFormat.getChannels()*2,audioFormat.getSampleRate(),false);
			
			DataLine.Info audioInfo = new DataLine.Info(Clip.class, decodedFormat);
			clip = (Clip) AudioSystem.getLine(audioInfo);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Could not get input stream for audio clip. " + e);
		}
		
		try{
			// If the clip is already running, we want to stop it and play the new one
			if(clip.isOpen()){
				clip.stop();
				clip.close();
			}
			
			clip.open(inputStream);
			clip.start();
			
			// Listener isn't static so we have to instantiate this
			AudioPlayer player = new AudioPlayer();
			
			// A listener lets us know when the audio has finished playing
			Listener listener = player.new Listener();
			listener.setClip(clip);
			clip.addLineListener(listener);
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
		for(Map.Entry<String, Clip> entry : audioClips.entrySet()){
			entry.getValue().stop();
			entry.getValue().close();
		}
	}
}
