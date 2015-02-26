package server;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * author parth.mudgal on 26/02/15.
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class Graph
{
	@XmlElementWrapper(name="nodes")
	@XmlElement(name="node")
	List<Node> nodes;

	@XmlElementWrapper (name = "edges")
	@XmlElement(name="edge")
	List<Edge> edges;

	@XmlAttribute
	private String mode = "static";
	@XmlAttribute
	private String defaultedgetype = "directed";

	public String getMode()
	{
		return mode;
	}

	public String getDefaultedgetype()
	{
		return defaultedgetype;
	}

	public Graph(List<Node> nodes, List<Edge> edges)
	{
		this.nodes = nodes;
		this.edges = edges;
	}

	public Graph()
	{
	}

	public List<Node> getNodes()
	{
		return nodes;
	}

	public List<Edge> getEdges()
	{
		return edges;
	}
}
