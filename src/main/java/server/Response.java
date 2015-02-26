package server;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * author parth.mudgal on 26/02/15.
 */
public class Response
{
	String message;
	private Object data;

	@JsonProperty
	public Object getData()
	{
		return data;
	}

	public void setData(Object data)
	{
		this.data = data;
	}

	public Response()
	{
	}

	public Response(String message, Object data)
	{
		this.message = message;
		this.data = data;
	}

	@JsonProperty
	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
