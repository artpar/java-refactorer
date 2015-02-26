package run;

import data.Actions;
import data.Configuration;

/**
 * author parth.mudgal on 30/01/15.
 */
public class Main extends Processor
{

	private final String[] args;

	public Main(String[] args)
	{
		this.args = args;
	}

	public static void main(String[] args) throws Exception
	{

		new Main(args).execute();
	}

	private void checkArguments(String[] args)
	{
		if (args.length < 2)
		{
			printHelp();
			System.exit(0);
		}
	}

	private void printHelp()
	{
		print("command <config_file> <action>\n" + "actions:\n");
		for (Actions action : Actions.values())
		{
			print("\t" + action.toString() + "\n");
		}

	}

	@Override
	public void execute() throws Exception
	{

		new Main(args);
		checkArguments(args);
		Configuration config = new Configuration(args[0]);
		config.setArguments(args);
		String actionString = args[1];
		Actions action = Actions.valueOf(actionString);
		try
		{
			action.getProcessor().getDeclaredConstructor(Configuration.class).newInstance(config).execute();
		}
		catch (Throwable e)
		{
			printError("Exception	", e);
		}

	}

}
