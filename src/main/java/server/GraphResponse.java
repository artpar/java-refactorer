package server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * author parth.mudgal on 26/02/15.
 */
@XmlRootElement (name = "gexf", namespace = "http://www.gexf.net/1.2draft")
public class GraphResponse
{
	String message;

	private Graph data;


	@XmlElement
	public Meta getMeta()
	{
		return meta;
	}

	private Meta meta = new Meta();

	@XmlAttribute
	public String getVersion()
	{
		return version;
	}

	private String version = "1.2";

	@XmlElement (name = "graph")
	public Graph getData()
	{
		return data;
	}

	public void setData(Graph data)
	{
		this.data = data;
	}

	public GraphResponse()
	{
	}

	public GraphResponse(String message, Graph data)
	{
		this.message = message;
		this.data = data;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
