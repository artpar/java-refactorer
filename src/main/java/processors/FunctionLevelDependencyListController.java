package processors;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.Configuration;
import refac.Module;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public class FunctionLevelDependencyListController extends Processor
{
	private final Map<String, List<String>> dependsOn = new HashMap<String, List<String>>();
	private final Map<String, List<String>> requiredBy = new HashMap<String, List<String>>();
	private HashMap<String, String> methodMap;
	private LinkedList<String> classesExplored;

	public FunctionLevelDependencyListController(Configuration config)
	{
		super(config);
	}

	@Override
	public void execute()
	{
		for (Module m : config.getModules().values())
		{
			getMethodReturnTypeMap(m.getPath());
			getDependencyMap(m.getPath());
		}
		// removeUseless();

		printDependencies(dependsOn);
	}

	public Map<String, List<String>> getDependsOn()
	{
		return dependsOn;
	}

	public Map<String, List<String>> getRequiredBy()
	{
		return requiredBy;
	}

	private void printDependencies(Map<String, List<String>> dependsOn)
	{
		for (Map.Entry<String, List<String>> stringListEntry : dependsOn.entrySet())
		{
			for (String s : stringListEntry.getValue())
			{
				print(stringListEntry.getKey() + " " + s + "\n");
			}

		}
	}

	private boolean removeUseless()
	{
		List<String> toRemove = new LinkedList<String>();
		toRemove.add("null");
		for (Map.Entry<String, List<String>> stringListEntry : requiredBy.entrySet())
		{
			String key = stringListEntry.getKey();
			boolean found = false;
			for (String s : classesExplored)
			{
				if (key.startsWith(s))
				{
					found = true;
				}
			}
			if (!found)
			{
				toRemove.add(key);
			}
		}

		for (String s : toRemove)
		{
			requiredBy.remove(s);
		}

		for (Map.Entry<String, List<String>> stringListEntry : dependsOn.entrySet())
		{
			stringListEntry.getValue().removeAll(toRemove);

		}
		return true;
	}

	private void getDependencyMap(String modulePath)
	{
		Iterator<File> files = getJavaFilesIterator(modulePath);
		classesExplored = new LinkedList<String>();
		while (files.hasNext())
		{
			File file = files.next();
			final BodyVisitor visitor = new BodyVisitor(methodMap);
			visitCompilationUnit(file, visitor);

			dependsOn.putAll(visitor.getDependsMap());
			final Collection<? extends String> allClasses = visitor.getAllClasses();
			classesExplored.addAll(allClasses);
		}

		Set<String> parents = dependsOn.keySet();

		for (String parent : parents)
		{
			List<String> requires = dependsOn.get(parent);
			for (String require : requires)
			{
				if (requiredBy.get(require) == null)
				{
					requiredBy.put(require, new LinkedList<String>());
				}
				requiredBy.get(require).add(parent);
			}

		}
		removeUseless();
	}

	private void getMethodReturnTypeMap(String modulePath)
	{
		methodMap = new HashMap<String, String>();
		Iterator<File> files = getJavaFilesIterator(modulePath);
		while (files.hasNext())
		{
			File file = files.next();
			final MethodVisitor visitor = new MethodVisitor();
			visitCompilationUnit(file, visitor);

			methodMap.putAll(visitor.getMethodToReturnType());

		}
	}

}
