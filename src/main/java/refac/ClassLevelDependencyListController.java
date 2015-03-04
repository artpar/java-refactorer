package refac;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import data.Configuration;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public class ClassLevelDependencyListController extends Processor
{

	public static final String RECURSE_MAIN = "Lcom/flipkart/payment/service/servlets/FkpgSystemInitializer";
	private CallGraph cg;

	public ClassLevelDependencyListController(Configuration config)
	{
		super(config);
	}

	public CallGraph getCg()
	{
		return cg;
	}

	@Override
	public void execute() throws ClassHierarchyException, CancelException, IOException
	{
		cg = makeCg();
		print("done parsing");
	}

	private CallGraph makeCg() throws IOException, ClassHierarchyException, CancelException
	{
		AnalysisScope scope =
		        AnalysisScopeReader.readJavaScope("wala.testdata.txt", (new FileProvider())
		                .getFile("Java60RegressionExclusions.txt"), this.getClass().getClassLoader());
		ClassHierarchy cha = ClassHierarchy.make(scope);

		// Iterable<Entrypoint> entrypoints =
		// com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha, RECURSE_MAIN);
		List<Entrypoint> entrypointList = new LinkedList<Entrypoint>();
		for (IClass klass : cha)
		{
			for (IMethod iMethod : klass.getAllMethods())
			{
				entrypointList.add(new DefaultEntrypoint(iMethod, iMethod.getClassHierarchy()));
			}
		}

		AnalysisOptions options = new AnalysisOptions(scope, entrypointList);
		return CallGraphTestUtil.buildRTA(options, new AnalysisCache(), cha, scope);
	}
}
