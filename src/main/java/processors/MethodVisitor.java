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
			for (BodyDeclaration bodyDeclaration : typeDeclaration.getMembers())
			{
				if (bodyDeclaration.getClass().equals(MethodDeclaration.class))
				{
					visit((MethodDeclaration) bodyDeclaration);
				}
			}

			// if (typeDeclaration.getClass().equals(ClassOrInterfaceDeclaration.class))
			// {
			// final ClassOrInterfaceDeclaration classOrInterface = (ClassOrInterfaceDeclaration) typeDeclaration;
			// visit(classOrInterface);
			// for (BodyDeclaration bodyDeclaration : classOrInterface.getMembers())
			// {
			// if ()
			// }
			//
			// }
			// else if (typeDeclaration.getClass().equals(EnumDeclaration.class))
			// {
			// visit((EnumDeclaration) typeDeclaration);
			// }
		}

	}

	public Map<String, String> getMethodToReturnType()
	{
		return methodToReturnType;
	}
}
