package data;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import refac.Module;

/**
 * author parth.mudgal on 30/01/15.
 */
public class Configuration
{
	private static final ObjectMapper objectMapper = new ObjectMapper();

	Map<String, Module> modules;
	private String[] arguments;

	public Configuration()
	{
	}

	public Configuration(String configFilePath) throws Exception
	{
		try
		{
			Configuration config =
			        objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(configFilePath),
			                Configuration.class);
			if (config.getModules() == null)
			{
				throw new Exception("List of module directories is not present");
			}
			this.modules = config.getModules();
			this.arguments = config.getArguments();
		}
		catch (IOException e)
		{
			throw new Exception("Failed to read the config file: " + configFilePath, e);
		}
	}

	public Map<String, Module> getModules()
	{
		return modules;
	}

	public void setModules(Map<String, Module> modules)
	{
		this.modules = modules;
	}

	public void setArguments(String[] arguments)
	{
		this.arguments = arguments;
	}

	public String[] getArguments()
	{
		return arguments;
	}
}
