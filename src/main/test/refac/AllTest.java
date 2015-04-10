package refac;

import org.junit.Test;

public class AllTest
{

	@Test
	public void test1() throws Exception
	{
		Explorer explorer = new Explorer("utils.json");
		explorer.run();
		explorer.printEntryPoints();

	}

	private void print(Object canonicalName)
	{
		System.out.print(String.valueOf(canonicalName));
	}
}
