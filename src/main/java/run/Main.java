package run;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import data.Configuration;

/**
 * author parth.mudgal on 30/01/15.
 */
public class Main
{

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final Map<String, List<String>> dependsOn = new HashMap<String, List<String>>();
	private static final Map<String, List<String>> requiredBy = new HashMap<String, List<String>>();

	public static void main(String[] args)
	{
		checkArguments(args);
		Configuration config = readConfigFile(args[0]);
		for (String m : config.getModulePath())
		{
			getDependencyMap(m);
		}
		removeUseless();

		printDependencies(dependsOn);
		// print("Finished");
	}

	private static void printDependencies(Map<String, List<String>> dependsOn)
	{
		for (Map.Entry<String, List<String>> stringListEntry : dependsOn.entrySet())
		{
			for (String s : stringListEntry.getValue())
			{
				print(stringListEntry.getKey() + " " + s);
			}

		}
	}

	private static boolean removeUseless()
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

	private static void getDependencyMap(String modulePath)
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

	private static class ImportVisitor extends VoidVisitorAdapter
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

	private static void checkArguments(String[] args)
	{
		if (args.length < 1)
		{
			printHelp();
		}
	}

	private static void printError(String s)
	{
		print(s);
		System.exit(1);
	}

	private static Configuration readConfigFile(String arg)
	{
		try
		{
			Configuration config = objectMapper.readValue(new File(arg), Configuration.class);
			if (config.getModulePath() == null)
			{
				printError("List of module directories is not present");
			}
			return config;
		}
		catch (IOException e)
		{
			print("Failed to read the config file: " + arg);
			System.exit(1);
		}
		return new Configuration();
	}

	private static void printHelp()
	{
		print("command <config_file>");
	}

	private static void print(String message)
	{
		System.out.println(message);
	}
}
