package run;

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

	public abstract void execute();

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
		print(e.getMessage());
		print(e.getStackTrace());
	}
}
