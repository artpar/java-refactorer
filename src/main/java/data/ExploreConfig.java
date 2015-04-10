package data;

import java.util.List;

/**
 * author parth.mudgal on 10/04/15.
 */
public class ExploreConfig
{
	String jar;
	List<String> mainClass;
	String exclusionFile;

	public String getExclusionFile()
	{
		return exclusionFile;
	}

	public void setExclusionFile(String exclusionFile)
	{
		this.exclusionFile = exclusionFile;
	}

	public String getJar()
	{
		return jar;
	}

	public void setJar(String jar)
	{
		this.jar = jar;
	}

	public List<String> getMainClass()
	{
		return mainClass;
	}

	public void setMainClass(List<String> mainClass)
	{
		this.mainClass = mainClass;
	}
}
