package refac;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Configuration;
import processors.ClassDependencyListController;

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
				final ClassDependencyListController classDependencyListController =
				        new ClassDependencyListController(config);
				classDependencyListController.execute();
				AllRef.requiredBy = classDependencyListController.getRequiredBy();
				AllRef.dependsOn = classDependencyListController.getDependsOn();
				AllRef.classLevelDependencyDone = true;
			}
		}).start();
	}

	public Module getModule(String name)
	{
		return projectList.get(name);
	}

	public Map<String, List<String>> getDependencyList()
	{
		if (!classLevelDependencyDone)
		{
			final ClassDependencyListController classDependencyListController =
			        new ClassDependencyListController(config);
			classDependencyListController.execute();
			this.requiredBy = classDependencyListController.getRequiredBy();
			this.dependsOn = classDependencyListController.getDependsOn();
			this.classLevelDependencyDone = true;
		}

		return this.dependsOn;
	}

	public Map<String, List<String>> getRequiredBy()
	{
		if (!classLevelDependencyDone)
		{
			final ClassDependencyListController classDependencyListController =
			        new ClassDependencyListController(config);
			this.requiredBy = classDependencyListController.getRequiredBy();
			this.dependsOn = classDependencyListController.getDependsOn();
			this.classLevelDependencyDone = true;
		}
		return this.requiredBy;
	}

}
