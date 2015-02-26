package server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
		return makeGraph(allMap.get(name).getRequiredBy());
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
		return makeGraph(allMap.get(name).getDependencyList());

	}

	@GET
	@Path ("getRequiredByList")
	public Response getRequiredByList(@QueryParam ("config") String name) throws Exception
	{
		if (!allMap.containsKey(name))
		{
			start(name);
		}
		return new Response("ok", makeGraph(allMap.get(name).getRequiredBy()));
	}


	public String makeGraph(Map<String, List<String>> map) throws IOException
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

		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		Map<Node, String> nodeToId = new HashMap<Node, String>();
		Map<String, Node> idToNode = new HashMap<String, Node>();
		Map<String, Node> keyToNode = new HashMap<String, Node>();
		int i = 1;
		String idStr = String.valueOf(i);


		for (String key : map.keySet())
		{
			Node fromNode;
			if (!idToNode.containsKey(key))
			{
				fromNode = graph.createNode(idStr);
				fromNode.setLabel(key);
				// jGraph.addVertex(key);
				idToNode.put(idStr, fromNode);
				nodeToId.put(fromNode, idStr);
				nodes.add(fromNode);
				fromNode.setPosition(new PositionImpl(nextFloatPos(), nextFloatPos(), nextFloatPos()));
				fromNode.setColor(new ColorImpl(30, 100, 200));
				fromNode.setSize(50f);
				keyToNode.put(key, fromNode);
				i++;
				idStr = String.valueOf(i);
			}

			fromNode = keyToNode.get(key);
			List<String> toList = map.get(key);
			for (String toNodeName : toList)
			{
				Node toNode;
				if (!idToNode.containsKey(toNodeName))
				{
					toNode = graph.createNode(idStr);
					toNode.setLabel(toNodeName);
					idToNode.put(idStr, toNode);
					nodeToId.put(toNode, idStr);
					nodes.add(toNode);
					keyToNode.put(toNodeName, toNode);
					toNode.setPosition(new PositionImpl(nextFloatPos(), nextFloatPos(), nextFloatPos()));
					toNode.setColor(new ColorImpl(30, 100, 200));
					toNode.setSize(50f);
					i++;
					idStr = String.valueOf(i);
				}
				toNode = keyToNode.get(toNodeName);
				fromNode.connectTo(toNode);
			}
		}







		StaxGraphWriter graphWriter = new StaxGraphWriter();
		StringWriter stringWriter = new StringWriter();

		graphWriter.writeToStream(gexf, stringWriter, "UTF-8");

		String outputXml = stringWriter.toString();
		return outputXml;
	}

	public float nextFloatPos()
	{
		return random.nextFloat() * 800;
	}
}
