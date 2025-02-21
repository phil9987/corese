package fr.inria.corese.core.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.tool.MetaIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class ProducerImplNode {
    
    ProducerImpl p;
    Graph graph;

    ProducerImplNode(ProducerImpl p) {
        this.p = p;
        graph = p.getGraph();
    }
    
    public Iterable<Node> getNodeIterator(Node gNode, List<Node> from, Edge edge,
            Environment env, List<Regex> exp, int index) {

        Node node = edge.getNode(index);

        if (node.isConstant()) {
            // return constant
            Node nn = graph.copy(node);
            ArrayList<Node> list = new ArrayList<>(1);
            list.add(nn);
            return list;
        } else if (gNode == null) {
            // default graph nodes
            if (from.size() > 0) {
                return getNodes(gNode, from, env);
            } else {
                return graph.getAllNodeIterator();
            }
        } // named graph nodes
        else if (gNode.isConstant()) {
            // return nodes of this named graph
            node = graph.getGraphNode(gNode.getLabel());
            if (node != null) {
                return graph.getNodeGraphIterator(node);
            }
        } 
        else if (env.isBound(gNode)) {
            // return nodes of this named graph
            node = env.getNode(gNode);
            // return nodes of this named graph
            node = graph.getGraphNode(node.getLabel());
            if (node != null) {
                return graph.getNodeGraphIterator(node);
            }
        }else if (from.size() > 0) {
            // return nodes of from named graph
            return getNodes(gNode, from, env);
        } else {
            // all nodes with named graph
            return graph.getNodeGraphIterator();
        }

        return new ArrayList<Node>(0);
    }

    /**
     * Enumerate nodes from graphs in the list gNode == null : from gNode !=
     * null : from named
     */
    Iterable<Node> getNodes(Node gNode, List<Node> from, Environment env) {
        MetaIterator<Node> meta = new MetaIterator<>();
        for (Node gn : p.getGraphNodes(gNode, from, env)) {
            meta.next(graph.getNodeGraphIterator(gn));
        }
        if (meta.isEmpty()) {
            return new ArrayList<Node>(0);
        }
        if (gNode == null) {
            // eliminate duplicates
            return getNodes(meta);
        }
        return meta;
    }

    /**
     * Use case:
     *
     * from <g1>
     * from <g2>
     * ?x :p{*} ?y
     *
     * enumerate nodes from g1 and g2 and eliminate duplicates
     */
    Iterable<Node> getNodes(final Iterable<Node> iter) {

        return () -> {
            final HashMap<Node, Node> table = new HashMap<Node, Node>();
            final Iterator<Node> it = iter.iterator();
            
            return new Iterator<Node>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }
                
                @Override
                public Node next() {
                    while (hasNext()) {
                        Node node = it.next();
                        if (node == null) {
                            return null;
                        }
                        if (!table.containsKey(node.getNode())) {
                            table.put(node.getNode(), node.getNode());
                            return node;
                        }
                    }
                    return null;
                }
                
                @Override
                public void remove() {
                }
            };
        };
    }

}
