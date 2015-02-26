package processors;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import data.Configuration;
import refac.Module;
import run.Processor;

/**
 * author parth.mudgal on 12/02/15.
 */
public class AdapterMethodToConstructorController extends Processor
{

	private Map<String, String> classNameToPath;

	public AdapterMethodToConstructorController(Configuration config)
	{
		super(config);
	}

	@Override
	public void execute()
	{
		String[] args = config.getArguments();
		if (args.length < 5)
		{
			print("command <config_file> AdapterMethodToConstructor <fromPackageName> <fromQualifiedClassName> <methodName>");
		}
		String moduleName = args[2];
		String className = args[3];
		String methodName = args[4];
		scanClasses();
		if (!classNameToPath.containsKey(moduleName + "." + className))
		{
			printError("Cannot find class [" + className + "] in module [" + moduleName + "]");
		}
		CompilationUnit cu = getCompilationUnit(new File(classNameToPath.get(moduleName + "." + className)));
		for (TypeDeclaration typeDeclaration : cu.getTypes())
		{
			if ((cu.getPackage().getName() + "." + typeDeclaration.getName()).equals(className))
			{
				for (BodyDeclaration bodyDeclaration : typeDeclaration.getMembers())
				{
					if (bodyDeclaration.getClass() == MethodDeclaration.class)
					{
						MethodDeclaration method = (MethodDeclaration) bodyDeclaration;
						if (method.getName().equals(methodName))
						{
							String returnTypeClass = method.getType().toString();
							for (String classNameTemp : classNameToPath.keySet())
							{
								if (classNameTemp.endsWith(returnTypeClass))
								{
									print("Found [" + classNameTemp + "] in: " + classNameToPath.get(classNameTemp)
									        + "\n");
									CompilationUnit destination =
									        getCompilationUnit(new File(classNameToPath.get(classNameTemp)));
									String destinationPackage = destination.getPackage().getName().getName();
									boolean replace = false;
									for (ImportDeclaration importDeclaration : cu.getImports())
									{
										if (importDeclaration.getName().toString()
										        .equals(destination.getPackage().getName() + "." + returnTypeClass))
										{
											replace = true;
											break;
										}
									}
									if (replace)
									{
										ConstructorDeclaration cd = new ConstructorDeclaration();
										final BlockStmt block = new BlockStmt();
										BlockStmt body = method.getBody();
										for (Statement statement : body.getStmts())
										{
											Statement replaceWith = null;
											print(statement.toString());

											if (statement.getClass().equals(ExpressionStmt.class))
											{
												ExpressionStmt exprStatement = (ExpressionStmt) statement;
												Expression expr = exprStatement.getExpression();
												if (expr.getClass().equals(MethodCallExpr.class))
												{
													// replaceWith = new ExpressionStmt(new AssignExpr(new Exp))
												}
											}

											if (statement.getClass().equals(ReturnStmt.class))
											{
												continue;
											}
										}

										cd.setBlock(block);
										break;
									}
									else
									{
										print("Package [" + destinationPackage
										        + "] is not imported by source. Skipping this\n");
									}

								}
							}

							print(returnTypeClass + "\n");
						}
					}
				}
			}
		}
	}

	private void scanClasses()
	{
		classNameToPath = new HashMap<String, String>();
		Collection<Module> modules = config.getModules().values();
		for (Module module : modules)
		{
			Iterator<File> iterator = getJavaFilesIterator(module.getPath());
			while (iterator.hasNext())
			{
				File file = iterator.next();
				ImportVisitor visitor = new ImportVisitor();
				visitCompilationUnit(file, visitor);
				classNameToPath.put(module.getName() + "." + visitor.getPackageName() + "." + visitor.getClassName(),
				        file.getAbsolutePath());
			}
		}

	}
}
