package org.simalliance.openmobileapi.service.terminals.SamplePluginTerminal;

/**
 * This mock class implements a minimum subset of the original class 
 * javacard.framework.Applet
 */

public abstract class Applet {

	public static final int INX_CLA = 0;
	public static final int INX_INS = 1;
	public static final int INX_P1  = 2;
	public static final int INX_P2  = 3;
	public static final int INX_LC  = 4;
	public static final int OFFSET_CDATA = 5;
	
	protected boolean isSelect;
	
    public boolean isSelect() { return isSelect; }
	abstract public byte[] process(byte[] command) throws Throwable;
	
	/**
	 * sets the isSelect flag indicating that the current C-APDU
	 * is a SELECT command.
	 * The isSelect flag can be queried by the selectingApplet() method
	 */
	public void mockSetSelect(boolean isSelect) {
		this.isSelect = isSelect;
	} // mockSetSelect
	
} // class
