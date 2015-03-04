package run;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import data.Configuration;

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


	}


}
