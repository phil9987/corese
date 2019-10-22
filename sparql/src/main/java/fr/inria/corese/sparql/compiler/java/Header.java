package fr.inria.corese.sparql.compiler.java;

import java.util.Date;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Header {
    static final String NL = System.getProperty("line.separator");
    
 static final String importList = 
              "import fr.inria.corese.sparql.api.IDatatype;\n"
            + "import fr.inria.corese.sparql.datatype.DatatypeMap;\n"
            + "import fr.inria.corese.sparql.triple.function.extension.*;\n" ; 
            //+ "import fr.inria.corese.extension.core.Core;\n";
 
 static final String comment  = 
         "/**\n"
         + " * Code generated by LDScript Java compiler for SPARQL extension functions \n"
         + " * Compiler is run by annotation: \n"
         + " * @compile <fr.inria.corese.extension.MyClass> \n"
         + " * @path    </home/me/src/> \n"
         + " * functions are called with: \n"
         + " * prefix java: <function://fr.inria.corese.extension.MyClass> \n"
         + " * java:fun(?x) \n"
         + " *\n"
         + " * Olivier Corby - Wimmics Inria I3S - %s \n"
         + " */";
 
 

    JavaCompiler jc;
    StringBuilder sb ;
    
    Header(JavaCompiler jc){
        this.jc = jc;
        sb = new StringBuilder();
    }
    
    void process(String pack, String name) {
        sb.append("package ").append(pack).append(";");
        nl();
        nl();
        sb.append(importList);      
        nl();
        nl();
        sb.append(String.format(comment, new Date()));
        nl();
        sb.append(String.format("public class %s extends Core { ", name));
        nl();
        nl();         
    }
    
    void nl(){
        sb.append(NL);
    }
    
    StringBuilder getStringBuilder(){
        return sb;
    }

}
