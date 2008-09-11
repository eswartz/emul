package boxpeeking.yourcode;

import boxpeeking.status.Status;

public class YourCode
{
	@Status("Executing action 'Go'")
	public static void go ()
	{
		loadData("IBM");

		for (int i = 0; i < 10000; i++) {
			calculate();
		}

		displayResults();
	}

	@Status("Loading data")
	public static void loadData (String symbol)
	{
		try {
			connectToDB();
			executeQuery();
		} catch (Exception ex) {
			throw new RuntimeException("Could not load data for symbol " + symbol, ex);
		}
	}

	@Status("Performing calculations")
	public static void calculate ()
	{
		for (int i = 0; i < 100; i++) {
			Math.sin(0.5);
		}
	}

	@Status("Displaying results")
	public static void displayResults ()
	{
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {}
	}

	@Status("Connecting to database")
	public static void connectToDB ()
	{
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {}
	}

	@Status("Querying database")
	public static void executeQuery ()
	{
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ex) {}

		for (int i = 0; i < 500; i++) {
			loadResult();
		}
	}

	@Status("Receiving results")
	public static void loadResult ()
	{
		try {
			Thread.sleep(5);
		} catch (InterruptedException ex) {}
	}
}
