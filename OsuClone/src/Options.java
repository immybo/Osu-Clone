import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Provides public, static fields which represent all MyOsu options.
 * 
 * @author Robert Campbell
 *
 */
public class Options {
	public static boolean MENU_FULLSCREEN;

	public static int MAIN_WINDOW_DEFAULT_WIDTH;
	public static int MAIN_WINDOW_DEFAULT_HEIGHT;

	public static int MAIN_WINDOW_INITIAL_X;
	public static int MAIN_WINDOW_INITIAL_Y;

	public static boolean MAIN_WINDOW_RESIZABLE;

	public static int OPTION_WINDOW_DEFAULT_WIDTH;
	public static int OPTION_WINDOW_DEFAULT_HEIGHT;

	public static int OPTION_WINDOW_INITIAL_X;
	public static int OPTION_WINDOW_INITIAL_Y;

	public static boolean OPTION_WINDOW_RESIZABLE;

	public static int GAME_WINDOW_DEFAULT_HEIGHT;
	public static int GAME_WINDOW_DEFAULT_WIDTH;
	public static int GAME_WINDOW_INITIAL_X;
	public static int GAME_WINDOW_INITIAL_Y;
	public static boolean GAME_WINDOW_RESIZABLE;
	
	public static int END_SCREEN_WIDTH = 900;
	public static int END_SCREEN_HEIGHT = 600;
	public static int END_WINDOW_INITIAL_X = 200;
	public static int END_WINDOW_INITIAL_Y = 200;
	public static boolean END_WINDOW_RESIZABLE = false;

	public static int GAME_TICK_TIME = 20;
	public static int GAME_CIRCLE_SIZE = 100;
	
	// Multipliers for the 3 attributes when Hard Rock is active.
	// Note that this actually multiplies the typically 1-10 value, not the proper value.
	public static double HRODMod = 1.5;
	public static double HRARMod = 1.5;
	public static double HRCSMod = 1.5;
	public static double HRHealthMod = 1;

	public static char GAME_KEY_1;
	public static char GAME_KEY_2;

	public final static String MAP_FOLDER = "./maps";
	public final static String PROGRAM_OPTION_FILE = "programOptions.txt";
	public final static String USER_OPTION_FILE = "userOptions.txt";
	public final static String DEFAULT_BG = "defaultbg.jpg";
	
	// Skin graphics
	public final static String SKIN_HIT_0 = "skin/hit0.png";
	public final static String SKIN_HIT_50 = "skin/hit50.png";
	public final static String SKIN_HIT_100 = "skin/hit100.png";
	public final static String SKIN_HIT_300 = "skin/hit300.png";

	public final static String SKIN_HIT_CIRCLE = "skin/hitcircleoverlay-0.png";
	public final static String SKIN_HIT_CIRCLE_OUTER = "skin/hitcircleselect.png";
	public final static String SKIN_APPROACH_CIRCLE = "skin/approachcircle.png";
	public final static String SKIN_HIT_CIRCLE_BORDER = "skin/hitcircle.png";
	
	public final static String SKIN_SKIP_BUTTON_IMAGE = "skin/play-skip.png";
	
	public final static String SKIN_PAUSE_MENU_BACKGROUND = "skin/pausebackground.png";
	
	public final static String SKIN_NUMBER_BASE = "skin/score-";
	public final static String SKIN_NUMBER_END = ".png";
	public final static String SKIN_DOT = "skin/score-dot.png";
	public final static String SKIN_PERCENT = "skin/score-percent.png";
	
	public final static String SKIN_HEALTHBAR_3 = "skin/scorebar-ki.png";
	public final static String SKIN_HEALTHBAR_2 = "skin/scorebar-kidanger.png";
	public final static String SKIN_HEALTHBAR_1 = "skin/scorebar-kidanger2.png";
	
	public final static String SKIN_SLIDER_FOLLOW_CIRCLE = "skin/sliderfollowcircle.png";
	
	public final static String SKIN_SS_RANK = "skin/ranking-X.png";
	public final static String SKIN_S_RANK = "skin/ranking-S.png";
	public final static String SKIN_A_RANK = "skin/ranking-A.png";
	public final static String SKIN_B_RANK = "skin/ranking-B.png";
	public final static String SKIN_C_RANK = "skin/ranking-C.png";
	public final static String SKIN_D_RANK = "skin/ranking-D.png";
	
	public final static String SKIN_300_HIT = "skin/hit300.png";
	public final static String SKIN_100_HIT = "skin/hit100.png";
	public final static String SKIN_50_HIT = "skin/hit50.png";
	public final static String SKIN_0_HIT = "skin/hit0.png";
	
	public final static String SKIN_RANKING_BACK_BUTTON = "skin/ranking-back.png";
	public final static String SKIN_TEXT_ACCURACY = "skin/ranking-accuracy.png";
	public final static String SKIN_TEXT_RANKING = "skin/ranking-title.png";
	public final static String SKIN_RANKING_PANEL = "skin/ranking-panel.png";
	
	// Skin sounds
	public final static String SKIN_COMBO_BREAK_SOUND = "skin/combobreak.mp3";
	public final static String SKIN_FAIL_SOUND = "skin/failsound.mp3";
	public final static String SKIN_SUCCEED_SOUND = "skin/applause.mp3";
	public final static String SKIN_CIRCLE_HIT_SOUND = "skin/soft-hitnormal.wav";
	
	
	// Maps options that don't need to be read by the user
	private static Map<String, Integer> numOptions;
	private static Map<String, Boolean> boolOptions;
	private static Map<String, String> stringOptions;
	
	// Maps of user option names to their respective values; one for each type of option
	private static Map<String, Integer> numUserOptions;
	private static Map<String, Boolean> boolUserOptions;
	private static Map<String, String> stringUserOptions;
	// Option names that are readable by the user
	private static Map<String, String> readableOptionNames;
	
	/**
	 * Returns a map of option names to numerical options
	 */
	public static Map<String, Integer> getNumOptions(){
		return numOptions;
	}
	/**
	 * Returns a map of option names to boolean options
	 */
	public static Map<String, Boolean> getBoolOptions(){
		return boolOptions;
	}
	/**
	 * Returns a map of option names to string options
	 */
	public static Map<String, String> getStringOptions(){
		return stringOptions;
	}
	/**
	 * Returns a map of option names to numerical user options
	 */
	public static Map<String, Integer> getNumUserOptions(){
		return numUserOptions;
	}
	/**
	 * Returns a map of option names to boolean user options
	 */
	public static Map<String, Boolean> getBoolUserOptions(){
		return boolUserOptions;
	}
	/**
	 * Returns a map of option names to string user options
	 */
	public static Map<String, String> getStringUserOptions(){
		return stringUserOptions;
	}
	/**
	 * Returns a map of option names to user-readable option names
	 */
	public static Map<String, String> getReadableOptionNames(){
		return readableOptionNames;
	}
	
	/**
	 * Reads and initialises all options
	 */
	public static void init(){
		getOptions(PROGRAM_OPTION_FILE, false);
		getOptions(USER_OPTION_FILE, true);
		setOptionParams();
	}
	
	/**
	 * Sets all parameters based options in the existing maps;
	 * getOptions should be called before this to initialise
	 * the maps.
	 */
	private static void setOptionParams(){
		MENU_FULLSCREEN = boolOptions.get("MENU_FULLSCREEN");

		MAIN_WINDOW_DEFAULT_WIDTH = numOptions.get("MAIN_WINDOW_DEFAULT_WIDTH");
		MAIN_WINDOW_DEFAULT_HEIGHT = numOptions.get("MAIN_WINDOW_DEFAULT_HEIGHT");
		MAIN_WINDOW_INITIAL_X = numOptions.get("MAIN_WINDOW_INITIAL_X");
		MAIN_WINDOW_INITIAL_Y = numOptions.get("MAIN_WINDOW_INITIAL_Y");
		MAIN_WINDOW_RESIZABLE = boolOptions.get("MAIN_WINDOW_RESIZABLE");

		OPTION_WINDOW_DEFAULT_WIDTH = numOptions.get("OPTION_WINDOW_DEFAULT_WIDTH");
		OPTION_WINDOW_DEFAULT_HEIGHT = numOptions.get("OPTION_WINDOW_DEFAULT_HEIGHT");
		OPTION_WINDOW_INITIAL_X = numOptions.get("OPTION_WINDOW_INITIAL_X");
		OPTION_WINDOW_INITIAL_Y = numOptions.get("OPTION_WINDOW_INITIAL_Y");
		OPTION_WINDOW_RESIZABLE = boolOptions.get("OPTION_WINDOW_RESIZABLE");

		GAME_WINDOW_DEFAULT_WIDTH = numOptions.get("GAME_WINDOW_DEFAULT_WIDTH");
		GAME_WINDOW_DEFAULT_HEIGHT = numOptions.get("GAME_WINDOW_DEFAULT_HEIGHT");
		GAME_WINDOW_INITIAL_X = numOptions.get("GAME_WINDOW_INITIAL_X");
		GAME_WINDOW_INITIAL_Y = numOptions.get("GAME_WINDOW_INITIAL_Y");
		GAME_WINDOW_RESIZABLE = boolOptions.get("GAME_WINDOW_RESIZABLE");

		// For safety, rather than casting to char, just get the first character
		GAME_KEY_1 = stringOptions.get("GAME_KEY_1").charAt(0);
		GAME_KEY_2 = stringOptions.get("GAME_KEY_2").charAt(0);
	}
	
	/**
	 * Finds and reads all options from a file. Returns a map of these as objects,
	 * but also appends to the num(User)Options, bool(User)Options and char(User)Options maps.
	 *
	 * @param filename The file to read from. Throws an exception if it can't read from this file.
	 * @param isUser Whether the options being read are user options; determines which maps to write to.
	 * @return A map of options found from the file (String -> Object).
	 */
	private static Map<String, Object> getOptions(String filename, boolean isUser){

		// The map to return; specifically a HashMap
		Map<String, Object> options = new HashMap<String, Object>();

		// Initialise (or recreate) the maps to store all options in (as we don't want to try and put duplicates in)
		Map<String, Integer> targetNumOptions = new HashMap<String, Integer>();
		Map<String, Boolean> targetBoolOptions = new HashMap<String, Boolean>();
		Map<String, String> targetStringOptions = new HashMap<String, String>();

		// If we're reading user options, we also need to map the names to readable names
		readableOptionNames = new HashMap<String, String>();

		// Attempt to create a scanner
		try{
			// Create a new scanner on the given file
			Scanner s = new Scanner(new File(filename));

			// The user options use a different delimiter
			if(isUser)
				s.useDelimiter(";");

			// While the scanner still has entries,
			while(s.hasNext()){
				// Take the next string as the option descriptor
				String descriptor = s.next();
				// If there's nothing after the semicolon, there's no next element
				if(!s.hasNext())
					break;
				// And the one after that as the readable name if applicable
				if(isUser){
					String readableName = s.next();
					readableOptionNames.put(descriptor, readableName);
				}
				// And check to see whether the option is boolean or numerical or a string, and append it appropriately
				Object value;
				if(s.hasNextInt()){
					int tempValue = s.nextInt();
					targetNumOptions.put(descriptor, tempValue);
					value = tempValue;
				}
				else if(s.hasNextBoolean()){
					boolean tempValue = s.nextBoolean();
					targetBoolOptions.put(descriptor, tempValue);
					value = tempValue;
				}
				// If it's a string, put it as a string
				else{
					String tempValue = s.next();
					targetStringOptions.put(descriptor, tempValue);
					value = tempValue;
				}

				options.put(descriptor, value);
			}

			s.close();
		}
		// Throw an exception if it can't read from the file
		catch(IOException e){
			System.err.println("Could not read from options file '"+filename+"'. Is this file not present? Details: " + e);
		}

		// Depending on if it was for user options or not, change the actual maps
		if(isUser){
			numUserOptions = targetNumOptions;
			boolUserOptions = targetBoolOptions;
			stringUserOptions = targetStringOptions;
		}
		else{
			numOptions = targetNumOptions;
			boolOptions = targetBoolOptions;
			stringOptions = targetStringOptions;
		}

		return options;
	}
}
