package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.CoreseDatatype.getGenericDatatype;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class CoreseMap extends CoreseExtension {

    private static final IDatatype dt = getGenericDatatype(IDatatype.MAP_DATATYPE);
    private static int count = 0;
    private static final String SEED = "_m_";  
    TreeDatatype map;
    

    public CoreseMap() {
        super(SEED + count++);
        map = new TreeDatatype();
    }
    
    @Override
    public IDatatype getDatatype() {
        return dt;
    }
      
    @Override
    public String getContent() {
        return String.format("[Map: size=%s]", map.size());
    }
    
    @Override
    public boolean isExtension() {
        return true;
    }
       
    @Override
    public int size() {
        return map.size();
    }
    
    @Override
    public boolean isMap() {
        return true;
    }
    
    @Override
    public Map getObject() {
        return map;
    }
    
    @Override
    public Map<IDatatype, IDatatype> getMap() {
        return map;
    }
    
    public void incr(IDatatype key) {
        IDatatype num = get(key);
        if (num == null) {
            num = DatatypeMap.newInstance(0);
        }
        set(key, num.intValue() + 1);
    }
    
    public void set(IDatatype key, int val) {
        set(key, DatatypeMap.newInstance(val));
    }

    public void set(String key, IDatatype val) {
        set(DatatypeMap.newResource(key), val);
    }
    
    @Override
    public IDatatype set(IDatatype key, IDatatype value) {
        map.put(key, value);
        return value;
    }
    
    @Override
    public IDatatype member(IDatatype key) {
        return  map.containsKey(key) ? TRUE : FALSE;
    }
    
    @Override
    public IDatatype has(IDatatype key) {
        return map.containsKey(key) ? TRUE : FALSE;
    }
    
    @Override
    public IDatatype get(IDatatype key) {
        return map.get(key);
    }
    
    public IDatatype keys() {
        return DatatypeMap.createList(map.keySet());
    }
    
    public IDatatype values() {
        return DatatypeMap.createList(map.values());
    } 
    
    @Override
    public Iterator<IDatatype> iterator() {
        return getValueList().iterator();
    }
    
    @Override
    public List<IDatatype> getValueList() { 
        ArrayList<IDatatype> list = new ArrayList<>();
        for (IDatatype key : map.keySet()) {
            IDatatype pair = DatatypeMap.newList(key, map.get(key));
            list.add(pair);
        }
        return list;
    } 
    
    @Override
    public boolean isLoop() {
        return true;
    }
    
    @Override
    public Iterable<IDatatype> getLoop() {
        return getValueList();
    }
    
    class TreeDatatype extends TreeMap<IDatatype, IDatatype> {

        TreeDatatype() {
            super((IDatatype t, IDatatype t1) -> t.compareTo(t1));
        }
    }

    
}
