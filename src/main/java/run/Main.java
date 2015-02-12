package run;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.Actions;
import data.Configuration;

/**
 * author parth.mudgal on 30/01/15.
 */
public class Main extends Processor
{

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final String[] args;

	public Main(String[] args)
	{
		this.args = args;
	}

	public static void main(String[] args)
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

	private Configuration readConfigFile(String configFilePath)
	{
		try
		{
			Configuration config = objectMapper.readValue(new File(configFilePath), Configuration.class);
			if (config.getModules() == null)
			{
				printError("List of module directories is not present");
			}
			return config;
		}
		catch (IOException e)
		{
			printError("Failed to read the config file: " + configFilePath, e);
			System.exit(1);
		}
		return new Configuration();
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
	public void execute()
	{

		new Main(args);
		checkArguments(args);
		Configuration config = readConfigFile(args[0]);
		config.setArguments(args);
		String actionString = args[1];
		Actions action = Actions.valueOf(actionString);
		try
		{
			action.getProcessor().getDeclaredConstructor(Configuration.class).newInstance(config).execute();
		}
		catch (Throwable e)
		{
			printError("Failed to instantiate class", e);
		}

	}

}
