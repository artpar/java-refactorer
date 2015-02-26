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
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import data.Configuration;
import refac.Module;

/**
 * author parth.mudgal on 30/01/15.
 */
public class FunctionLevel
{

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final Map<String, List<String>> dependsOn = new HashMap<String, List<String>>();
	private static final Map<String, List<String>> requiredBy = new HashMap<String, List<String>>();

	public static void main(String[] args) throws Exception {
		checkArguments(args);
		Configuration config = new Configuration(args[0]);
		for (Module m : config.getModules().values())
		{
			getDependencyMap(m.getPath());
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

		}

	}

	private static class ImportVisitor extends VoidVisitorAdapter
	{
		List<String> imports = new LinkedList<String>();
		private String packageName;
		private String className;
		private Map<String, List<String>> functions = new HashMap<String, List<String>>();
		private LinkedList<String> functionStack = new LinkedList<String>();
		private String currentFunction;

		@Override
		public void visit(CompilationUnit n, Object arg)
		{
			parse(n);
		}

		private void parse(CompilationUnit n)
		{
			packageName = n.getPackage().getName().toString();
			List<TypeDeclaration> types = n.getTypes();
			for (TypeDeclaration type : types)
			{
				visitType(type);
			}

		}

		private void visitType(TypeDeclaration type)
		{
			String typeName = type.getName();
			List<BodyDeclaration> members = type.getMembers();
			if (members == null)
			{
				return;
			}
			for (BodyDeclaration member : members)
			{
				if (member.getClass().equals(FieldDeclaration.class))
				{
					visitFieldDeclaration((FieldDeclaration) member);
				}
				else if (member.getClass().equals(ConstructorDeclaration.class))
				{
					visitConstructorDeclaration(member);
				}
				else if (member.getClass().equals(MethodDeclaration.class))
				{
					visitMethodDeclaration((MethodDeclaration) member);
				}

			}

		}

		private void visitConstructorDeclaration(BodyDeclaration member)
		{

		}

		private void visitMethodDeclaration(MethodDeclaration method)
		{

		}

		private void visitFieldDeclaration(FieldDeclaration field)
		{

		}

	}

	private static void checkArguments(String[] args)
	{
		if (args.length < 1)
		{
			printHelp();
			System.exit(0);
		}
	}

	private static void printError(String s)
	{
		print(s);
		System.exit(1);
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
