package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manage a table of list of properties for each node
 * Node ni -> (p1, .. pn)
 *
 * Use case:
 * ?x rdfs:label "Antibes"@fr . ?x ?q ?v
 * When we enumerate ?x ?q ?v we can focus on the properties of ?x=xi
 * instead of enumerating all properties.
 * DPbedia has more than 10.000 properties !
 *
 * TODO: Consider Updates
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class NodeManager {

    private Graph graph;
    private HashMap<Node, List<Node>> table;
    // in some case content is obsolete
    private boolean active = true;
    // safety to switch off
    private boolean available = true;
    private int count = 0;
    private int index =0;
    private boolean debug = false;

    NodeManager(Graph g, int index) {
        graph = g;
        table = new HashMap<>();
        this.index = index;
    }

    int size() {
        return table.size();
    }

    int count() {
        return count;
    }

    void clear() {
        table.clear();
        count = 0;
    }

    void desactivate() {
        clear();
        setActive(false);
    }

    void activate() {
        clear();
        setActive(true);
    }

    void add(Node node, Node predicate) {
        if (isEffective()) {
            List<Node> list = table.get(node);
            if (list == null) {
                list = new ArrayList<Node>();
                table.put(node, list);
            }
            list.add(predicate);
            count++;
        }
    }

    Iterable<Node> getPredicates(Node node) {
        if (isEffective()) {
            if (debug) {
                System.out.println("NM true: " + index + " " + node + " " + getPredicateList(node));
            }
            return getPredicateList(node);
        } 
        // draft: 
//        else if (index != 0 && ! graph.isIndex()) {
//            graph.getIndex(index).indexNodeManager();
//            if (debug) {
//                System.out.println("NM true: " + index + " " + node + " " + getPredicateList(node));
//            }
//            return getPredicateList(node);
//        } 
        else {
            if (debug) {
                System.out.println("NM false: " + index + " " + node + " " + graph.getSortedProperties());
            }
            return graph.getSortedProperties();
        }
    }

    List getPredicateList(Node node) {
        List<Node> list = table.get(node);
        if (list == null) {
            list = new ArrayList(0);
            table.put(node, list);
        }
        return list;
    }

    public boolean isEffective() {
        return active && available;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active ;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

     /**
     * @return the available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * @param available the available to set
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }


    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
