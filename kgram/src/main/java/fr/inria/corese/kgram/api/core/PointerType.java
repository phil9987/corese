package fr.inria.corese.kgram.api.core;

import static fr.inria.corese.kgram.api.core.ExpType.DT;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public enum PointerType {
           
        UNDEF("pointer"),
        MAPPINGS("mappings"),
        MAPPING("mapping"),
        GRAPH("graph"),
        NODE("node"),
        TRIPLE("triple"),
        PATH("path"),
        QUERY("query"),
        EXPRESSION("expression"),
        STATEMENT("statement"),
        DATASET("dataset"),
        PRODUCER("producer"),
        METADATA("metadata"),
        CONTEXT("context"),
        NSMANAGER("nsmanager"),
        VISITOR("visitor"),
        ; 
    
        private String name;
                
        private PointerType(String n) {
            name = DT+n;
        }
        
        public String getName() {
            return name;
        } 

}
