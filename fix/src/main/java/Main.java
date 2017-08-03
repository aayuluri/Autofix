import java.io.*;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;



public class Main {



    public static void saveSource(Document document) {

        try {
            String newSource = document.get();
            File tempFile = new File(Constants.WORKING_DIR + "Fixed.java");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(newSource.getBytes());
            fos.flush();
            System.out.println("File saved!");
        } catch (Exception e) {
            System.out.println("File not saved!");
        }
    }

    public static void applyChanges(ASTRewrite astRewrite, String oldContent) {
        Document document = new Document(oldContent);
        try {
            TextEdit edits = astRewrite.rewriteAST(document, null);
            edits.apply(document);
            saveSource(document);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static int getStartLineNumber(final ASTNode node, CompilationUnit root) {
        return root.getLineNumber(node.getStartPosition());
    }

    public static int getEndLineNumber(final ASTNode node, CompilationUnit root) {
        if (node instanceof IfStatement) {
            final ASTNode elseStatement = ((IfStatement) node)
                    .getElseStatement();
            final int thenEnd = (elseStatement == null) ? node
                    .getStartPosition() + node.getLength() : elseStatement
                    .getStartPosition() - 1;
            return root.getLineNumber(thenEnd);
        } else if (node instanceof TryStatement) {
            final TryStatement tryStatement = (TryStatement) node;
            int tryEnd = 0;
            for (Object obj : tryStatement.catchClauses()) {
                CatchClause catchClause = (CatchClause) obj;
                tryEnd = catchClause.getStartPosition() - 1;
                break;
            }
            if (tryEnd == 0) {
                final Block finallyBlock = tryStatement.getFinally();
                if (finallyBlock != null) {
                    tryEnd = finallyBlock.getStartPosition() - 1;
                }
            }
            if (tryEnd == 0) {
                tryEnd = node.getStartPosition() + node.getLength();
            }
            return root.getLineNumber(tryEnd);
        } else {
            return root.getLineNumber(node.getStartPosition()
                    + node.getLength());
        }
    }

    public static void switchIsDefaultLastFix(String oldContent, int switchStatementLocation) {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(oldContent.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite astRewrite = ASTRewrite.create(ast);

        cu.accept(new ASTVisitor() {

            public boolean visit(SwitchStatement node) {

                if(getStartLineNumber(node, cu) == switchStatementLocation) {
                    SwitchCase newSwitchCase = ast.newSwitchCase();
                    newSwitchCase.setExpression(null);

                    ListRewrite listRewrite = astRewrite.getListRewrite(node, SwitchStatement.STATEMENTS_PROPERTY);
                    listRewrite.insertLast(newSwitchCase, null);

                    Statement comment = (Statement) astRewrite.createStringPlaceholder("// Added a blank default case", ASTNode.EMPTY_STATEMENT);
                    listRewrite.insertLast(comment, null);

                    BreakStatement breakStatement = ast.newBreakStatement();
                    listRewrite.insertLast(breakStatement, null);

                    applyChanges(astRewrite, oldContent);

                }

                return true;
            }


        });

    }

    public static void switchIsDefaultLastFix(String oldContent, int defaultcaseStartLocation, int defaultCaseEndLocation) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(oldContent.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite astRewrite = ASTRewrite.create(ast);

        cu.accept(new ASTVisitor() {

            public boolean visit(SwitchCase node) {

                if (getStartLineNumber(node, cu) == defaultcaseStartLocation) {

                    SwitchCase newSwitchCase = ast.newSwitchCase();
                    newSwitchCase.setExpression(null);

                    ListRewrite listRewrite = astRewrite.getListRewrite(node.getParent(), SwitchStatement.STATEMENTS_PROPERTY);
                    listRewrite.remove(node, null);
                    listRewrite.insertLast(newSwitchCase, null);

                    applyChanges(astRewrite, oldContent);

                }

                return true;
            }

            public boolean visit(ExpressionStatement node) {

                if (getStartLineNumber(node, cu) >= defaultcaseStartLocation && getEndLineNumber(node, cu) <= defaultCaseEndLocation) {

                    ListRewrite listRewrite = astRewrite.getListRewrite(node.getParent(), SwitchStatement.STATEMENTS_PROPERTY);
                    listRewrite.remove(node, null);
                    listRewrite.insertLast(node, null);

                    applyChanges(astRewrite, oldContent);

                }

                return true;
            }

            public boolean visit(BreakStatement node) {

                if (getStartLineNumber(node, cu) >= defaultcaseStartLocation && getEndLineNumber(node, cu) <= defaultCaseEndLocation) {

                    ListRewrite listRewrite = astRewrite.getListRewrite(node.getParent(), SwitchStatement.STATEMENTS_PROPERTY);
                    listRewrite.remove(node, null);
                    listRewrite.insertLast(node, null);

                    applyChanges(astRewrite, oldContent);
                }

                return true;
            }

        });

    }


    public static void createSetterMethod(AST ast, ASTRewrite astRewrite, FieldDeclaration node, String oldContent) {

        MethodDeclaration setterMethod = ast.newMethodDeclaration();

        VariableDeclarationFragment var = (VariableDeclarationFragment) node.fragments().get(0);
        String methodName = "set" + var.getName();
        SimpleName simpleName = ast.newSimpleName(methodName);


        setterMethod.modifiers().addAll(ast.newModifiers(Modifier.PUBLIC));
        setterMethod.setName(simpleName);

        SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
        param.setName(ast.newSimpleName(var.getName().toString()));
        setterMethod.parameters().add(param);

        Assignment assignment = ast.newAssignment();

        FieldAccess fieldAccess = ast.newFieldAccess();
        ThisExpression thisExpression = ast.newThisExpression();
        fieldAccess.setExpression(thisExpression);
        fieldAccess.setName(ast.newSimpleName(var.getName().toString()));

        Expression rightHandExpression = ast.newSimpleName(var.getName().toString());


        assignment.setLeftHandSide(fieldAccess);
        assignment.setRightHandSide(rightHandExpression);
        assignment.setOperator(Assignment.Operator.ASSIGN);
        ExpressionStatement expressionStatement = ast.newExpressionStatement(assignment);


        Block block = ast.newBlock();
        setterMethod.setBody(block);

        block.statements().add(expressionStatement);

        ListRewrite listRewrite = astRewrite.getListRewrite(node.getParent(), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertLast(setterMethod, null);

        applyChanges(astRewrite, oldContent);

    }

    public static void createGetterMethod(AST ast, ASTRewrite astRewrite, FieldDeclaration node, String oldContent) {
        MethodDeclaration getterMethod = ast.newMethodDeclaration();

        VariableDeclarationFragment var = (VariableDeclarationFragment) node.fragments().get(0);
        String methodName = "get" + var.getName();
        SimpleName simpleName = ast.newSimpleName(methodName);

        getterMethod.modifiers().addAll(ast.newModifiers(Modifier.PUBLIC));
        getterMethod.setName(simpleName);

        Block block = ast.newBlock();
        getterMethod.setBody(block);

        Type returnType = ast.newPrimitiveType(PrimitiveType.INT);
        getterMethod.setReturnType2(returnType);

        ReturnStatement returnStatement = ast.newReturnStatement();
        Expression expression = ast.newSimpleName(var.getName().toString());
        returnStatement.setExpression(expression);
        block.statements().add(returnStatement);



        ListRewrite listRewrite = astRewrite.getListRewrite(node.getParent(), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertLast(getterMethod, null);


        applyChanges(astRewrite, oldContent);
    }


    public static void privateStaticNonFinalFieldsFix(String oldContent, int fieldDeclarationLocation) {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(oldContent.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite astRewrite = ASTRewrite.create(ast);

        cu.accept(new ASTVisitor() {

            public boolean visit(FieldDeclaration node) {
                //String fieldName = node.fragments();
                System.out.println(node.getParent());
                if(getStartLineNumber(node, cu) == fieldDeclarationLocation) {
                    createSetterMethod(ast, astRewrite, node, oldContent);
                    createGetterMethod(ast, astRewrite, node, oldContent);
                }
                return true;
            }

        });
    }




    //read file content into a string
    public static String readFileToString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        char[] buf = new char[10];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            //System.out.println(numRead);
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }

        reader.close();

        return fileData.toString();
    }


    //loop directory to get file list
    public static void ParseFilesInDir() throws IOException {

        String filePath = Constants.WORKING_DIR + "Bad.java";
        int startLineNumber = 16, endLineNumber = 18;
        File root = new File(filePath);
        if (root.isFile()) {
            String content = readFileToString(filePath);
//            switchIsDefaultLastFix(content, startLineNumber, endLineNumber);
//            switchIsDefaultLastFix(content, 12);
            privateStaticNonFinalFieldsFix(content, 5);
        }
    }


    public static void main(String[] args) throws IOException {
        ParseFilesInDir();
    }
}