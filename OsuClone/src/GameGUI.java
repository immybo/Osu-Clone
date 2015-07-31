import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * Handles the GUI of the game
 * @author campberobe1
 *
 */
public class GameGUI extends JPanel{
	private int score = 0;
	private int health = 100;


	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;

		// Clear current graphics
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,this.getWidth(),this.getHeight());

		// Draw the score
		g2d.setColor(Color.BLACK);
		g2d.drawString("Score: " + score, 400, 30);
		// Draw the health
		g2d.setColor(new Color((int)(2.5*health),0,0));
		g2d.fillRect(110, 10, health*2, 50);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(110, 10, health*2, 50);
		g2d.drawString("Health: ", 30, 30);

		// Draw a line below the gui
		g2d.setColor(Color.BLACK);
		g2d.drawLine(0,this.getHeight()-1,this.getWidth(),this.getHeight()-1);
	}

	/**
	 * Sets the score to draw
	 */
	public void setScore(int score){
		this.score = score;
	}

	/**
	 * Sets the health to draw
	 */
	public void setHealth(int health){
		this.health = health;
	}
}
