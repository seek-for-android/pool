/**
 * This mock class implements a minimum subset of the original class 
 * javacard.framework.ISOException
 */
package mockcard;

public class MockCardException extends Throwable {

	private static final long serialVersionUID = 1L;
	
    static final int SW_APPLET_SELECT_FAILED           = 0x6999;
    static final int SW_BYTES_REMAINING_00             = 0x6100;
    static final int SW_CLA_NOT_SUPPORTED              = 0x6E00;
    static final int SW_COMMAND_CHAINING_NOT_SUPPORTED = 0x6884;
    static final int SW_COMMAND_NOT_ALLOWED            = 0x6986;
    static final int SW_CONDITIONS_NOT_SATISFIED       = 0x6985;
    static final int SW_CORRECT_LENGTH_00              = 0x6C00;
    static final int SW_DATA_INVALID                   = 0x6984;
    static final int SW_FILE_FULL                      = 0x6A84;
    static final int SW_FILE_INVALID                   = 0x6983;
    static final int SW_FILE_NOT_FOUND                 = 0x6A82; 
    static final int SW_FUNC_NOT_SUPPORTED             = 0x6A81;
    static final int SW_INCORRECT_P1P2                 = 0x6A86;
    static final int SW_INS_NOT_SUPPORTED              = 0x6D00;
    static final int SW_LAST_COMMAND_EXPECTED          = 0x6883;
    static final int SW_LOGICAL_CHANNEL_NOT_SUPPORTED  = 0x6881;
    static final int SW_NO_ERROR                       = 0x9000;
    static final int SW_RECORD_NOT_FOUND               = 0x6A83;
    static final int SW_SECURE_MESSAGING_NOT_SUPPORTED = 0x6882;
    static final int SW_SECURITY_STATUS_NOT_SATISFIED  = 0x6982;
    static final int SW_UNKNOWN                        = 0x6F00;
    static final int SW_WARNING_STATE_UNCHANGED        = 0x6200;
    static final int SW_WRONG_DATA                     = 0x6A80;
    static final int SW_WRONG_LENGTH                   = 0x6700;
    static final int SW_WRONG_P1P2                     = 0x6B00;	

	static protected byte[] swBytes = {0, 0};
    static MockCardException isoException = new MockCardException();
	
    public MockCardException(){ }

    public MockCardException(int sw){ 
    	MockCardException.swBytes[0] = (byte)(sw>>8);
    	MockCardException.swBytes[1] = (byte)sw;
    } // constructor

    /**
     * gets the SW for this exception as array of bytes
     * @return array of two bytes containing SW
     */
    public byte[] mockGetSWBytes() { return swBytes; }
    
} // class
