package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.compiler.eval.SQLResult;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.producer.DataProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Implements unnest() statement 
 * values ?var { unnest(exp) }
 * bind (unnest(?exp) as ?var)
 * bind (unnest(?exp) as (?x, ?y))
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class Mapper {
    Producer p;
    MapperSQL mapper;
    
    Mapper(Producer p){
        this.p = p;
        mapper = new MapperSQL(p);

    }
    
    
    public Mappings map(List<Node> nodes, Object object) {
        if (object instanceof IDatatype) {
            return map(nodes, (IDatatype) object);
        }  
        else if (object instanceof Mappings) {
            return map(nodes, (Mappings) object);
        } 
        else if (object instanceof Collection){
            return map(nodes, (Collection<IDatatype>)object);
        }
        else if (object instanceof SQLResult) {
            return mapper.sql(nodes, (SQLResult) object);
        }
        return new Mappings();
    }
    
    
    Mappings map(List<Node> nodes, IDatatype dt) {   
        if (dt.isList()) { 
            return map(nodes, dt.getValues());
        } 
        else if (dt.isMap() || dt.isJSON()) { 
            return map(nodes, dt.getValueList());
        } 
        else if (dt.isPointer()){
            return map(nodes, dt.getPointerObject());
        }
        else if (dt.getObject() != null) {
            return map(nodes, dt.getObject());
        } else  {
            return mapDT(nodes, dt);
        }
    }
    
    Mappings map(List<Node> nodes, Pointerable obj) { 
        switch (obj.pointerType()){
            case GRAPH: 
                return map(nodes, (Graph) obj.getTripleStore());
                
            case MAPPINGS:
                return map(nodes, obj.getMappings());
                
            case TRIPLE:
                 return map(nodes, obj.getEdge()); 
                
            case NSMANAGER:
                 return map(nodes, (NSManager) obj);   
                
             case QUERY:
                 return map(nodes, obj.getQuery());
                 
             case CONTEXT:
                 return map(nodes, (Context) obj);
                 
             case METADATA:
                 return map(nodes, (Metadata) obj); 
                 
             case PRODUCER:
                 return map(nodes, (DataProducer) obj);                  
        }
        
        return map(nodes, (Object) obj);
    }
    
    /**
     * bind (unnest(us:graph()) as (?s, ?p, ?o))
     * bind (unnest(us:graph()) as ?t)
     */
    Mappings map(List<Node> varList, Graph g){
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Node[] nodes;
        Mappings map = new Mappings();
        int size = varList.size();
        if (! (size == 1 || size == 3 || size == 4)){
            return map;
        }
        for (Edge ent : g.getEdges()){
            nodes = new Node[size];
            if (size >= 3){
                nodeArray(ent, nodes);
            }
            else {
                nodes[0] = DatatypeMap.createObject(g.getEdgeFactory().copy(ent));
            }
            map.add(Mapping.create(qNodes, nodes));           
        }
        
        return map;
    }
    
    void nodeArray(Edge ent, Node[] nodes){
        nodes[0] = ent.getNode(0);
        nodes[1] = ent.getEdgeNode();
        nodes[2] = ent.getNode(1);
        if (nodes.length > 3){
            nodes[3] = ent.getGraph(); 
        }
    }
        
    Mappings map(List<Node> varList, Edge e) {
        Mappings map = new Mappings();
        int size = varList.size();
        if (size != 1) {
            return map;
        }
        Node[] qNodes = new Node[1];
        varList.toArray(qNodes);
        
        for (Object obj : e.getLoop()){
            Node[] nodes = new Node[1];
            nodes[0] = (Node) obj;
            map.add(Mapping.create(qNodes, nodes));
        }

        return map;
    }
    
    Mappings map(List<Node> varList, NSManager nsm){
        return mapListOfList(varList, nsm.getList().getValues());
    }
     
    Mappings map(List<Node> varList, Context c){
        return mapListOfList(varList, c.getList().getValues());
    } 
    
    Mappings map(List<Node> varList, Metadata m){
        return map(varList, m.getList().getValues());
    } 
    
    Mappings map(List<Node> varList, DataProducer d){        
        return map(varList, d.getList().getValues());
    } 
    
    Mappings mapListOfList(List<Node> varList, List<IDatatype> listOfList){
        Mappings map =  new Mappings(); 
        int size = varList.size();
        if (size > 2){
            return map;
        }
        Node[] qNodes = new Node[size];
        varList.toArray(qNodes);
            
        for (IDatatype def : listOfList){          
                Node[] tn = new Node[size];
                for (int i = 0; i < size; i++){
                    tn[i] = def.get(i);
                }        
                map.add(Mapping.create(qNodes, tn));           
        }
        return map;
    } 
      
    Mappings map(List<Node> varList, Query q){
        if (varList.size() != 1){
            return new Mappings(); 
        }
       ArrayList<IDatatype> list = new  ArrayList<IDatatype>();
       for (Edge e : q.getEdges()){          
            list.add(DatatypeMap.createObject(e));                                         
       }
       return map(varList, list);
    }
      
    Mappings map(List<Node> lNodes, Collection<IDatatype> list) {
        Mappings map = new Mappings();
        for (IDatatype dt : list){
            Mapping m =  Mapping.create(lNodes.get(0), dt);
            map.add(m);
        }
        return map;
    }
    
    
    /**
     * bind (unnest(exp) as ?x)
     * bind (unnest(exp) as (?x, ?y))
     * eval(exp) = list
     * 
     * 
     */
    Mappings map(List<Node> varList, List<IDatatype> valueList) {
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Mappings map = new Mappings();
        Mapping m;
        for (IDatatype dt : valueList){
            if (dt.isList() && varList.size() > 1){ 
                // bind (((1 2)(3 4)) as (?x, ?y))
                // ?x = 1 ; ?y = 2
                 Node[] val = new Node[dt.size()];
                 m = Mapping.create(qNodes, dt.getValues().toArray(val));
            }
            else {
                // bind (((1 2)(3 4)) as ?x)
                // HINT: bind (((1 2)(3 4)) as (?x))
                // ?x = (1 2)
                 m =  Mapping.create(varList.get(0), dt);
            }
           
            map.add(m);
        }
        return map;
    }
    
    Mappings map(List<Node> lNodes, IDatatype[] list) {
        Mappings map = new Mappings();
        for (IDatatype dt : list){
            Mapping m =  Mapping.create(lNodes.get(0), dt);
            map.add(m);
        }
        return map;
    }

    /**
     * Binding by name
     * Eval extBind() bind list of Node on this Mappings by name
     */
    Mappings map(List<Node> list, Mappings map) {       
        return map;
    }

    Mappings mapDT(List<Node> varList, IDatatype dt) {
        Node[] qNodes = new Node[varList.size()];
        varList.toArray(qNodes);
        Mappings lMap = new Mappings();
        List<Node> lNode = toNodeList(dt);
        for (Node node : lNode) {
            Node[] tNodes = new Node[1];
            tNodes[0] = node;
            Mapping map = Mapping.create(qNodes, tNodes);
            lMap.add(map);
        }
        return lMap;
    }
    


    public List<Node> toNodeList(Object obj) {
        IDatatype dt = (IDatatype) obj;
        List<Node> list = new ArrayList<Node>();
        if (dt.isList()) {
            for (IDatatype dd : dt.getValues()) {
                if (dd.isXMLLiteral() && dd.getLabel().startsWith("http://")) {
                    // try an URI
                    dd = DatatypeMap.newResource(dd.getLabel());

                }
                list.add(p.getNode(dd));
            }
        } else {
            list.add(p.getNode(dt));
        }
        return list;
    }

}
