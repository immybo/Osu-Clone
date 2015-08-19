import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A pause menu for the game of MyOsu; is merely a panel that should
 * be added to the game frame. Automatically calls doUnpause() on the
 * game that called it if it should be resumed, as well as resetMap()
 * if it should be reset or terminate() if it should exit.
 * 
 * @author Robert Campbell
 *
 */
public class GamePauseMenu extends JPanel {
	private Game game;
	private JButton resumeButton;
	private JButton restartButton;
	private JButton exitButton;
	
	/**
	 * Initialises the pause menu.
	 * @param game The instance of game to affect when buttons are pressed.
	 */
	public void init(Game game){
		setPreferredSize(new Dimension(300,300));
		
		this.game = game;

		ActionListener l = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				doAction(e);
			}
		};
		
		resumeButton = new JButton("Resume Map");
		restartButton = new JButton("Restart Map");
		exitButton = new JButton("Exit to Menu");
		
		resumeButton.addActionListener(l);
		restartButton.addActionListener(l);
		exitButton.addActionListener(l);
		
		resumeButton.setLocation(300,300);
		resumeButton.setPreferredSize(new Dimension(100,200));
		
		add(resumeButton);
		add(restartButton);
		add(exitButton);
		
		setVisible(true);
	}
	
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.BLACK);
		g2d.drawRect(100, 100, 1000, 1000);
	}
	
	/**
	 * Responds to a button event.
	 */
	private void doAction(ActionEvent e){
		// Check if it's on one of the buttons (do nothing if not)
		if(e.getSource().equals(resumeButton))
			game.doUnpause();
		else if(e.getSource().equals(restartButton)){
			game.doUnpause();
			game.restart();
		}
		else if(e.getSource().equals(exitButton)){
			game.doUnpause();
			game.terminate();
		}
	}
}
