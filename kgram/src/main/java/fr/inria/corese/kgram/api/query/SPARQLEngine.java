package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;

/**
 *
 * @author corby
 */
public interface SPARQLEngine {
        
    Mappings eval(Query q, Mapping m, Producer p);
    
    Mappings eval(Node gNode, Query q, Mapping m, Producer p);
        
    void getLinkedFunction(String uri);
    
}
