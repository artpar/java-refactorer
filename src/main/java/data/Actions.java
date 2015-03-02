package data;

import processors.AdapterMethodToConstructorController;
import processors.ClassLevelDependencyListController;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public enum Actions
{
	ClassDependencyList(ClassLevelDependencyListController.class), AdapterMethodToConstructor(
	    AdapterMethodToConstructorController.class);
	private Class<? extends Processor> controllerClass;

	Actions(Class<? extends Processor> controllerClass)
	{
		this.controllerClass = controllerClass;
	}

	public Class<? extends Processor> getProcessor()
	{
		return this.controllerClass;
	}
}
