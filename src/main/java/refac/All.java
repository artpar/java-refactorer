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
	private Map<String, List<String>> dependsOn;
	private Map<String, List<String>> requiredBy;
	private boolean classLevelDependencyDone = false;

	public All(String configFile) throws Exception
	{
		this.config = new Configuration(configFile);
		AllRef = this;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final ClassLevelDependencyListController classLevelDependencyListController =
				        new ClassLevelDependencyListController(config);
				classLevelDependencyListController.execute();
				AllRef.requiredBy = classLevelDependencyListController.getRequiredBy();
				AllRef.dependsOn = classLevelDependencyListController.getDependsOn();
				AllRef.classLevelDependencyDone = true;
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
				AllRef.requiredBy = functionLevelDependencyListController.getRequiredBy();
				AllRef.dependsOn = functionLevelDependencyListController.getDependsOn();
				AllRef.classLevelDependencyDone = true;
			}
		}).start();
		return "ok";
	}

	public Map<String, List<String>> getDependencyList()
	{
		if (!classLevelDependencyDone)
		{
			final ClassLevelDependencyListController classLevelDependencyListController =
			        new ClassLevelDependencyListController(config);
			classLevelDependencyListController.execute();
			this.requiredBy = classLevelDependencyListController.getRequiredBy();
			this.dependsOn = classLevelDependencyListController.getDependsOn();
			this.classLevelDependencyDone = true;
		}

		return this.dependsOn;
	}

	public Map<String, List<String>> getRequiredBy()
	{
		if (!classLevelDependencyDone)
		{
			final ClassLevelDependencyListController classLevelDependencyListController =
			        new ClassLevelDependencyListController(config);
			this.requiredBy = classLevelDependencyListController.getRequiredBy();
			this.dependsOn = classLevelDependencyListController.getDependsOn();
			this.classLevelDependencyDone = true;
		}
		return this.requiredBy;
	}

}
