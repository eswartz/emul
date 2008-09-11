package boxpeeking.status.ui;

import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import boxpeeking.status.StatusListener;
import boxpeeking.status.StatusState;

public class JStatusBar extends JComponent
{
	private JLabel label = new JLabel("");
	private JProgressBar progressBar = new JProgressBar();
	private StatusListener listener = new Listener();

	public JStatusBar ()
	{
		setLayout(new BorderLayout());

		label.setPreferredSize(new Dimension(200, 15));
		add(label, BorderLayout.CENTER);

		progressBar.setPreferredSize(new Dimension(50, 15));
		add(progressBar, BorderLayout.EAST);
	}

	public StatusListener getListener ()
	{
		return listener;
	}

	class Listener implements StatusListener
	{
		public void notify (StatusState m)
		{
			label.setText(m.getTopMessage());
		}
	}
}
