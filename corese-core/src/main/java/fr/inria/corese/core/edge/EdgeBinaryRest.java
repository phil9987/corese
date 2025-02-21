package fr.inria.corese.core.edge;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

/**
 * Edge entailed by a Rule
 * index and provenance
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeBinaryRest extends EdgeBinary {

    EdgeBinaryRest(Node subject, Node object) {
        super(subject, object);
    }
    
    public static EdgeBinaryRest create(Node source, Node subject, Node predicate, Node object){
        return new EdgeBinaryRest(subject, object);
    }

    @Override
    public Node getEdgeNode() {
        return subject.getTripleStore().getNode(Graph.REST_INDEX);
    }
}
