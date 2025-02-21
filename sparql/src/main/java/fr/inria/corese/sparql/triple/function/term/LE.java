package fr.inria.corese.sparql.triple.function.term;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.core.BinaryFunction;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class LE extends BinaryFunction {
        
    public LE(){
    }
 
    public LE(String name){
        super(name);
    }
    
    public LE(String name, Expression e1, Expression e2){
        super(name, e1, e2);
    }
         
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt1 = getExp1().eval(eval, b, env, p);
        IDatatype dt2 = getExp2().eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }
        return overload(eval, b, env, p, dt1, dt2, dt1.le(dt2));
    }
      
}
