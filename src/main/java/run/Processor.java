package run;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import data.Configuration;
import processors.Visitor;

/**
 * author parth.mudgal on 12/02/15.
 */
public abstract class Processor
{
	protected Configuration config;

	public Processor(Configuration config)
	{
		this.config = config;
	}

	protected Processor()
	{
	}

	public abstract void execute() throws Exception;

	public void print(Object message)
	{
		System.out.print(message);
	}

	public void printError(String s)
	{
		print(s);
		System.exit(1);
	}

	protected void printError(String s, Throwable e)
	{
		print(s);
		print(e.getMessage() + "\n");
		e.printStackTrace();
	}

	protected Iterator<File> getJavaFilesIterator(String modulePath)
	{
		if (modulePath.charAt(modulePath.length() - 1) != '/')
		{
			modulePath = modulePath + "/src/main/";
		}
		else
		{
			modulePath = modulePath + "src/main/";
		}
		return FileUtils.iterateFiles(new File(modulePath), new String[]{"java"}, true);

		// return FileUtils.iterateFiles(new File(modulePath), new IOFileFilter()
		// {
		// @Override
		// public boolean accept(File file)
		// {
		// return file.getName().endsWith(".java") || file.getName().endsWith(".java");
		// }
		//
		// @Override
		// public boolean accept(File dir, String name)
		// {
		// return name.endsWith(".java") || name.endsWith(".java");
		// }
		// }, new IOFileFilter()
		// {
		// @Override
		// public boolean accept(File file)
		// {
		// return true;
		// }
		//
		// @Override
		// public boolean accept(File dir, String name)
		// {
		// return true;
		// }
		// });

	}

	protected void visitCompilationUnit(File file, Visitor importVisitor)
	{
		CompilationUnit cu = getCompilationUnit(file);
		importVisitor.visit(cu, null);
	}

	protected CompilationUnit getCompilationUnit(File file)
	{
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
		return cu;
	}
}
