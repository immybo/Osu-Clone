import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * A frame containing an editable list of options;
 * - Reads options from its GameMenu instance
 * - Displays these options to the user
 * - Saves the options back to the file after close
 * 
 * @author Robert Campbell
 *
 */
public class OptionMenu extends JFrame{
	private Map<String, JRadioButton> optionComponentBoolean;
	private Map<String, JTextField> optionComponentString;
	
	private JScrollPane optionScrollPane;
	
	private JButton okButton;
	private JButton cancelButton;
	
	/**
	 * Initialises the option menu
	 */
	public void init(){
		optionComponentBoolean = new HashMap<String, JRadioButton>();
		optionComponentString = new HashMap<String, JTextField>();

		setSize(Options.OPTION_WINDOW_DEFAULT_WIDTH, Options.OPTION_WINDOW_DEFAULT_HEIGHT);
		setLocation(Options.OPTION_WINDOW_INITIAL_X, Options.OPTION_WINDOW_INITIAL_Y);
		setResizable(Options.OPTION_WINDOW_RESIZABLE);
		setLayout(new BorderLayout());
		// Set what to do on window close
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				saveUserOptions();
				dispose();
			}
		});

		JPanel optionPanel = new JPanel();
		GridLayout optionPanelLayout = new GridLayout(0, 1);
		optionPanelLayout.setVgap(0);
		optionPanel.setLayout(optionPanelLayout);
		add(optionPanel);
		
		optionScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		optionScrollPane.setPreferredSize(getPreferredSize());
		optionScrollPane.setViewportView(optionPanel);
		add(optionScrollPane, BorderLayout.CENTER);

		// Create components for:
		// Each boolean option
		for(Map.Entry<String, Boolean> entry : Options.getBoolUserOptions().entrySet()){
			String key = entry.getKey();
			boolean value = entry.getValue();

			JRadioButton newOption = new JRadioButton(Options.getReadableOptionNames().get(key));
			newOption.setSelected(value);
			newOption.setVisible(true);

			optionComponentBoolean.put(key, newOption);
			optionPanel.add(newOption);
		}
		
		// Each string option
		for(Map.Entry<String, String> entry : Options.getStringUserOptions().entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			
			JLabel newOptionLabel = new JLabel(Options.getReadableOptionNames().get(key));
			JTextField newOption = new JTextField(value);
			
			newOptionLabel.setPreferredSize(new Dimension(300,50));
			newOption.setPreferredSize(new Dimension(300,50));
			
			newOptionLabel.setVisible(true);
			newOption.setVisible(true);
			
			optionComponentString.put(key, newOption);
			
			optionPanel.add(newOptionLabel);
			optionPanel.add(newOption);
		}
		
		// Add buttons at the bottom for clicking OK or cancel
		
		ActionListener l = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				doButtons(e);
			}
		};
		
		// A new panel so we can place them next to each other
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		add(buttonPanel, BorderLayout.SOUTH);
		
		okButton = new JButton("OK");
		okButton.setPreferredSize(new Dimension(300,50));
		
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(300,50));
		
		okButton.addActionListener(l);
		cancelButton.addActionListener(l);
		
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		optionScrollPane.setVisible(true);
		setVisible(true);
	}
	
	/**
	 * Responds to the OK and cancel buttons
	 */
	private void doButtons(ActionEvent e){
		if(e.getSource().equals(okButton)){
			saveUserOptions();
			dispose();
		}
		else if(e.getSource().equals(cancelButton)){
			dispose();
		}
	}
	
	/**
	 * Saves all user options back to the file
	 */
	private void saveUserOptions(){
		// Make a new printer to write to the user options file
		try{
			PrintStream userStream = new PrintStream(new File(Options.USER_OPTION_FILE));
			// Go through every component and record their values along with string to the file
			for(Map.Entry<String, Boolean> entry : Options.getBoolUserOptions().entrySet()){
				// Format: NAME;readable name;value; - 1 line per option
				userStream.print(entry.getKey() + ";" + Options.getReadableOptionNames().get(entry.getKey()) + ";" + optionComponentBoolean.get(entry.getKey()).isSelected() + ";");
			}
			for(Map.Entry<String, String> entry : Options.getStringUserOptions().entrySet()){
				userStream.print(entry.getKey() + ";" + Options.getReadableOptionNames().get(entry.getKey()) + ";" + optionComponentString.get(entry.getKey()).getText() + ";");
			}
			userStream.close();
		}
		catch(IOException e){
			System.err.println("Could not save back to user option file! " + e);
		}
	}
}
