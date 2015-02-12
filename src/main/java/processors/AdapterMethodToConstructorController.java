package processors;

import data.Configuration;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public class AdapterMethodToConstructorController extends Processor
{

	public AdapterMethodToConstructorController(Configuration config)
	{
		super(config);
	}

	@Override
	public void execute()
	{
		String[] args = config.getArguments();
		if (args.length < 4)
		{
			print("command <config_file> AdapterMethodToConstructor <fromPackageName> <fromQualifiedClassName> <methodName>");
		}
	}
}
