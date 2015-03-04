package server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.codahale.metrics.annotation.Timed;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.ColorImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.PositionImpl;
import refac.All;

@Path ("/ref")
@Produces (MediaType.APPLICATION_JSON)
public class HelloWorldResource
{
	private final AtomicLong counter;
	private final Map<String, All> allMap = new HashMap<String, All>();
	private final All all;
	private String configFile;
	private Random random = new Random(Calendar.getInstance().getTimeInMillis());
	private int i = 1;

	public HelloWorldResource(String configFile) throws Exception
	{
		this.configFile = configFile;
		this.all = new All(configFile);
		this.counter = new AtomicLong();
	}

	@GET
	@Timed
	public String start(@QueryParam ("config") String name) throws Exception
	{
		if (!allMap.containsKey(name))
		{
			allMap.put(name, this.all);
		}
		return makeGraph(this.all.getFunctionLevelDependsOn());
	}

	@GET
	@Path ("test")
	public String test(@QueryParam ("jar") String jarName) throws Exception
	{
		All tempAll;
		if (!allMap.containsKey(jarName))
		{
			tempAll = new All(configFile);
			allMap.put(jarName, tempAll);
		}
		else
		{
			tempAll = allMap.get(jarName);
		}
		tempAll.test(jarName);
		return "ok";
	}

	@GET
	@Path ("getDependencyList")
	@Produces (MediaType.TEXT_XML)
	public String getDependencyList(@QueryParam ("config") String name, @QueryParam ("type") String type)
	        throws Exception
	{
		if (!allMap.containsKey(name))
		{
			start(name);
		}

		final All allInstance = allMap.get(name);
		if (type.equals("class"))
		{
			return makeGraph(allInstance.getFunctionLevelDependsOn());
		}
		else if (type.equals("function"))
		{
			return makeGraph(allInstance.getFunctionLevelDependsOn());
		}
		else
		{
			return makeGraph(allInstance.getFunctionLevelDependsOn());
		}

	}

	public String makeGraph(Gexf gexf) throws IOException
	{

		StaxGraphWriter graphWriter = new StaxGraphWriter();
		StringWriter stringWriter = new StringWriter();

		graphWriter.writeToStream(gexf, stringWriter, "UTF-8");

		String outputXml = stringWriter.toString();
		return outputXml;
	}

	public Node makeNode(Graph graph, String key)
	{
		String idStr = String.valueOf(i);
		Node fromNode;
		fromNode = graph.createNode(idStr);
		fromNode.setLabel(key);
		fromNode.setPosition(new PositionImpl(nextFloatPos(), nextFloatPos(), nextFloatPos()));
		fromNode.setColor(new ColorImpl(30, 100, 200));
		fromNode.setSize(50f);
		i = i + 1;

		return fromNode;
	}

	public float nextFloatPos()
	{
		return random.nextFloat() * 800;
	}
}
