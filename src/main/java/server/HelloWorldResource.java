package server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.codahale.metrics.annotation.Timed;
import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.dynamic.TimeFormat;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
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
		return makeGraph(allMap.get(name).getRequiredBy(), allMap.get(name).getRequiredBy());
	}

	@GET
	@Path ("test")
	public String test()
	{
		all.test();
		return "ok";
	}

	@GET
	@Path ("getDependencyList")
	@Produces (MediaType.TEXT_XML)
	public String getDependencyList(@QueryParam ("config") String name) throws Exception
	{
		if (!allMap.containsKey(name))
		{
			start(name);
		}
		return makeGraph(allMap.get(name).getDependencyList(), allMap.get(name).getRequiredBy());

	}

	@GET
	@Path ("getRequiredByList")
	public Response getRequiredByList(@QueryParam ("config") String name) throws Exception
	{
		if (!allMap.containsKey(name))
		{
			start(name);
		}
		return new Response("ok", makeGraph(allMap.get(name).getRequiredBy(), allMap.get(name).getRequiredBy()));
	}

	public String makeGraph(Map<String, List<String>> map1, Map<String, List<String>> map2) throws IOException
	{
		Gexf gexf = new GexfImpl();

		gexf.setVisualization(true);
		// gexf.setVariant()
		Calendar date = Calendar.getInstance();
		date.set(2012, 4, 03);
		// date.setTimeZone(TimeZone.getTimeZone("GMT+0300"));
		gexf.getMetadata().setLastModified(date.getTime()).setCreator("Gephi.org").setDescription("A Web network");

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.DYNAMIC).setTimeType(TimeFormat.XSDDATETIME);

		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);

		// ObservableGraph<String, String> jGraph = new ObservableGraph<String, String>();

		Map<String, Node> keyToNode = new HashMap<String, Node>();
		i = 1;

		for (String key : map1.keySet())
		{
			Node fromNode;
			if (!keyToNode.containsKey(key))
			{
				fromNode = makeNode(graph, key);
				keyToNode.put(key, fromNode);
			}

			fromNode = keyToNode.get(key);
			List<String> toList = map1.get(key);
			for (String toNodeName : toList)
			{
				Node toNode;
				if (!keyToNode.containsKey(toNodeName))
				{
					toNode = makeNode(graph, toNodeName);
					keyToNode.put(toNodeName, toNode);

				}
				toNode = keyToNode.get(toNodeName);
				if (!fromNode.hasEdgeTo(toNode.getId()))
				{

					fromNode.connectTo(UUID.randomUUID().toString(), "depends on", EdgeType.DIRECTED, toNode);
				}
			}
		}

		for (String key : map2.keySet())
		{
			Node fromNode;
			if (!keyToNode.containsKey(key))
			{
				fromNode = makeNode(graph, key);
				keyToNode.put(key, fromNode);
			}

			fromNode = keyToNode.get(key);
			List<String> toList = map1.get(key);
			for (String toNodeName : toList)
			{
				Node toNode;
				if (!keyToNode.containsKey(toNodeName))
				{
					toNode = makeNode(graph, toNodeName);
					keyToNode.put(toNodeName, toNode);

				}
				toNode = keyToNode.get(toNodeName);

				if (!fromNode.hasEdgeTo(toNode.getId()))
				{
					fromNode.connectTo(UUID.randomUUID().toString(), "required by", EdgeType.DIRECTED, toNode);
				}
				else
				{
					for (Edge edge : fromNode.getEdges())
					{
						if (edge.getTarget().equals(toNode))
						{
							edge.setLabel("depends and requires");
							break;
						}
					}
				}
			}
		}

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
