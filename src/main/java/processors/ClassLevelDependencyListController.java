package processors;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.Configuration;
import refac.Module;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public class ClassLevelDependencyListController extends Processor
{
	private final Map<String, List<String>> dependsOn = new HashMap<String, List<String>>();
	private final Map<String, List<String>> requiredBy = new HashMap<String, List<String>>();

	public ClassLevelDependencyListController(Configuration config)
	{
		super(config);
	}

	@Override
	public void execute()
	{
		for (Module m : config.getModules().values())
		{
			getDependencyMap(m.getPath());
		}
		removeUseless();

		print("done print class level");
//		printDependencies(dependsOn);
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
		for (Map.Entry<String, List<String>> stringListEntry : requiredBy.entrySet())
		{
			if (!dependsOn.containsKey(stringListEntry.getKey()))
			{
				toRemove.add(stringListEntry.getKey());
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
		while (files.hasNext())
		{
			File file = files.next();
			final ImportVisitor importVisitor = new ImportVisitor();
			visitCompilationUnit(file, importVisitor);

			if (importVisitor.getClassName() == null)
			{
				continue;
			}
			dependsOn.put(importVisitor.getPackageName() + "." + importVisitor.getClassName(),
			        importVisitor.getImports());
			List<String> imports = importVisitor.getImports();
			for (String anImport : imports)
			{
				if (requiredBy.get(anImport) == null)
				{
					requiredBy.put(anImport, new LinkedList<String>());
				}
				requiredBy.get(anImport).add(importVisitor.getPackageName() + "." + importVisitor.getClassName());
			}

		}

	}

}
