package boxpeeking.yourcode.ui;

import boxpeeking.yourcode.YourCode;
import boxpeeking.status.StatusManager;
import boxpeeking.status.ui.JStatusBar;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

public class Main
{
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
				t.start();
			}
		});

		JStatusBar statusBar = new JStatusBar();
		StatusManager.addListener(statusBar.getListener());

		JFrame frame = new JFrame("StatusBar Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(button, BorderLayout.CENTER);
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}
}




