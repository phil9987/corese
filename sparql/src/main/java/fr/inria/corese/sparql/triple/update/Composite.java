package fr.inria.corese.sparql.triple.update;

import fr.inria.corese.sparql.triple.parser.ASTBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Values;

/**
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Composite extends Update {

    static final String WITH = "with";
    static final String USING = "using";
    static final String NAMED = "using named";
    static final String WHERE = "where";
    static final String DATA = "data";
    static final String NL = System.getProperty("line.separator");

    Exp data,
            // insert delete
            pattern,
            // where
            body;
    // list insert delete
    List<Composite> list;

    Constant with;
    Dataset ds;
    private Values values;

    Composite(int t) {
        type = t;
        list = new ArrayList<Composite>();
        ds = Dataset.create();

    }

    Composite(int t, Exp d) {
        this(t);
        data = d;
    }

    public static Composite create(int type) {
        return new Composite(type);
    }

    public static Composite create(int type, Exp d) {
        Composite ope = new Composite(type, d);
        return ope;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {

        if (type() != COMPOSITE) {
            sb.append(title(), " ");
        }

        if (getData() != null) {
            sb.append(DATA, " ");
            getData().pretty(sb);
        } else {
            if (getWith() != null) {
                sb.append(WITH, " ", getWith().toString()).nl();
            }
            for (Composite cc : getUpdates()) {
                sb.append(cc.title(), " ");
                if (cc.getPattern() != getBody()) {
                    // use case: delete where {}
                    // no pattern in this case
                    cc.getPattern().pretty(sb);
                    sb.nl();
                }
            }

            for (Constant uri : getUsing()) {
                sb.append(USING, " ", uri.toString()).nl();
            }

            for (Constant uri : getNamed()) {
                sb.append(NAMED, " ", uri.toString()).nl();
            }

            if (getBody() != null) {
                sb.append(WHERE, " ");
                getBody().pretty(sb);
            }
        }
        return sb;
    }

    public void setPattern(Exp d) {
        pattern = d;
    }

    public Exp getPattern() {
        return pattern;
    }

    public void setBody(Exp d) {
        body = d;
    }

    public Exp getBody() {
        return body;
    }

    public Exp getData() {
        return data;
    }

    public void add(Composite ope) {
        list.add(ope);
    }

    public List<Composite> getUpdates() {
        return list;
    }

    public void setWith(Constant uri) {
        with = uri;
    }

    public Constant getWith() {
        return with;
    }

    // TODO: insert { graph uri {} } where {}
    @Override
    public Constant getGraphName() {
        if (getWith() != null) {
            return getWith();
        }
        if (getData() != null) {
            return getData().getGraphName();
        }
        return getGraphNameDeleteInsert();
    }
    
    Constant getGraphNameDeleteInsert() {
        for (Composite c : list) {
            Constant cst = c.getPattern().getGraphName();
            if (cst != null) {
                return cst;
            }
        }
        return null;
    }

    public void addUsing(Constant uri) {
        ds.addFrom(uri);
    }

    public List<Constant> getUsing() {
        return ds.getFrom();
    }

    public void addNamed(Constant uri) {
        ds.addNamed(uri);
    }

    public List<Constant> getNamed() {
        return ds.getNamed();
    }

    public Dataset getDataset() {
        return ds;
    }

    /**
     * @return the values
     */
    public Values getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(Values values) {
        this.values = values;
    }

}
