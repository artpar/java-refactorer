package refac;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ArrayQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.Atom;
import data.ExploreConfig;

/**
 * author parth.mudgal on 10/04/15.
 */
public class Explorer
{
	private String configFile;
	ObjectMapper objectMapper = new ObjectMapper();
	private final ExploreConfig config;
	private List<Entrypoint> entryPointList;
	private Logger logger = Logger.getLogger(Explorer.class);
	private CallGraph cg;

	public Explorer(String configFile) throws IOException
	{
		this.configFile = configFile;
		config =
		        objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream(this.configFile),
		                ExploreConfig.class);
		List<String> newMainClass = new LinkedList<String>();
		for (String s : config.getMainClass())
		{
			newMainClass.add(s.replaceAll("\\.", "/"));
		}
		config.setMainClass(newMainClass);

	}

	public void run() throws IOException, ClassHierarchyException, CallGraphBuilderCancelException
	{
		AnalysisScope scope =
		        AnalysisScopeReader.makeJavaBinaryAnalysisScope(config.getJar(), config.getExclusionFile() != null
		                ? new File(config.getExclusionFile()) : new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

		ClassHierarchy cha = ClassHierarchy.make(scope);
		entryPointList = new LinkedList<Entrypoint>();

		for (IClass klass : cha)
		{
			final String className = klass.getClassLoader().getReference().getName().toString();
			if (!className.equals("Application"))
			{
				continue;
			}

			boolean doAdd = false;
			for (String mainClass : config.getMainClass())
			{
				if (klass.getName().toString().endsWith(mainClass))
				{
					doAdd = true;
					break;
				}
			}
			if (doAdd)
			{
				for (IMethod iMethod : klass.getDeclaredMethods())
				{
					if (iMethod.getName().toString().equals("main"))
					{
						entryPointList.add(new DefaultEntrypoint(iMethod, iMethod.getClassHierarchy()));
					}
				}
			}
		}
		AnalysisOptions options = new AnalysisOptions(scope, entryPointList);
		CallGraphBuilder builder = Util.makeZeroOneCFABuilder(options, new AnalysisCache(), cha, scope);
		cg = builder.makeCallGraph(options, null);

	}

	public void printEntryPoints() throws InvalidClassFileException
	{
		CGNode mainMethod = findMainMethod(cg);
		Queue<CGNode> cgNodeQueue = new ArrayQueue<CGNode>(50, 50);
		Map<CGNode, Boolean> done = new HashMap<CGNode, Boolean>();
		cgNodeQueue.add(mainMethod);
		while (cgNodeQueue.size() > 0)
		{
			CGNode cgNode = cgNodeQueue.remove();
			if (done.containsKey(cgNode))
			{
				continue;
			}
			done.put(cgNode, true);
			final Iterator<CGNode> succNodes = cg.getSuccNodes(cgNode);
			while (succNodes.hasNext())
			{
				CGNode n = succNodes.next();
				if (!done.containsKey(n))
				{
					if ("Application".equals(n.getMethod().getDeclaringClass().getClassLoader().getReference()
					        .getName().toString()))
					{
						cgNodeQueue.add(n);
					}
				}
			}
			logger.info("\nNode - " + cgNode);
			// logger.info(cgNode.getIR().getControlFlowGraph());
			final IR ir = cgNode.getIR();
			logger.info("\n" + ir + "\n\n");
			final SymbolTable symbolTable = ir.getSymbolTable();
			int numParams = symbolTable.getNumberOfParameters();
			int maxVariable = symbolTable.getMaxValueNumber();
			for (int i = 0; i < maxVariable; i++)
			{
				if (symbolTable.isConstant(i))
				{
					logger.info("Constant [" + i + "] - " + symbolTable.getValueString(i));
				}
			}
			logger.info("");
			for (ISSABasicBlock block : ir.getControlFlowGraph())
			{
				for (SSAInstruction instruction : block)
				{

					ir.getBasicBlockForInstruction(instruction);
					if (instruction.iindex > -1)
					{
						NormalStatement statement = new NormalStatement(cgNode, instruction.iindex);
						logger.info(getLineNumber(statement) + " - " + instruction);
					}
					else
					{
						logger.info("\t - " + instruction);
					}
				}
			}
		}
	}

	public int getLineNumber(Statement s)
	{
		if (s.getKind() == Statement.Kind.NORMAL)
		{ // ignore special kinds of statements
			int bcIndex, instructionIndex = ((NormalStatement) s).getInstructionIndex();
			try
			{
				bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
				try
				{
					int src_line_number = s.getNode().getMethod().getLineNumber(bcIndex);
					return src_line_number;
					// System.err.println("Source line number = " + src_line_number);
				}
				catch (Exception e)
				{
					System.err.println("Bytecode index no good");
					System.err.println(e.getMessage());
				}
			}
			catch (Exception e)
			{
				System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
				System.err.println(e.getMessage());
			}
		}
		return -1;
	}

	public static CGNode findMainMethod(CallGraph cg)
	{
		Descriptor d = Descriptor.findOrCreateUTF8("([Ljava/lang/String;)V");
		Atom name = Atom.findOrCreateUnicodeAtom("main");
		for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();)
		{
			CGNode n = it.next();
			if (n.getMethod().getName().equals(name) && n.getMethod().getDescriptor().equals(d))
			{
				return n;
			}
		}
		Assertions.UNREACHABLE("failed to find main() method");
		return null;
	}
}
