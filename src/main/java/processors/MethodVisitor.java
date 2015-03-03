package processors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * author parth.mudgal on 12/02/15.
 */
public class MethodVisitor extends Visitor
{
	List<String> imports = new LinkedList<String>();
	private String packageName;
	private String className;
	private Map<String, String> methodToReturnType = new HashMap<String, String>();
	private List<String> classes = new LinkedList<String>();

	public void visit(PackageDeclaration n)
	{
		packageName = n.getName().toString();
	}

	public void visit(MethodDeclaration n)
	{
		methodToReturnType.put(packageName + "." + className + "." + n.getName(), n.getType().toString());
	}

	public void visit(EnumDeclaration n)
	{
		className = n.getName();
	}

	public void visit(ClassOrInterfaceDeclaration n)
	{
		className = n.getName();
	}

	@Override
	public void visit(CompilationUnit cu, Object arg)
	{
		packageName = cu.getPackage().getName().toString();
		for (TypeDeclaration typeDeclaration : cu.getTypes())
		{
			className = typeDeclaration.getName();
			classes.add(packageName + "." + className);
			if (typeDeclaration.getMembers() == null) {
				continue;
			}
			for (BodyDeclaration bodyDeclaration : typeDeclaration.getMembers())
			{
				if (bodyDeclaration.getClass().equals(MethodDeclaration.class))
				{
					visit((MethodDeclaration) bodyDeclaration);
				}
			}
		}

	}

	public Map<String, String> getMethodToReturnType()
	{
		return methodToReturnType;
	}

	public List<String> getClasses() {
		return classes;
	}
}
