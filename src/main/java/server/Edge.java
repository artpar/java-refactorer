package server;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * author parth.mudgal on 26/02/15.
 */

public class Edge
{
	private static int counter = 1;
	private Integer id;
	private String from;
	private String to;

	public Edge(String from, String to)
	{
		id = counter++;
		this.from = from;
		this.to = to;
	}

	@XmlAttribute (name = "source")
	public String getFrom()
	{
		return from;
	}

	@XmlAttribute (name = "id")
	public Integer getId() {
		return id;
	}

	public Edge()
	{
		id = counter++;

	}

	@XmlAttribute (name = "target")
	public String getTo()
	{
		return to;
	}

}
