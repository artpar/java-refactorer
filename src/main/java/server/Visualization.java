package server;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * author parth.mudgal on 27/02/15.
 */
public class Visualization
{
	private Double size = 50d;
	private Position position;
	private Color color;

	@XmlAttribute
	public Double getSize()
	{
		return size;
	}

	public Visualization()
	{
	}

	public Visualization(Double size, Position position, Color color)
	{

		this.size = size;
		this.position = position;
		this.color = color;
	}

	public Position getPosition()
	{

		return position;
	}

	public Color getColor()
	{
		return color;
	}
}
