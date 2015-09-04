import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The end screen of a game of MyOsu. One of these should be created
 * after a map has finished, and then it should be initialised with
 * the specified parameters to show the player statistics about the
 * map that they completed.
 * 
 * @author Robert Campbell
 *
 */
public class GameEndScreen extends JPanel {
	private Game game;
	
	private JButton menuButton;
	
	private BufferedImage backgroundImage;
	private BufferedImage menuButtonBackground;
	private BufferedImage[] numberImage;
	private BufferedImage[] scoreImage;
	private BufferedImage accuracyText;
	private BufferedImage percentImage;
	private BufferedImage dotImage;
	private BufferedImage rankingImage;
	
	private int score;
	private double accuracy;
	// 300-100-50-miss
	private int[] scoreCounts;
	private GameMap map;
	
	// x offset is offset from the right of the panel for the last number
	private int scoreXOffset;
	private int scoreYOffset;
	private int accXOffset;
	private int accYOffset;
	
	private int rankXOffset;
	private int rankYOffset;
	
	private int menuXOffset;
	private int menuYOffset;
	
	/**
	 * Initialises the end screen.
	 * @param score The score at the time when the map was finished.
	 * @param accuracy The accuracy at the time when the map was finished.
	 * @param scoreCounts The amount of 300's, 100's, 50's and misses, respectively, that were attained during the map.
	 * @param map The map that was just played.
	 */
	public void init(int score, double accuracy, int[] scoreCounts, GameMap map, Game game){
		this.game = game;
		this.score = score;
		this.accuracy = accuracy;
		this.scoreCounts = scoreCounts;
		this.map = map;
		
		MouseListener l = new MouseListener(){
			public void mousePressed(MouseEvent e){
				doMouse(e);
			}
			public void mouseClicked(MouseEvent e){}
			public void mouseReleased(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseExited(MouseEvent e){}
		};
		addMouseListener(l);
		
		scoreXOffset = getPreferredSize().width/2;
		scoreYOffset = getPreferredSize().height/2 - 75;
		accXOffset = getPreferredSize().width/2;
		accYOffset = getPreferredSize().height/2 + 75;
		
		rankXOffset = getPreferredSize().width/2 + 30;
		rankYOffset = getPreferredSize().height/2 - 75;
		
		menuXOffset = accXOffset - 200;
		menuYOffset = scoreYOffset - 200;
		
		backgroundImage = map.getBackground();
		
		try{
			menuButtonBackground = ImageIO.read(new File(Options.SKIN_RANKING_BACK_BUTTON));
			numberImage = new BufferedImage[10];
			for(int i = 0; i < 10; i++){
				numberImage[i] = ImageIO.read(new File(Options.SKIN_NUMBER_BASE + i + Options.SKIN_NUMBER_END));
			}
			
			scoreImage = new BufferedImage[4];
			scoreImage[0] = ImageIO.read(new File(Options.SKIN_300_HIT));
			scoreImage[1] = ImageIO.read(new File(Options.SKIN_100_HIT));
			scoreImage[2] = ImageIO.read(new File(Options.SKIN_50_HIT));
			scoreImage[3] = ImageIO.read(new File(Options.SKIN_0_HIT));
			
			accuracyText = ImageIO.read(new File(Options.SKIN_TEXT_ACCURACY));
			percentImage = ImageIO.read(new File(Options.SKIN_PERCENT));
			dotImage = ImageIO.read(new File(Options.SKIN_DOT));
			
			// The proportion of 300's against the total amount of hits
			// Used to calculate the rank
			double proportion300 = (double)scoreCounts[0] / (scoreCounts[3] + scoreCounts[2] + scoreCounts[1] + scoreCounts[0]);
			
			// SS only if every hit was a 300
			if(proportion300 == 1)
				rankingImage = ImageIO.read(new File(Options.SKIN_SS_RANK));
			// S if >90% were 300's and none were missed
			else if(proportion300 > 0.9 && scoreCounts[3] == 0)
				rankingImage = ImageIO.read(new File(Options.SKIN_S_RANK));
			// A if >90% were 300's or if >80% were 300's and none were missed
			else if(proportion300 > 0.9 || (proportion300 > 0.8 && scoreCounts[3] == 0))
				rankingImage = ImageIO.read(new File(Options.SKIN_A_RANK));
			// B if >80% were 300's or if >70% were 300's and none were missed
			else if(proportion300 > 0.8 || (proportion300 > 0.7 && scoreCounts[3] == 0))
				rankingImage = ImageIO.read(new File(Options.SKIN_B_RANK));
			// C if >65% were 300's
			else if(proportion300 > 0.65)
				rankingImage = ImageIO.read(new File(Options.SKIN_C_RANK));
			// D otherwise
			else
				rankingImage = ImageIO.read(new File(Options.SKIN_D_RANK));
		}
		catch(IOException e){
			System.out.println("Couldn't read from image file while initialising end screen. " + e);
		}
		
		setVisible(true);
	}
	
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		if(backgroundImage == null){
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}
		else
			g2d.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
		
		// Draw the score
		// We do this similarly to in GameDraw, where it is an annoying method
		String strScore = Integer.toString(score);
		
		int currentX = this.getWidth()-scoreXOffset;
		for(int i = strScore.length()-1; i >= 0; i--){
			BufferedImage img = numberImage[Integer.parseInt(((Character)(strScore.charAt(i))).toString())];
			g2d.drawImage(img, currentX-img.getWidth()*2, scoreYOffset, currentX, scoreYOffset+img.getHeight()*2, 0, 0, img.getWidth(), img.getHeight(), this);
			currentX -= img.getWidth()*2+5;
		}
		
		// Draw the accuracy
		// Again, done similarly to in GameDraw
		String strAcc = String.format("%.2f",accuracy);
		currentX = this.getWidth()-accXOffset;

		// Draw a percent sign at the end
		g2d.drawImage(percentImage, currentX-percentImage.getWidth()/2, accYOffset, currentX, accYOffset+percentImage.getHeight()/2, 0, 0, percentImage.getWidth(), percentImage.getHeight(), this);
		currentX -= percentImage.getWidth()/2 + 5;
		for(int i = strAcc.length()-1; i >= 0; i--){
			BufferedImage img;
			// If it's a dot, obviously we can't parse it as an int
			if(strAcc.charAt(i) == '.') img = dotImage;
			else img = numberImage[Integer.parseInt(((Character)(strAcc.charAt(i))).toString())];

			g2d.drawImage(img, currentX-img.getWidth(), accYOffset, currentX, accYOffset+img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), this);
			currentX -= img.getWidth() + 5;
		}
		
		g2d.drawImage(accuracyText, currentX - 150, accYOffset, currentX - 15, accYOffset + numberImage[0].getHeight(), 0, 0, accuracyText.getWidth(), accuracyText.getHeight(), this);
		
		// Draw the ranking
		g2d.drawImage(rankingImage, rankXOffset, rankYOffset, rankXOffset+250, rankYOffset+250*(rankingImage.getHeight()/rankingImage.getWidth()), 0, 0, rankingImage.getWidth(), rankingImage.getHeight(), this);
		
		g2d.drawImage(menuButtonBackground, menuXOffset, menuYOffset, menuXOffset + menuButtonBackground.getWidth(), menuYOffset + menuButtonBackground.getHeight(), 0, 0, menuButtonBackground.getWidth(), menuButtonBackground.getHeight(), this);
	}
	
	/**
	 * Responds to a mouse event, checking whether it was pressed on the back button or not.
	 */
	private void doMouse(MouseEvent e){
		// Is there a better way of doing this? Not sure. Feels clunky.
		if(e.getX() > menuXOffset &&
		   e.getX() < menuXOffset + menuButtonBackground.getWidth() &&
		   e.getY() > menuYOffset &&
		   e.getY() < menuYOffset + menuButtonBackground.getHeight())
			game.terminate();
	}
}
