package boxpeeking.yourcode.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import boxpeeking.yourcode.YourCode;
import boxpeeking.status.StatusManager;
import boxpeeking.status.ui.JStatusBar;

public class Main
{
	private static JStatusBar statusBar;

	public static void main (String[] args)
	{
		final JButton button = new JButton("Go");

		button.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e)
			{
				Thread t = new Thread(new Runnable() {
					public void run () {
						long start, end;

						button.setEnabled(false);

						start = System.nanoTime();
						YourCode yc = new YourCode();
						end = System.nanoTime();

						System.err.println("class loading took " + ((end - start) / 1E9D) + " sec");

						start = System.nanoTime();
						yc.go();
						end = System.nanoTime();

						System.err.println("execution took " + ((end - start) / 1E9D + " sec"));

						button.setEnabled(true);
					}
				});

				StatusManager.addListener(statusBar.getListener());
				t.start();
			}
		});

  		JPanel panel = new JPanel(new BorderLayout());
		
		statusBar = new JStatusBar();

		JFrame frame = new JFrame("StatusBar Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(button, BorderLayout.NORTH);
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}
}
