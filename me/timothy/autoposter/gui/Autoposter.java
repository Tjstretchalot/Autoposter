package me.timothy.autoposter.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Autoposter extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Autoposter ap = new Autoposter();
				ap.begin();
			}
		});
	}
	
	private void begin() {
		setTitle("Autoposter");
		setLocationRelativeTo(null);
		setSize(640, 480);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		add(new AutoposterPanel());
		
		setVisible(true);
	}
}
