package fr.inria.corese.sparql.triple.javacc1;

import java.util.Vector;

public class JavaccParseException extends ParseException {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;

	private Vector expectedToken;
	private String encouteredToken = "";
	private String message = "";

	public JavaccParseException(ParseException e) {
		super();
		this.expectedTokenSequences = e.expectedTokenSequences;
		this.currentToken = e.currentToken;
		this.tokenImage = e.tokenImage;
		this.message = e.getMessage();
		setExpectedToken();
		setEnccouteredToken();
	}
	
	public String getMessage() {
		return message;
	}
	
	public Vector getExpectedToken() {
		return expectedToken;
	}

	public String getEncouteredToken() {
		return encouteredToken;
	}
	
	private void setExpectedToken() {
		String expected = "";
		expectedToken = new Vector();
	    for (int i = 0; i < expectedTokenSequences.length; i++) {
	      for (int j = 0; j < expectedTokenSequences[i].length; j++) {
	        expected = tokenImage[expectedTokenSequences[i][j]];
	        expectedToken.add(expected);
	      }
	    }
	}
	
	private void setEnccouteredToken() {
		encouteredToken = currentToken.next.image;
	}
	
}
