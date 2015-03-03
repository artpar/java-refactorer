package processors;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

/**
 * author parth.mudgal on 12/02/15.
 */
public class BodyVisitor extends Visitor
{
	List<String> imports = new LinkedList<String>();
	private String packageName;
	private String className;
	private Map<String, String> classNameToPackageName = new HashMap<String, String>();
	private Map<String, String> variableTypeMap = new HashMap<String, String>();
	private Map<String, List<String>> dependsOn = new HashMap<String, List<String>>();
	private Map<String, String> requiredBy = new HashMap<String, String>();
	private String thisClassName;
	private HashMap<String, String> methodReturnTypeMap;
	private Collection<String> classesList;

	public BodyVisitor(HashMap<String, String> methodReturnTypeMap)
	{

		this.methodReturnTypeMap = methodReturnTypeMap;
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

	public void visit(ImportDeclaration n)
	{
		// print(n.getName().toString());
		final String name = n.getName().toString();
		imports.add(name);
		final String[] parts = name.split("\\.");
		classNameToPackageName.put(parts[parts.length - 1], name.substring(0, name.lastIndexOf(".".codePointAt(0))));
	}

	@Override
	public void visit(CompilationUnit cu, Object arg)
	{
		classesList = new LinkedList<String>();
		visitPackage(cu.getPackage());
		final List<ImportDeclaration> imports1 = cu.getImports();
		if (imports1 != null)
		{
			for (ImportDeclaration importDeclaration : imports1)
			{
				visit(importDeclaration);
			}
		}

		List<TypeDeclaration> types = cu.getTypes();

		// Types are Class/Interface/Enum
		for (TypeDeclaration type : types)
		{
			addType(type);
		}
	}

	private void visitPackage(PackageDeclaration aPackage)
	{
		this.packageName = aPackage.getName().toString();
	}

	private void addType(TypeDeclaration type)
	{
		String typeName = type.getName();
		thisClassName = packageName + "." + typeName;
		classesList.add(thisClassName);
		if (type.getMembers() == null)
		{
			return;
		}
		// Members are Fields and Methods
		for (BodyDeclaration bodyDeclaration : type.getMembers())
		{
			final Class<? extends BodyDeclaration> aClass = bodyDeclaration.getClass();
			if (aClass.equals(FieldDeclaration.class))
			{
				visitField((FieldDeclaration) bodyDeclaration, thisClassName);
			}
			else if (aClass.equals(MethodDeclaration.class))
			{
				visitMethod((MethodDeclaration) bodyDeclaration, thisClassName);
			}
		}
	}

	private void visitMethod(MethodDeclaration method, String parent)
	{
		String thisParent = parent + "." + method.getName();
		if (method.getParameters() != null)
		{
			for (Parameter parameter : method.getParameters())
			{
				visitParameter(parameter, thisParent);
			}
		}

		visitBlockStatements(method.getBody(), thisParent);

	}

	private void visitBlockStatements(BlockStmt blockStmt, String thisParent)
	{
		if (blockStmt == null || blockStmt.getStmts() == null)
		{
			return;
		}
		for (Statement statement : blockStmt.getStmts())
		{
			evaluateStatement(statement, thisParent);
		}
	}

	private void evaluateStatement(Statement statement, String thisParent)
	{
		if (statement.getClass().equals(ExpressionStmt.class))
		{
			visitExpressionStmt((ExpressionStmt) statement, thisParent);
		}
		else if (statement.getClass().equals(TryStmt.class))
		{
			TryStmt tryStmt = (TryStmt) statement;
			visitBlockStatements(tryStmt.getTryBlock(), thisParent);
			visitBlockStatements(tryStmt.getFinallyBlock(), thisParent);
		}
		else if (statement.getClass().equals(IfStmt.class))
		{
			IfStmt ifStmt = (IfStmt) statement;
			evaluateExpression(ifStmt.getCondition(), thisParent);
			final Statement thenStmt = ifStmt.getThenStmt();
			if (thenStmt != null && thenStmt.getClass().equals(BlockStmt.class))
			{
				visitBlockStatements((BlockStmt) thenStmt, thisParent);
			}
			else if (thenStmt != null)
			{
				evaluateStatement(thenStmt, thisParent);
			}

			final Statement elseStmt = ifStmt.getElseStmt();
			if (elseStmt != null && elseStmt.getClass().equals(BlockStmt.class))
			{
				visitBlockStatements((BlockStmt) elseStmt, thisParent);
			}
			else if (elseStmt != null)
			{
				evaluateStatement(elseStmt, thisParent);
			}

		}
	}

	private void visitExpressionStmt(ExpressionStmt statement, String thisParent)
	{
		Expression expression = statement.getExpression();
		final Class<? extends Expression> expressionClass = expression.getClass();
		evaluateExpression(expression, thisParent);

	}

	private void evaluateExpression(Expression originalExpression, String thisParent)
	{

		final Class<? extends Expression> originalExpressionClass = originalExpression.getClass();

		if (originalExpressionClass.equals(VariableDeclarationExpr.class))
		{
			String variableTypeName = "";
			VariableDeclarationExpr variableDeceleration = (VariableDeclarationExpr) originalExpression;
			if (variableDeceleration.getType().getClass().equals(PrimitiveType.class))
			{
				PrimitiveType type = (PrimitiveType) variableDeceleration.getType();
				variableTypeName = type.getType().toString();
			}
			else
			{
				ReferenceType type = (ReferenceType) variableDeceleration.getType();
				variableTypeName = type.getType().toString();
			}

			String qualifiedClassName = null;
			if (classNameToPackageName.containsKey(variableTypeName))
			{
				qualifiedClassName = classNameToPackageName.get(variableTypeName) + "." + variableTypeName;
			}

			// TODO: do we want function to class dependency ?
			// addDepends(thisParent, qualifiedClassName);
			for (VariableDeclarator variableDeclarator : variableDeceleration.getVars())
			{
				// LHS of the declaration
				String variableName = variableDeclarator.getId().getName();
				variableTypeMap.put(variableName, qualifiedClassName);

				// RHS of the declaration
				visitVariableDecelerator(variableDeclarator, thisParent);
			}
		}
		else if (originalExpressionClass.equals(MethodCallExpr.class))
		{
			MethodCallExpr expression = (MethodCallExpr) originalExpression;
			String scope = "";
			if (expression.getScope() == null)
			{
				return;
			}
			scope = evaluateScopeToString(expression.getScope(), thisParent);

			if (classNameToPackageName.get(scope) != null)
			{
				scope = classNameToPackageName.get(scope) + "." + scope;
			}
			else if (variableTypeMap.get(scope) != null)
			{
				scope = variableTypeMap.get(scope);
				if (classNameToPackageName.get(scope) != null)
				{
					scope = classNameToPackageName.get(scope) + "." + scope;
				}
			}
			else
			{
				// i don't know this type
			}
			final String fullyQualifiedMethodCall = scope + "." + expression.getName();
			addDepends(thisParent, fullyQualifiedMethodCall);
			variableTypeMap.put(fullyQualifiedMethodCall, methodReturnTypeMap.get(fullyQualifiedMethodCall));
			variableTypeMap.put(expression.getScope() + "." + expression.getName(),
					methodReturnTypeMap.get(fullyQualifiedMethodCall));
		}
		else if (originalExpressionClass.equals(BinaryExpr.class))
		{
			BinaryExpr binaryExpr = (BinaryExpr) originalExpression;
			evaluateExpression(binaryExpr.getLeft(), thisParent);
			evaluateExpression(binaryExpr.getRight(), thisParent);
		}
		else if (originalExpressionClass.equals(AssignExpr.class))
		{
			AssignExpr assignExpress = (AssignExpr) originalExpression;
			Expression target = assignExpress.getTarget();
			Expression value = assignExpress.getValue();
			evaluateExpression(value, thisParent);
		}
		else if (originalExpressionClass.equals(AssignExpr.class))
		{
			AssignExpr assignExpression = (AssignExpr) originalExpression;
			evaluateExpression(assignExpression.getValue(), thisParent);
		}
		else if (originalExpressionClass.equals(NameExpr.class))
		{
			// do nothing
			// System.out.println("Skipping - [" + originalExpressionClass + "] - " + originalExpression.toString());
		}
		else if (originalExpressionClass.equals(UnaryExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(EnclosedExpr.class))
		{
			EnclosedExpr excExpression = (EnclosedExpr) originalExpression;
			evaluateExpression(excExpression.getInner(), thisParent);
		}
		else if (originalExpressionClass.equals(StringLiteralExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(IntegerLiteralExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(BooleanLiteralExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(NullLiteralExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(InstanceOfExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(ArrayAccessExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(ThisExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(CharLiteralExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(CastExpr.class))
		{
			CastExpr castExpr = (CastExpr) originalExpression;
			evaluateExpression(castExpr.getExpr(), thisParent);
		}
		else if (originalExpressionClass.equals(ConditionalExpr.class))
		{
			ConditionalExpr conditionalExpr = (ConditionalExpr) originalExpression;
			evaluateExpression(conditionalExpr.getCondition(), thisParent);
			evaluateExpression(conditionalExpr.getElseExpr(), thisParent);
			evaluateExpression(conditionalExpr.getThenExpr(), thisParent);
		}
		else if (originalExpressionClass.equals(FieldAccessExpr.class))
		{
			// do nothing
		}
		else if (originalExpressionClass.equals(ObjectCreationExpr.class))
		{
			ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) originalExpression;
			if (objectCreationExpr.getArgs() != null)
			{

				for (Expression expression : objectCreationExpr.getArgs())
				{
					evaluateExpression(expression, thisParent);
				}
			}
		}
		else
		{
			System.out.println("How to do this - [" + originalExpressionClass + "] - " + originalExpression.toString());
		}

	}

	private String evaluateScopeToString(Expression scopeExpression, String thisParent)
	{
		String scope;
		// final Expression scopeExpression = scopeExpression.getScope();
		if (scopeExpression.getClass().equals(MethodCallExpr.class))
		{
			final MethodCallExpr methodCallScope = (MethodCallExpr) scopeExpression;

			evaluateExpression(methodCallScope, thisParent);

			final Expression methodCallScopeScope = methodCallScope.getScope();
			if (methodCallScopeScope == null)
			{
				scope = thisClassName + "." + methodCallScope.getName();
			}
			else
			{

				scope =
				        classNameToPackageName.get(methodCallScopeScope.toString()) + "." + methodCallScopeScope + "."
				                + methodCallScope.getName();
			}
		}

		else if (scopeExpression.getClass().equals(FieldAccessExpr.class))
		{
			scope = scopeExpression.toString();
		}
		else if (scopeExpression.getClass().equals(ObjectCreationExpr.class))
		{
			final ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) scopeExpression;
			final ClassOrInterfaceType type = objectCreationExpr.getType();
			final ClassOrInterfaceType typeScope = type.getScope();
			if (typeScope == null)
			{
				scope = type.toString();
			}
			else
			{
				scope = typeScope.toString();
			}
		}
		else if (scopeExpression.getClass().equals(SuperExpr.class))
		{
			scope = thisClassName;
		}
		else if (scopeExpression.getClass().equals(EnclosedExpr.class))
		{
			EnclosedExpr enclosedExpr = (EnclosedExpr) scopeExpression;
			scope = evaluateScopeToString(enclosedExpr.getInner(), thisParent);
		}
		else if (scopeExpression.getClass().equals(StringLiteralExpr.class))
		{
			scope = "String";
		}
		else if (scopeExpression.getClass().equals(CastExpr.class))
		{
			CastExpr castExpression = (CastExpr) scopeExpression;
			evaluateExpression(castExpression.getExpr(), thisParent);
			scope = ((CastExpr) scopeExpression).getType().toString();
		} else if (scopeExpression.getClass().equals(ArrayAccessExpr.class)) {
			ArrayAccessExpr arrayAccessExpr = (ArrayAccessExpr) scopeExpression;
			scope = arrayAccessExpr.getName().toString();
		}
		else
		{
			scope = ((NameExpr) scopeExpression).getName();
		}
		return scope;
	}

	private void visitVariableDeclarationExpr(VariableDeclarationExpr variableDeceleration, String thisParent)
	{
		System.out.println("i did nothing - " + variableDeceleration.toString());
	}

	private void visitParameter(Parameter parameter, String parent)
	{
		Type parameterType = parameter.getType();

		// Do we have this in our imports ?
		if (!classNameToPackageName.containsKey(parameterType.toString()))
		{
			return;
		}

		String variableName = parameter.getId().getName();
		final String parameterClassType =
		        classNameToPackageName.get(parameterType.toString()) + "." + parameterType.toString();
		variableTypeMap.put(variableName, parameterClassType);

		// TODO: lets not worry about function to Class dependency
		// addDepends(parent, parameterClassType);
	}

	private void addDepends(String parent, String dependency)
	{
		if (!dependsOn.containsKey(parent))
		{
			dependsOn.put(parent, new LinkedList<String>());
		}
		if (!dependsOn.get(parent).contains(dependency))
		{
			dependsOn.get(parent).add(dependency);
		}
	}

	private void visitField(FieldDeclaration field, String parent)
	{
		// Type of the field *ClassA* instance
		String typeName = field.getType().toString();
		// Package name of ClassA
		String typePackageName = classNameToPackageName.get(typeName);

		if (typePackageName == null)
		{
			return;
		}
		String qualifiedClassName = typePackageName + "." + typeName;

		// TODO: lets not worry about the fields
		// addDepends(parent, qualifiedClassName);

		// The field could declare multiple variables, put all into a map variableName -> itsClassType
		for (VariableDeclarator variableDeclarator : field.getVariables())
		{
			// LHS of the declaration
			String variableName = variableDeclarator.getId().getName();
			variableTypeMap.put(variableName, qualifiedClassName);

			// RHS of the declaration
			// TODO, not listing class to function dependencies
			// visitVariableDecelerator(variableDeclarator, parent);

		}

	}

	private void visitVariableDecelerator(VariableDeclarator variableDeclarator, String parent)
	{

		// RHS of the declaration
		Expression init = variableDeclarator.getInit();

		// Variable is not initialised
		if (init == null)
		{
			return;
		}

		// The initialisation is a Method call using maybe Class.staticMethod or objInstance.method()
		if (init.getClass().equals(MethodCallExpr.class))
		{
			MethodCallExpr methodCall = (MethodCallExpr) init;
			String scopeClassName = "";
			Expression scope = methodCall.getScope();
			if (scope != null && scope.getClass().equals(NameExpr.class))
			{
				String className = classNameToPackageName.get(scope.toStringWithoutComments());
				if (className == null)
				{
					className = variableTypeMap.get(scope.toStringWithoutComments());
				}
				else
				{
					className = className + "." + scope.toStringWithoutComments();
				}
				scopeClassName = className;
			}
			else if (scope != null && scope.getClass().equals(MethodCallExpr.class))
			{
				evaluateExpression((MethodCallExpr) scope, parent);
				scopeClassName =
				        variableTypeMap.get(((MethodCallExpr) scope).getScope() + "."
				                + ((MethodCallExpr) scope).getName());
			}
			else
			{
				scopeClassName = thisClassName;
			}

			// TODO: function depends on class name, do we want this ?
			// addDepends(parent, scopeClassName);
			String methodName = methodCall.getName();
			// TODO: Also check for parameters of this call
			String qualifiedMethodName = scopeClassName + "." + methodName;

			// this is a field declaration, hence the class is dependent on another method
			// TODO: Check do we want to list this or not ?
			addDepends(parent, qualifiedMethodName);
		}
		else if (init.getClass().equals(StringLiteralExpr.class))
		{
			// String x = "Hello world"
			// nothing to do
		}
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

	public Map<? extends String, ? extends List<String>> getDependsMap()
	{
		return dependsOn;
	}

	public Collection<? extends String> getAllClasses()
	{
		return classesList;
	}
}
