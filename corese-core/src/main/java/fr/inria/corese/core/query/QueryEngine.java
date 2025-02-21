package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.api.Engine;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.transform.Transformer;
import static fr.inria.corese.core.transform.Transformer.STL_PROFILE;
import fr.inria.corese.core.transform.TransformerVisitor;
import fr.inria.corese.kgram.filter.Extension;
import fr.inria.corese.sparql.triple.parser.ASTExtension;

/**
 * Equivalent of RuleEngine for Query and Template Run a set of query
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryEngine implements Engine {

    private static Logger logger = LoggerFactory.getLogger(QueryEngine.class);
    Graph graph;
    private QueryProcess exec;
    ArrayList<Query> list;
    private Dataset ds;
    HashMap<String, Query> table;
    HashMap<String, ArrayList<Query>> tableList;
    TemplateIndex index;
    boolean isDebug = false,
            isActivate = true,
            isWorkflow = false;
    private boolean transformation = false;
    private String base;

    // focus type -> templates
    QueryEngine(Graph g) {
        graph = g;
        exec = QueryProcess.create(g);
        list = new ArrayList<Query>();
        table = new HashMap<String, Query>();       
        index = new TemplateIndex();
    }

    public static QueryEngine create(Graph g) {
        return new QueryEngine(g);
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    public void addQuery(String q) {
        try {
            defQuery(q);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Query defQuery(String q) throws EngineException {
        //System.out.println("** QE: \n" + q);
        if (getBase() != null){
            getQueryProcess().setBase(getBase());
        }
        Query qq = getQueryProcess().compile(q, ds);
        if (qq != null) {
            defQuery(qq);
        }
        return qq;
    }

    public void defQuery(Query q) {
        if (q.isTemplate()) {
            defTemplate(q);
        } else {
            list.add(q);
        }
    }

    /**
     * Named templates are stored in a table, not in the list
     */
    public void defTemplate(Query q) {
        q.setPrinterTemplate(true);
        if (q.getName() != null) {
            table.put(q.getName(), q);
        } else {
            list.add(q);
            index.add(q);
        }
    }

    /**
     * called once with this transformer map and 
     * may be called again with outer transformer map if any
     * map belongs to current or outer transformer
     * current transformer may inherit table from outer transformer
     * hence all subtransformers share same table
     * table: transformation -> Transformer
     */
    public void complete(Transformer trans) {
        complete();
        //trans.setTransformerMap(map);
        for (Query q : getTemplates()) {
            trans.complete(q);
            complete(q);
        }
        for (Query q : getNamedTemplates()) {
            trans.complete(q);
            complete(q);
        }
    }
    
    void complete(Query q) {
        q.setTransformationTemplate(true);
        q.setListPath(true);
    }
    
    void complete(){
        if (tableList == null){
            tableList = new HashMap<String, ArrayList<Query>>();
            for (String name : table.keySet()){
                ArrayList<Query> list = new ArrayList<Query>(1);
                list.add(table.get(name));
                tableList.put(name, list);
            }
        }
    }

    /**
     * templates share profile function definitions
     * function st:optimize(){} : run TransformerVisitor to optimize template
     */
    public void profile() {
        Query profile = getTemplate(STL_PROFILE);
        if (profile != null) {

            if (profile.getExtension() != null) {
                // share profile function definitions in templates
                fr.inria.corese.compiler.parser.Transformer tr = fr.inria.corese.compiler.parser.Transformer.create();
                ASTExtension ext = Interpreter.getExtension(profile);
                tr.definePublic(ext, profile, false);
                TransformerVisitor tv = new TransformerVisitor(profile.getExtension().get(Transformer.STL_OPTIMIZE) != null);
                
                for (Query t : getTemplates()) {
                    addExtension(t, ext);
                    tv.visit(t);
                }
                for (Query t : getNamedTemplates()) {
                    addExtension(t, ext);
                    tv.visit(t);
                }
            }
        }
    }
    
    void addExtension(Query q, ASTExtension ext){
        if (ext == null){
            return;
        }
        if (q.getExtension() == null){
            q.setExtension(ext);
        }
        else {
            //q.getExtension().add(ext);
            Interpreter.getExtension(q).add(ext);
        }
    }
            
    public void trace() {
        System.out.println(index.toString());
    }

    public List<Query> getQueries() {
        return list;
    }

    public List<Query> getTemplates() {
        return list;
    }

    public List<Query> getTemplates(IDatatype dt) {
        String type = null;
        if (dt != null) {
            type = dt.getLabel();
        }
        List<Query> l = index.getTemplates(type);
        if (l != null) {
            return l;
        }
        return list;
    }

    public Query getTemplate(String name) {
        return table.get(name);
    }
    
    public List<Query> getTemplateList(String name) {
        return tableList.get(name);
    }

    public Collection<Query> getNamedTemplates() {
        return table.values();
    }
    
    public Query getTemplate() {
        Query q = getTemplate(STL_PROFILE);
        if (q != null) {
            return q;
        } else if (getTemplates().isEmpty()) {
            for (Query qq : table.values()){
                    return qq;
            }
        } else {
            return getTemplates().get(0);
        }
        return null;
    }
    

    public boolean isEmpty() {
        return list.isEmpty() && table.isEmpty();
    }
    
    public boolean contains(Query q){
        if (q.getName() == null){
            return list.contains(q);
        }
        else {
            return table.containsValue(q);
        }
    }

    @Override
    public boolean process() {
        if (!isActivate) {
            return false;
        }

        boolean b = false;
        if (isWorkflow) {
            // TRICKY:
            // This engine is part of a workflow which is processed by graph.init()
            // hence it is synchronized by graph.init() 
            // We are here because a query is processed, hence a (read) lock has been taken
            // tell the query processor that it is already synchronized to prevent QueryProcess synUpdate
            // to take a write lock that would cause a deadlock
            getQueryProcess().setSynchronized(true);
        }
        for (Query q : list) {

            //q.setSynchronized(isWorkflow);
            if (isDebug) {
                q.setDebug(isDebug);
                System.out.println(q.getAST());
            }
            Mappings map = getQueryProcess().query(q);
            b = map.nbUpdate() > 0 || b;
            if (isDebug) {
                logger.debug(map + "\n");
            }
        }
        return b;
    }

    public Mappings process(Query q, Mapping m) {
        try {
            Mappings map = getQueryProcess().query(null, q, m, null);
            return map;
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Mappings.create(q);
    }

    /**
     * pname is property name queries are construct where find a query with
     * construct {?x pname ?y} process the query use case: ProducerImpl
     * getEdges() computed by construct-where
     */
    Mappings process(Node start, String pname, int index) {
        for (Query q : getQueries()) {

            if (q.isConstruct()) {
                Exp cons = q.getConstruct();
                for (Exp ee : cons.getExpList()) {

                    if (ee.isEdge()) {
                        Edge edge = ee.getEdge();
                        if (edge.getLabel().equals(pname)) {

                            Mapping bind = null;
                            if (start != null) {
                                bind = Mapping.create(edge.getNode(index), start);
                            }

                            Mappings map = process(q, bind);
                            return map;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setActivate(boolean b) {
        isActivate = b;
    }

    public boolean isActivate() {
        return isActivate;
    }

    /**
     * This method is called by a workflow where this engine is submitted
     */
    public void init() {
        isWorkflow = true;
    }

    public void remove() {
    }

    public void onDelete() {
    }

    public void onInsert(Node gNode, Edge edge) {
    }

    public void onClear() {
    }

    public int type() {
        return Engine.QUERY_ENGINE;
    }

    public void sort() {
        index.sort(list);
        index.sort();
    }

    public void clean() {
        ArrayList<Query> l = new ArrayList<Query>();
        for (Query q : list) {
            if (!q.isFail()) {
                l.add(q);
            }
        }
        list = l;
    }

    /**
     * @return the ds
     */
    public Dataset getDataset() {
        return ds;
    }

    /**
     * @param ds the ds to set
     */
    public void setDataset(Dataset ds) {
        this.ds = ds;
    }

    /**
     * @return the transformation
     */
    public boolean isTransformation() {
        return transformation;
    }

    /**
     * @param transformation the transformation to set
     */
    public void setTransformation(boolean transformation) {
        this.transformation = transformation;
    }

    public void setVisitor(QueryVisitor vis) {
        getQueryProcess().setVisitor(vis);
    }

    /**
     * @return the exec
     */
    public QueryProcess getQueryProcess() {
        return exec;
    }

    /**
     * @return the base
     */
    public String getBase() {
        return base;
    }

    /**
     * @param base the base to set
     */
    public void setBase(String base) {
        this.base = base;
    }
}
