package server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.codahale.metrics.annotation.Timed;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.dynamic.TimeFormat;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
import refac.All;

@Path ("/ref")
@Produces (MediaType.APPLICATION_JSON)
public class HelloWorldResource
{
	private final AtomicLong counter;
	private final Map<String, All> allMap = new HashMap<String, All>();
	private final All all;
	private String configFile;

	public HelloWorldResource(String configFile) throws Exception
	{
		this.configFile = configFile;
		this.all = new All(configFile);
		this.counter = new AtomicLong();
	}

	@GET
	@Timed
	public Response start(@QueryParam ("config") String name) throws Exception
	{
		if (!allMap.containsKey(name))
		{
			allMap.put(name, this.all);
		}
		return new Response("ok", mapToGraph(allMap.get(name).getRequiredBy()));
	}

	@GET
	@Path ("getDependencyList")
	@Produces (MediaType.APPLICATION_XML)
	public Response getDependencyList(@QueryParam("config") String name) throws Exception
	{
		if (!allMap.containsKey(name))
		{
			start(name);
		}
		return new Response("ok", makeGraph(allMap.get(name).getDependencyList()));
	}

	@GET
	@Path ("getRequiredByList")
	public Response getRequiredByList(@QueryParam("config") String name) throws Exception
	{
		if (!allMap.containsKey(name))
		{
			start(name);
		}
		return new Response("ok", makeGraph(allMap.get(name).getRequiredBy()));
	}

//	public Graph mapToGraph(Map<String, List<String>> map)
//	{
//		List<Node> nodes = new ArrayList<Node>();
//		List<Edge> edges = new ArrayList<Edge>();
//		Map<String, String> nodeToId = new HashMap<String, String>();
//		Map<String, String> idToNode = new HashMap<String, String>();
//		int i = 1;
//		String idStr = String.valueOf(i);
//		for (String key : map.keySet())
//		{
//			if (!idToNode.containsKey(key))
//			{
//				idToNode.put(idStr, key);
//				nodeToId.put(key, idStr);
//				nodes.add(new Node(idStr, key));
//				i++;
//				idStr = String.valueOf(i);
//			}
//
//			String fromId = nodeToId.get(key);
//			List<String> toList = map.get(key);
//			for (String toNode : toList)
//			{
//				if (!idToNode.containsKey(toNode))
//				{
//					idToNode.put(idStr, toNode);
//					nodeToId.put(toNode, idStr);
//					nodes.add(new Node(idStr, toNode));
//					i++;
//					idStr = String.valueOf(i);
//				}
//				String toNodeId = nodeToId.get(toNode);
//				Edge newEdge = new Edge(fromId, toNodeId);
//				edges.add(newEdge);
//			}
//		}
//		return new Graph(nodes, edges);
//	}

	public String makeGraph(Map<String, List<String>> requiredBy)
	{
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();
		date.set(2012, 4, 03);
//        date.setTimeZone(TimeZone.getTimeZone("GMT+0300"));
		gexf.getMetadata()
				.setLastModified(date.getTime())
				.setCreator("Gephi.org")
				.setDescription("A Web network");


		Graph graph = gexf.getGraph();
		graph.
				setDefaultEdgeType(EdgeType.UNDIRECTED)
				.setMode(Mode.DYNAMIC)
				.setTimeType(TimeFormat.XSDDATETIME);

		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);
	}
}
