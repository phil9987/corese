package fr.inria.corese.sparql.triple.update;

import fr.inria.corese.sparql.triple.parser.ASTBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class ASTUpdate {

    static final String NL = System.getProperty("line.separator");
    private static final String PV = ";";

    List<Update> list;
    ASTQuery ast;
    Basic prolog;

    ASTUpdate() {
        list = new ArrayList<Update>();
        prolog = Basic.create(Update.PROLOG);
    }

    public static ASTUpdate create() {
        return new ASTUpdate();
    }
    
    public ASTUpdate subCreate() {
        ASTUpdate ast = new ASTUpdate();
        ast.setProlog(prolog);
        return ast;
    }

    @Override
    public String toString() {
        ASTBuffer sb = new ASTBuffer();
        toString(sb);
        return sb.toString();
    }

    public ASTBuffer toString(ASTBuffer sb) {
        for (Update ast : list) {
            ast.toString(sb);
            if (ast.type() != Update.PROLOG) {
                sb.append(PV);
                sb.nl();
            }
        }
        return sb;
    }

    public void set(ASTQuery a) {
        ast = a;
    }

    ASTQuery getASTQuery() {
        return ast;
    }

    public void add(Update ope) {
        ope.set(this);
        list.add(ope);
    }

    public List<Update> getUpdates() {
        return list;
    }

    public String getGraphName() {
        for (Update up : getUpdates()) {
            Constant cst = up.getGraphName();
            if (cst != null) {
                return cst.getLabel();
            }
        }
        return null;
    }

    public void defNamespace(String p, String ns) {
        prolog.defNamespace(p, ns);
    }

    public void defService(String ns) {

    }

    public void defBase(String s) {
        prolog.defBase(s);
    }
    
    void setProlog(Basic p) {
        prolog = p;
    }
            

    /**
     * A new prolog have been declared within a list of updates Insert prolog
     * and copy global NSM
     */
    public void defProlog() {
        if (prolog.hasContent()) {
            NSManager nsm = getNSM().copy();
            prolog.setLocalNSM(nsm);
            add(prolog);
            prolog = Basic.create(Update.PROLOG);
        }
    }

    public NSManager getNSM() {
        return getASTQuery().getNSM();
    }

}
