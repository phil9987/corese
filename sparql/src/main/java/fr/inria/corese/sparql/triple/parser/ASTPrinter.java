package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ASTPrinter implements KeywordPP {

    ASTQuery ast;
    private ASTBuffer sb;
    private boolean prefix = true;
    private boolean lambda = false;
    
    public ASTPrinter(ASTQuery a){
        ast = a;
        setBuffer(new ASTBuffer());
    }
    
    public ASTPrinter(ASTQuery a, ASTBuffer sb){
        ast = a;
        setBuffer(sb);
    }
    
    public void setCompiler(JavaCompiler jc) {
        getBuffer().setCompiler(jc);
    }
    
    @Override
    public String toString() {
        process();
        return getBuffer().toString();
    }
    
    public void process() {
        if (ast.isUpdate()) {
            ast.getUpdate().toString(sb);
        } else {
            if (isPrefix()){
                sb.append(ast.getNSM().toString(null, false, false));
            }
            
            if (ast.isTemplate()) {
                template();
            }
            
            getSparqlHeader();
                       
            if (!ast.isData() && (!ast.isDescribe() || ast.getBody() != null)) {
                if (ast.getBody() != null) {
                    sb.append(WHERE).append(" ");
                    ast.getBody().pretty(sb);
                }
            }

            if (!ast.isAsk()) {
                sb.nl();
                getSparqlSolutionModifier();
            }
        }

        getFinal();
    }
    
    void template() {
        sb.append("# template").append(" ");
        if (ast.getName() != null) {
            sb.append(ast.getName()).append(" ");
        }
        if (ast.getArgList() != null) {
            sb.append(ast.getArgList());
        }
        sb.nl();
    }

    public ASTBuffer getSparqlPrefix(Exp exp, ASTBuffer sb) {
        for (Exp e : exp.getBody()) {
            Triple t = e.getTriple();
            String r = t.getSubject().getName();
            String p = t.getPredicate().getName();
            String v = t.getObject().getName();

            // if v starts with "<function://", we have add a ".", so we have to remove it now
            if (v.startsWith(CORESE_PREFIX)
                    && v.endsWith(".")) {
                v = v.substring(0, v.length() - 1);
            }

            if (r.equalsIgnoreCase(PREFIX)) {
                sb.append(PREFIX + SPACE).append(p)
                        .append(": ").append(OPEN).append(v).append(CLOSE).nl();
            } else if (r.equalsIgnoreCase(BASE)) {
                sb.append(BASE + SPACE + OPEN)
                        .append(v).append(CLOSE).nl();
            }
        }
        return sb;
    }

    /**
     * Return the header part of the SPARQL-like Query (2nd parser)
     *
     * @return
     */
    ASTBuffer getSparqlHeader() {
        List<Constant> from = ast.getFrom();
        List<Constant> named = ast.getNamed();
        List<Variable> select = ast.getSelectVar();

        // Select
        if (ast.isSelect()) {
            sb.append(SELECT).append(SPACE);

            if (ast.isDebug()) {
                sb.append(DEBUG).append(SPACE);
            }

            if (ast.isMore()) {
                sb.append(MORE).append(SPACE);
            }

            if (ast.isDistinct()) {
                sb.append(DISTINCT).append(SPACE);
            }

            if (ast.isSelectAll()) {
                sb.append(STAR).append(SPACE);
            }

            if (select != null && select.size() > 0) {
                for (Variable s : ast.getSelectVar()) {

                    if (ast.getExpression(s) != null) {
                        expr(ast.getExpression(s), s, sb);
                    } else {
                        sb.append(s);
                    }
                    sb.append(SPACE);
                }
            }

        } else if (ast.isAsk()) {
            sb.append(ASK).append(SPACE);
        } else if (ast.isDelete()) {
            sb.append(DELETE).append(SPACE);
            if (ast.isDeleteData()) {
                sb.append(DATA).append(SPACE);
            }
            ast.getDelete().toString(sb);

            if (ast.isInsert()) {
                sb.append(INSERT).append(SPACE);
                ast.getInsert().toString(sb);
            }

        } else if (ast.isConstruct()) {
            if (ast.isInsert()) {
                sb.append(INSERT).append(SPACE);
                if (ast.isInsertData()) {
                    sb.append(DATA).append(SPACE);
                }
                ast.getInsert().toString(sb);
            } else if (ast.getConstruct() != null) {
                sb.append(CONSTRUCT).append(SPACE);
                ast.getConstruct().toString(sb);
            } else if (ast.getInsert() != null) {
                sb.append(INSERT).append(SPACE);
                ast.getInsert().toString(sb);
            } else if (ast.getDelete() != null) {
                sb.append(DELETE).append(SPACE);
                if (ast.isDeleteData()) {
                    sb.append(DATA).append(SPACE);
                }
                ast.getDelete().toString(sb);
            }
        } else if (ast.isDescribe()) {
            sb.append(DESCRIBE).append(SPACE);

            if (ast.isDescribeAll()) {
                sb.append(STAR).append(SPACE);
            } else if (ast.adescribe != null && ast.adescribe.size() > 0) {

                for (Atom at : ast.adescribe) {
                    at.toString(sb);
                    sb.append(SPACE);
                }
            }
        }

        // DataSet
        //if (! isConstruct())    // because it's already done in the construct case
        sb.nl();

        // From
        for (Atom name : from) {
            sb.append(FROM, SPACE);
            name.toString(sb);
            sb.nl();
        }

        // From Named
        for (Atom name : named) {
            sb.append(FROM, SPACE, NAMED, SPACE);
            name.toString(sb);
            sb.nl();
        }
      
        return sb;
    }

    void expr(Expression exp, Variable var, ASTBuffer sb) {
        sb.append("(");
        exp.toString(sb);
        sb.append(" as ");

        if (var.getVariableList() != null) {
            sb.append("(");
            int i = 0;
            for (Variable v : var.getVariableList()) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(v);
            }
            sb.append(")");
        } else {
            sb.append(var);
        }
        sb.append(")");
    }
    
     /**
     * return the solution modifiers : order by, limit, offset
     *
     * @param parser
     * @return
     */
    ASTBuffer getSparqlSolutionModifier() {
        List<Expression> sort = ast.getSort();
        List<Boolean> reverse = ast.getReverse();

        if (ast.getGroupBy().size() > 0) {
            sb.append(GROUPBY).append(SPACE);
            for (Expression exp : ast.getGroupBy()) {
                sb.append(exp.toString()).append(SPACE);
            }
            sb.nl();
        }

        if (sort.size() > 0) {
            int i = 0;
            sb.append(ORDERBY).append(SPACE);

            for (Expression exp : ast.getOrderBy()) {

                boolean breverse = reverse.get(i++);
                if (breverse) {
                    sb.append(DESC, "(");
                }
                sb.append(exp.toString());
                if (breverse) {
                    sb.append(")");
                }
                sb.append(SPACE);
            }
            sb.nl();
        }

        if (ast.getOffset() > 0) {
            sb.append(OFFSET).append(SPACE).append(ast.getOffset()).append(SPACE);
        }

        if (ast.getMaxResult() != ast.getDefaultMaxResult()) {
            sb.append(LIMIT).append(SPACE).append(ast.getMaxResult()).append(SPACE);
        }

        if (ast.getHaving() != null) {
            sb.append(HAVING);
            sb.append(OPEN_PAREN);
            ast.getHaving().toString(sb);
            sb.append(CLOSE_PAREN);
        }

        if (sb.length() > 0) {
            //sb.append(NL);
        }

        return sb;
    }

    void getFinal() {
        String SPACE = " ";

        if (ast.getValues() != null) {
            ast.getValues().toString(sb);
        }

        if (ast.getPragma() != null) {
            sb.append(PRAGMA);
            sb.append(SPACE);
            ast.getPragma().toString(sb);
        }

        if (ast.getGlobalASTBasic()!= null) {
            // ast is a subquery, do not print functions
        }
        else {
            function(sb);
        }
        
    }
    
    void function(ASTBuffer sb) {
        function(sb, ast.getDefine());
        if (isLambda()) {
            function(sb, ast.getDefineLambda());
        }
    }
    
    void function(ASTBuffer sb, ASTExtension ext) {
        for (Expression fun : ext.getFunList()) {
            sb.nl();
            fun.toString(sb);
            sb.nl();
        }
    }
    
    /**
     * @return the prefix
     */
    public boolean isPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(boolean prefix) {
        this.prefix = prefix;
    }
    
    /**
     * @return the lambda
     */
    public boolean isLambda() {
        return lambda;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(boolean lambda) {
        this.lambda = lambda;
    }

    /**
     * @return the sb
     */
    public ASTBuffer getBuffer() {
        return sb;
    }

    /**
     * @param sb the sb to set
     */
    public void setBuffer(ASTBuffer sb) {
        this.sb = sb;
    }
    

}
