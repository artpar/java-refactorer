package processors;

import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * author parth.mudgal on 12/02/15.
 */
public class ImportVisitor extends VoidVisitorAdapter
{
	List<String> imports = new LinkedList<String>();
	private String packageName;
	private String className;

	@Override
	public void visit(PackageDeclaration n, Object arg)
	{
		packageName = n.getName().toString();
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, Object arg)
	{
		if (className == null)
		{
			className = n.getName();
		}
	}

	@Override
	public void visit(EnumDeclaration n, Object arg)
	{
		if (className == null)
		{
			className = n.getName();
		}
	}

	@Override
	public void visit(ImportDeclaration n, Object arg)
	{
		// print(n.getName().toString());
		imports.add(n.getName().toString());
	}

	public List<String> getImports()
	{
		return imports;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public String getClassName()
	{
		return className;
	}
}
