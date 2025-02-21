package fr.inria.corese.core.load;

public class LoadException extends Exception {
	
	private Exception ex;
	private Object object;
	private String path;
	
	public LoadException(Exception ee){
		this.set(ee);
	}
	
	private void set(Exception ee) {
		ex = ee;
	}

	LoadException(Exception ee, Object oo){
		this.set(ee);
		this.setObject(oo);
	}
        
        @Override
        public String getMessage(){
            return ex.getMessage();
        }
	
	public static LoadException create(Exception e){
		return new LoadException(e);
	}
	
	public static LoadException create(Exception e, String p){
		LoadException ee = new LoadException(e);
		ee.setPath(p);
		return ee;
	}
	
	public static LoadException create(Exception e, Object o){
		return new LoadException(e, o);
	}
	
	public static LoadException create(Exception e, Object o, String p){
		LoadException ee =  new LoadException(e, o);
		ee.setPath(p);
		return ee;
	}
	
        @Override
	public String toString(){
		if (ex == null) return super.toString();
		
		String str = ex.getClass().getName() + " ";
		if (ex.getMessage()!=null){
			str += ex.getMessage();
		}
		else {
			str += ex.toString();
		}
		if (getObject() != null){
			str += "\n" + getObject();
		}
		if (getPath() != null) {
			str += "\n" + getPath();
		}
                else {
                    str += "\n" + "unknown location";
                }
		return str;
	}

	public Exception getException() {
		return ex;
	}

	void setObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	public LoadException setPath(String path) {
		this.path = path;
                return this;
	}

	public String getPath() {
		return path;
	}
	
	
	

}
