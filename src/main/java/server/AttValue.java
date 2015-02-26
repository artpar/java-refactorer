package server;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * author parth.mudgal on 27/02/15.
 */
public class AttValue
{
	private String forName;
	private String value;

	public AttValue()
	{
	}

	public AttValue(String forName, String value)
	{

		this.forName = forName;
		this.value = value;
	}

	@XmlAttribute (name = "for")
	public String getForName()
	{
		return forName;
	}

	@XmlAttribute (name = "value")
	public String getValue()
	{
		return value;
	}
}
