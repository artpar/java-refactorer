package refac;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Configuration;
import processors.ClassLevelDependencyListController;
import processors.FunctionLevelDependencyListController;

/**
 * author parth.mudgal on 26/02/15.
 */
public class All
{
	private final Configuration config;
	private final All AllRef;
	Map<String, Module> projectList = new HashMap<String, Module>();
	private Map<String, List<String>> classLevelDependsOn;
	private Map<String, List<String>> classLevelRequireBy;
	private boolean classLevelDependencyDone = false;
	private boolean functionLevelDependencyDone = false;
	private Map<String, List<String>> functionLevelRequireBy;
	private Map<String, List<String>> functionLevelDependsOn;

	public All(String configFile) throws Exception
	{
		this.config = new Configuration(configFile);
		AllRef = this;
		init();

	}

	protected void init()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final ClassLevelDependencyListController classLevelDependencyListController =
				        new ClassLevelDependencyListController(config);
				classLevelDependencyListController.execute();
				AllRef.classLevelRequireBy = classLevelDependencyListController.getRequiredBy();
				AllRef.classLevelDependsOn = classLevelDependencyListController.getDependsOn();
				AllRef.classLevelDependencyDone = true;
			}
		}).start();

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final FunctionLevelDependencyListController functionLevelDependencyListController =
				        new FunctionLevelDependencyListController(config);
				functionLevelDependencyListController.execute();
				AllRef.functionLevelRequireBy = functionLevelDependencyListController.getRequiredBy();
				AllRef.functionLevelDependsOn = functionLevelDependencyListController.getDependsOn();
				AllRef.functionLevelDependencyDone = true;
			}
		}).start();
	}

	public Module getModule(String name)
	{
		return projectList.get(name);
	}

	public String test()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final FunctionLevelDependencyListController functionLevelDependencyListController =
				        new FunctionLevelDependencyListController(config);
				functionLevelDependencyListController.execute();
				AllRef.functionLevelRequireBy = functionLevelDependencyListController.getRequiredBy();
				AllRef.functionLevelDependsOn = functionLevelDependencyListController.getDependsOn();
				AllRef.functionLevelDependencyDone = true;
			}
		}).start();
		return "ok";
	}

	public Map<String, List<String>> getClassLevelDependsOn()
	{
		if (!classLevelDependencyDone)
		{
			init();
		}

		return this.classLevelDependsOn;
	}

	public Map<String, List<String>> getClassLevelRequireBy()
	{
		if (!classLevelDependencyDone)
		{
			init();
		}
		return this.classLevelRequireBy;
	}

	public Map<String, List<String>> getFunctionLevelRequireBy()
	{
		return functionLevelRequireBy;
	}

	public Map<String, List<String>> getFunctionLevelDependsOn()
	{
		return functionLevelDependsOn;
	}
}
