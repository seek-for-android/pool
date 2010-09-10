/*
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SecurePurseClient.java	1.18 03/06/20
 *
 */

package com.sun.javacard.clientsamples.securepurseclient;

import java.rmi.*;
import javacard.framework.*;

import com.sun.javacard.javax.smartcard.rmiclient.*;
import com.sun.javacard.ocfrmiclientimpl.*;
import opencard.core.service.*;

import com.sun.javacard.samples.SecureRMIDemo.Purse;

import java.util.ResourceBundle;


public class SecurePurseClient {
    
    private static final byte[] SECURE_RMI_DEMO_AID = { 
	(byte)0xa0, (byte)0x00, (byte)0x00, (byte)0x00, 
	(byte)0x62, (byte)0x03, (byte)0x01, (byte)0xc, 
	(byte)0xa, (byte)0x01 
    };
    
    private static final short PRINCIPAL_APP_PROVIDER_ID = 0x1234;
    private static final short PRINCIPAL_CARDHOLDER_ID = 0x4321;
    
    
    public static void main(java.lang.String[] argv) {
        
        /*
         * The following line shows how to point the OCF to the directory
         * containing your opencard.properties file.
         * This may be useful if the file is not located in your current
         * directory or if the currect directory is not known
         * (for example, when you run the client in a debugger)
         */
        //System.getProperties().setProperty("user.dir", "H:/ws/javacard_refimpl_victor/cjdk/samples");
        
        
        
        ResourceBundle msg
	    = ResourceBundle.getBundle("com/sun/javacard/clientsamples/securepurseclient/MessagesBundle");
        try {
            
            // initialize OCF
            SmartCard.start();
            
            CardRequest cr = new CardRequest( CardRequest.NEWCARD, 
					      null, 
					      SecureOCFCardAccessor.class );
            SmartCard myCard = SmartCard.waitForCard( cr );
            
            // obtain the customized Java Card RMI Card Service class: SecurePurseClientCardService
            SecureOCFCardAccessor myCS 
		= (SecureOCFCardAccessor) myCard.getCardService( SecureOCFCardAccessor.class, 
								 true );
            
            
            // create a Java Card RMI instance
            JavaCardRMIConnect jcRMI = new JavaCardRMIConnect( myCS );
            
            
            // select the Java Card applet
            if(argv.length == 0) {
                jcRMI.selectApplet( SECURE_RMI_DEMO_AID );
            }
            else {
                CardObjectFactory factory = new JCCardProxyFactory(myCS);
                jcRMI.selectApplet( SECURE_RMI_DEMO_AID, factory );
            }
            
            // give your PIN
            System.out.print(msg.getString("msg03"));
            if (! myCS.authenticateUser( PRINCIPAL_APP_PROVIDER_ID )){
                throw new RemoteException(msg.getString("msg04"));
            }
            System.out.println(msg.getString("msg05"));
            
            System.out.print(msg.getString("msg06"));
            Purse myPurse = (Purse) jcRMI.getInitialReference();
            if(myPurse != null) {
                System.out.println(msg.getString("msg07"));
            }
            else {
                throw new Exception(msg.getString("msg08"));
            }
            
            System.out.print(msg.getString("msg09"));
            short balance = myPurse.getBalance();
            System.out.println(msg.getString("msg10") + balance);
            
            System.out.println(msg.getString("msg11"));
            myPurse.credit((short)20);
            
            System.out.print(msg.getString("msg12"));
            balance = myPurse.getBalance();
            System.out.println(msg.getString("msg10") + balance);
            
            System.out.println(msg.getString("msg13"));
            myPurse.debit((short)15);
            
            
        }
        catch(UserException e) {
            System.out.println(msg.getString("msg14") + e);
            System.out.println(msg.getString("msg15") + 
			       Integer.toHexString(0x00FFFF & e.getReason()));
        }
        catch (Exception e){
            System.out.println(e);
        } 
	finally {
            try{
                SmartCard.shutdown();
            }
	    catch (Exception e){
                System.out.println(e);
            }
        }
    }
    
}


