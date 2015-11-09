/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 *
 * @author macina
 */
public class CallableGetBasicGraphPattern implements Callable<Mappings>{

    private final Logger logger = Logger.getLogger(CallableGetBasicGraphPattern.class);
    private Producer producer = null;
    private Node graphNode = null; 
    private List<Node> from = null;
    private Exp exp = null;
    private Environment environment = null;
    
    
    public CallableGetBasicGraphPattern(Producer producer, Node graphNode, List<Node> from, Exp exp, Environment environment){
        this.producer = producer;
        this.graphNode = graphNode;
        this.from = from;
        this.exp = exp;
        this.environment = environment;
        
    }
    
    @Override
    public Mappings call() throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();
        logger.info("BGP in CALL : "+exp);
        Mappings mappings =  producer.getMappings(graphNode, from, exp, environment);
        logger.info("resutls: "+mappings.size());
        sw.stop();
        logger.info("Finished CallableGetBGP in "+sw.getTime()+" ms.");
        return mappings;
    }
    
}
