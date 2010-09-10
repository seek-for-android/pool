/*
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * %W% %E%
 */

package com.sun.javacard.clientsamples.purseclient;

import java.rmi.*;
import javacard.framework.*;

import com.sun.javacard.javax.smartcard.rmiclient.*;
import com.sun.javacard.ocfrmiclientimpl.*;

import opencard.core.service.*;

import com.sun.javacard.samples.RMIDemo.Purse;

import java.util.ResourceBundle;

public class PurseClient {
    
    private static final byte[] RMI_DEMO_AID = {
	(byte)0xa0, (byte)0x00, (byte)0x00, 
	(byte)0x00, (byte)0x62, (byte)0x03, 
	(byte)0x01, (byte)0xc, (byte)0x8,
	(byte)0x01
    };
    
    public static void main(String[] argv) throws RemoteException{
        /*
         * The following line shows how to point the OCF to the directory
         * containing your opencard.properties file.
         * This may be useful if the file is not located in your current
         * directory or if the currect directory is not known
         * (for example, when you run the client in a debugger)
         */
        //System.getProperties().setProperty("user.dir", "H:/ws/javacard_refimpl_victor/cjdk/samples");
        
        ResourceBundle msg
        = ResourceBundle.getBundle("com/sun/javacard/clientsamples/purseclient/MessagesBundle");
        
        try {
            
            // initialize OCF
            SmartCard.start();
            
            // wait for a smartcard
            CardRequest cr = new CardRequest( CardRequest.NEWCARD, null, OCFCardAccessor.class );
            SmartCard myCard = SmartCard.waitForCard( cr );
            
            // obtain an OCFCardAccessor for Java Card RMI
            CardAccessor myCS = (CardAccessor) myCard.getCardService( OCFCardAccessor.class, true );
            
            // create a Java Card RMI instance
            JavaCardRMIConnect jcRMI = new JavaCardRMIConnect( myCS );
            
            // select the Java Card applet
            if(argv.length == 0) {
                jcRMI.selectApplet( RMI_DEMO_AID );
            }
            else {
                CardObjectFactory factory = new JCCardProxyFactory(myCS);
                jcRMI.selectApplet( RMI_DEMO_AID, factory );
            }
            
            // obtain the initial reference
            System.out.print(msg.getString("msg01")+" ");
            Purse myPurse = (Purse) jcRMI.getInitialReference();
            if(myPurse != null) {
                System.out.println(msg.getString("msg02"));
            }
            else {
                throw new Exception(msg.getString("msg03"));
            }
            
            // get the balance amount
            System.out.print(msg.getString("msg04"));
            short balance = myPurse.getBalance();
            System.out.println(msg.getString("msg05") + balance);  // prints 0
            
            System.out.println(msg.getString("msg06"));
            myPurse.credit((short)20);
            System.out.println(msg.getString("msg07"));
            myPurse.debit((short)15);
            
            System.out.print(msg.getString("msg08"));
            balance = myPurse.getBalance();
            System.out.println(msg.getString("msg05") + balance);  // prints 5
            
            System.out.println(msg.getString("msg09"));
            myPurse.setAccountNumber(new byte[]{5,4,3,2,1});  // expecting OK
            
            System.out.print(msg.getString("msg10"));
            byte[] acct_number = myPurse.getAccountNumber();
            printArray(acct_number);  // prints 5 4 3 2 1
            
            System.out.println(msg.getString("msg11"));
            myPurse.setAccountNumber(new byte[]{6,7,8,9,10,11});
            
        }
        catch(UserException e) {
            System.out.println(msg.getString("msg12") + e.toString());
            System.out.println(msg.getString("msg13") + Integer.toHexString(0x00FFFF & e.getReason()));
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
    
    private static void printArray(byte[] arr) {
        for(int i=0; i<arr.length; ++i) System.out.print(" " + arr[i]);
        System.out.println();
    }
    
}



