package server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * author parth.mudgal on 27/02/15.
 */
public class Meta
{

	private String lastmodifieddate = "2009-02-02";
	private String creator = "parth";
	private String description = "graph";

	@XmlAttribute
	public String getLastmodifieddate()
	{
		return lastmodifieddate;
	}

	@XmlElement
	public String getCreator()
	{
		return creator;
	}

	@XmlElement
	public String getDescription()
	{
		return description;
	}
}
