package data;

import processors.AdapterMethodToConstructorController;
import processors.ClassDependencyListController;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public enum Actions
{
	ClassDependencyList(ClassDependencyListController.class), AdapterMethodToConstructor(
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
