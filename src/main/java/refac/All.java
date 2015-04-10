package refac;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.io.FileProvider;
import data.Configuration;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.dynamic.TimeFormat;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.NodeImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.ColorImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.PositionImpl;

/**
 * author parth.mudgal on 26/02/15.
 */
public class All
{
	private static final int MAX_LABEL_LENGTH = Integer.MAX_VALUE;
	private static final AttributeImpl ATTR_TYPE = new AttributeImpl(UUID.randomUUID().toString(),
	        AttributeType.STRING, "type");
	private static final AttributeImpl CONTEXT = new AttributeImpl(UUID.randomUUID().toString(), AttributeType.STRING,
	        "context");
	private static final AttributeImpl CLASS_TYPE = new AttributeImpl(UUID.randomUUID().toString(),
	        AttributeType.STRING, "class_type");
	private static final AttributeImpl CLASS = new AttributeImpl(UUID.randomUUID().toString(), AttributeType.STRING,
	        "class");
	public static final String COM_FLIPKART = "Lcom/flipkart";
	private final Configuration config;
	private final All AllRef;
	private Random random = new Random(Calendar.getInstance().getTimeInMillis());

	Map<String, Module> projectList = new HashMap<String, Module>();
	private Map<String, List<String>> classLevelDependsOn;
	private Map<String, List<String>> classLevelRequireBy;
	private boolean classLevelDependencyDone = false;
	private boolean functionLevelDependencyDone = false;
	private Map<String, List<String>> functionLevelRequireBy;
	private Map<String, List<String>> functionLevelDependsOn;
	public CallGraph cg;
	private Map<String, Node> nodeMap = new HashMap<String, Node>();
	private Graph<CGNode> g;

	public All(String configFile) throws Exception
	{
		this.config = new Configuration(configFile);
		AllRef = this;
		init();

	}

	protected void init()
	{
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					final ClassLevelDependencyListController classLevelDependencyListController =
					        new ClassLevelDependencyListController(config);
					classLevelDependencyListController.execute();
					cg = classLevelDependencyListController.getCg();
				}
				catch (ClassHierarchyException e)
				{
					e.printStackTrace();
				}
				catch (CancelException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public Module getModule(String name)
	{
		return projectList.get(name);
	}

	public String test(final String jarName) throws InterruptedException
	{
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					g = buildPrunedCallGraph(jarName, (new FileProvider()).getFile("Java60RegressionExclusions.txt"));
				}
				catch (WalaException e)
				{
					e.printStackTrace();
				}
				catch (CancelException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		thread.start();
		thread.join();
		return "ok";
	}

	public static Graph<CGNode> buildPrunedCallGraph(String appJar, File exclusionFile) throws WalaException,
	        IllegalArgumentException, CancelException, IOException
	{
		AnalysisScope scope =
		        AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, exclusionFile != null ? exclusionFile
		                : new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

		ClassHierarchy cha = ClassHierarchy.make(scope);
		List<Entrypoint> entrypointList = new LinkedList<Entrypoint>();
		for (IClass klass : cha)
		{
			if (klass.getClassLoader().getReference().getName().toString().startsWith("App"))
			{
				for (IMethod iMethod : klass.getDeclaredMethods())
				{
					entrypointList.add(new DefaultEntrypoint(iMethod, iMethod.getClassHierarchy()));
				}
			}
		}

		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = new AnalysisOptions(scope, entrypointList);

		// //
		// build the call graph
		// //
		com.ibm.wala.ipa.callgraph.CallGraphBuilder builder =
		        Util.makeZeroOneCFABuilder(options, new AnalysisCache(), cha, scope);
		CallGraph cg = builder.makeCallGraph(options, null);

		System.err.println(CallGraphStats.getStats(cg));

		Graph<CGNode> g = pruneForAppLoader(cg);

		return g;
	}

	public static Graph<CGNode> pruneForAppLoader(CallGraph g) throws WalaException
	{
		return pruneGraph(g, new ApplicationLoaderFilter());
	}

	public Gexf getFunctionLevelDependsOn()
	{
		try
		{
			return getFunctionLevelDependsOnInternal(g);
		}
		catch (WalaException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * A filter that accepts WALA objects that "belong" to the application loader.
	 * Currently supported WALA types include
	 * <ul>
	 * <li> {@link CGNode}
	 * <li> {@link com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey}
	 * </ul>
	 */
	private static class ApplicationLoaderFilter extends Predicate<CGNode>
	{

		@Override
		public boolean test(CGNode o)
		{
			if (o instanceof CGNode)
			{
				CGNode n = (CGNode) o;
				return n.getMethod().getDeclaringClass().getClassLoader().toString().equals("Application");
			}
			else if (o instanceof LocalPointerKey)
			{
				LocalPointerKey l = (LocalPointerKey) o;
				return test(l.getNode());
			}
			else
			{
				return false;
			}
		}
	}

	public static <T> Graph<T> pruneGraph(Graph<T> g, Predicate<T> f) throws WalaException
	{
		Collection<T> slice = GraphSlicer.slice(g, f);
		return GraphSlicer.prune(g, new CollectionFilter<T>(slice));
	}

	public Map<String, List<String>> getClassLevelDependsOn()
	{
		if (!classLevelDependencyDone)
		{
			init();
		}

		return this.classLevelDependsOn;
	}

	public Map<String, List<String>> getClassLevelRequireBy()
	{
		if (!classLevelDependencyDone)
		{
			init();
		}
		return this.classLevelRequireBy;
	}

	public Map<String, List<String>> getFunctionLevelRequireBy()
	{
		return functionLevelRequireBy;
	}

	public <T> Gexf getFunctionLevelDependsOnInternal(Graph<CGNode> g) throws WalaException
	{
		nodeMap = new HashMap<String, Node>();
		Gexf gexf = new GexfImpl();

		gexf.setVisualization(true);
		// gexf.setVariant()
		Calendar date = Calendar.getInstance();
		date.set(2012, 4, 03);
		// date.setTimeZone(TimeZone.getTimeZone("GMT+0300"));
		gexf.getMetadata().setLastModified(date.getTime()).setCreator("Gephi.org").setDescription("A Web network");

		it.uniroma1.dis.wsngroup.gexf4j.core.Graph graph = gexf.getGraph();

		graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC).setTimeType(TimeFormat.XSDDATETIME);

		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		attrList.add(ATTR_TYPE);
		attrList.add(CONTEXT);
		attrList.add(CLASS_TYPE);
		attrList.add(CLASS);
		graph.getAttributeLists().add(attrList);

		for (Iterator<CGNode> it = g.iterator(); it.hasNext();)
		{
			CGNode n = it.next();
			final Node start = getPort(n, graph);
			if (!start.getLabel().startsWith(COM_FLIPKART))
			{
				continue;
			}
			for (Iterator<CGNode> it2 = g.getSuccNodes(n); it2.hasNext();)
			{
				CGNode next = it2.next();
				final Node endNode = getPort(next, graph);
				if (!endNode.getLabel().startsWith(COM_FLIPKART))
				{
					continue;
				}
				if (!start.hasEdgeTo(endNode.getId()))
				{
					start.connectTo(endNode);
				}
			}
		}
		return gexf;
	}

	public float nextFloatPos()
	{
		return random.nextFloat() * 800;
	}

	private <T> Node getPort(CGNode n, it.uniroma1.dis.wsngroup.gexf4j.core.Graph graph) throws WalaException
	{
		final String label =
		        n.getIR().getMethod().getDeclaringClass().getName() + "." + n.getIR().getMethod().getName().toString();
		if (!label.startsWith(COM_FLIPKART) || n.getMethod().isPrivate() || n.getMethod().isProtected())
		{
			final NodeImpl dummyNode = new NodeImpl("123");
			dummyNode.setLabel("Lnoncom.flipkart");
			return dummyNode;
		}
		if (nodeMap.containsKey(label))
		{
			return nodeMap.get(label);
		}
		final String idStr = UUID.randomUUID().toString();
		Node node = graph.createNode(idStr);
		node.getAttributeValues().addValue(ATTR_TYPE, n.getContext().toString());
		node.getAttributeValues().addValue(CONTEXT, n.getContext().toString());
		node.getAttributeValues().addValue(CLASS_TYPE, n.getMethod().getDeclaringClass().getClassLoader().toString());
		node.getAttributeValues().addValue(CLASS, n.getMethod().getDeclaringClass().getName().toString());
		node.setLabel(label);
		node.setPosition(new PositionImpl(nextFloatPos(), nextFloatPos(), nextFloatPos()));
		node.setColor(new ColorImpl(30, 100, 200));
		node.setSize(50f);
		nodeMap.put(node.getLabel(), node);
		return node;
	}

}
