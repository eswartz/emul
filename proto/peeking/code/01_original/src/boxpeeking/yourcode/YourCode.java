package boxpeeking.yourcode;

import boxpeeking.status.StatusManager;

public class YourCode
{
	public static void go ()
	{
		StatusManager.push("Executing action 'Go'");
		try {
			loadData();

			for (int i = 0; i < 10000; i++) {
				calculate();
			}

			displayResults();
		} finally {
			StatusManager.pop();
		}
	}

	public static void loadData ()
	{
		StatusManager.push("Loading data");
		try {
			connectToDB();
			executeQuery();
		} finally {
			StatusManager.pop();
		}
	}

	public static void calculate ()
	{
		StatusManager.push("Performing calculations");
		try {
			for (int i = 0; i < 100; i++) {
				Math.sin(0.5);
			}
		} finally {
			StatusManager.pop();
		}
	}

	public static void displayResults ()
	{
		StatusManager.push("Displaying results");
		try {
			try {
				Thread.sleep(500);
			} catch (InterruptedException ex) {}
		} finally {
			StatusManager.pop();
		}
	}

	public static void connectToDB ()
	{
		StatusManager.push("Connecting to database");
		try {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ex) {}
		} finally {
			StatusManager.pop();
		}
	}

	public static void executeQuery ()
	{
		StatusManager.push("Querying database");
		try {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {}

			for (int i = 0; i < 500; i++) {
				loadResult();
			}
		} finally {
			StatusManager.pop();
		}
	}

	public static void loadResult ()
	{
		StatusManager.push("Receiving results");
		try {
			try {
				Thread.sleep(5);
			} catch (InterruptedException ex) {}
		} finally {
			StatusManager.pop();
		}
	}
}
