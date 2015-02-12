package processors;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import data.Configuration;
import data.Module;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public class ClassDependencyListController extends Processor
{
	private final Map<String, List<String>> dependsOn = new HashMap<String, List<String>>();
	private final Map<String, List<String>> requiredBy = new HashMap<String, List<String>>();

	public ClassDependencyListController(Configuration config)
	{
		super(config);
	}

	@Override
	public void execute()
	{
		for (Module m : config.getModules())
		{
			getDependencyMap(m.getPath());
		}
		removeUseless();

		printDependencies(dependsOn);
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
		if (modulePath.charAt(modulePath.length() - 1) != '/')
		{
			modulePath = modulePath + "/src/";
		}
		else
		{
			modulePath = modulePath + "src/";
		}
		Iterator<File> files = FileUtils.iterateFiles(new File(modulePath), new String[]{"java"}, true);
		while (files.hasNext())
		{
			File file = files.next();
			CompilationUnit cu = null;
			try
			{
				cu = JavaParser.parse(file);
			}
			catch (ParseException e)
			{
				printError("Failed to parse the file: " + file.getAbsolutePath());
			}
			catch (IOException e)
			{
				printError("Failed to parse the file: " + file.getAbsolutePath());
			}
			final ImportVisitor importVisitor = new ImportVisitor();
			importVisitor.visit(cu, null);

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

	private class ImportVisitor extends VoidVisitorAdapter
	{
		List<String> imports = new LinkedList<String>();
		private String packageName;
		private String className;

		@Override
		public void visit(PackageDeclaration n, Object arg)
		{
			packageName = n.getName().toString();
		}

		@Override
		public void visit(ClassOrInterfaceDeclaration n, Object arg)
		{
			if (className == null)
			{
				className = n.getName();
			}
		}

		@Override
		public void visit(EnumDeclaration n, Object arg)
		{
			if (className == null)
			{
				className = n.getName();
			}
		}

		@Override
		public void visit(ImportDeclaration n, Object arg)
		{
			// print(n.getName().toString());
			imports.add(n.getName().toString());
		}

		public List<String> getImports()
		{
			return imports;
		}

		public String getPackageName()
		{
			return packageName;
		}

		public String getClassName()
		{
			return className;
		}
	}
}
