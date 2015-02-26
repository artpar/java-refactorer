package server;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * author parth.mudgal on 26/02/15.
 */
public class Node
{
	private String id;
	private String name;
	private Visualization viz = new Visualization(5d, new Position(100F, 100F, 100F), new Color(100, 20, 30));

	@XmlElementWrapper (name = "attvalues")
	public List<AttValue> getAttvalues()
	{
		return attvalues;
	}

	@XmlElement
	public Visualization getViz()
	{
		return viz;
	}

	private List<AttValue> attvalues = new LinkedList<AttValue>()
	{
		{
			add(new AttValue("type", "test"));
		}
	};

	public Node()
	{
	}

	@XmlAttribute (name = "id")
	public String getId()
	{
		return id;

	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Node(String id, String name)
	{
		this.id = id;

		this.name = name;
	}

	@XmlAttribute (name = "label")
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
