package server;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * author parth.mudgal on 27/02/15.
 */
public class Position
{
	private Float x;
	private Float y;
	private Float z;

	public Position()
	{
	}

	public Position(Float x, Float y, Float z)
	{

		this.x = x;
		this.y = y;
		this.z = z;
	}

	@XmlAttribute
	public Float getZ()
	{
		return z;
	}

	@XmlAttribute
	public Float getY()
	{
		return y;
	}

	@XmlAttribute
	public Float getX()
	{
		return x;
	}

}
