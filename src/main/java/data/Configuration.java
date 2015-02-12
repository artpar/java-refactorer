package data;

import java.util.List;

/**
 * author parth.mudgal on 30/01/15.
 */
public class Configuration
{
	List<Module> modules;
	private String[] arguments;

	public List<Module> getModules()
	{
		return modules;
	}

	public void setModules(List<Module> modules)
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
