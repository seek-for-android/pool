/*
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SecureOCFCardAccessorFactory.java	1.5 03/06/06
 */

package com.sun.javacard.clientsamples.securepurseclient;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardType;

import opencard.core.terminal.CardID;
import opencard.core.service.CardServiceScheduler;

import com.sun.javacard.ocfrmiclientimpl.*;

/**
 * The OCFCardAccessorFactory class creates the <CODE>OCFCardAccessor</CODE>
 * instance which is used by terminal client applications to initiate and conduct
 * a Java Card RMI based dialogue with the smart card. The methods in this class are
 * intended to be invoked by the OCF <CODE>CardServiceRegistry</CODE> class. Java Card
 * RMI Client applications should access the <CODE>SmartCard</CODE> class to obtain
 * instances of <CODE>OCFCardAccessor</CODE>.
 *
 *
 */
public class SecureOCFCardAccessorFactory extends CardServiceFactory {
    
    private static int NUMCARDSERVICES = 1;
    private Class services[];

    /** Creates new OCFCardAccessorFactory */
    public SecureOCFCardAccessorFactory() {
        services = new Class[ NUMCARDSERVICES ];
        services[0] = SecureOCFCardAccessor.class;
    }
    
/** This method examines the <CODE>CardID</CODE> object ( containing the ATR returned by the Card )
 * and checks if the card could be a Java Card.&nbsp;If so, this method returns a
 * <CODE>JavaCardType</CODE> object.
 * @param cid <CODE>CardID</CODE> received from a Card <CODE>Slot</CODE>.
 * @param scheduler <CODE>CardServiceScheduler</CODE> that can be used to communicate with
 * the card to determine its type.
 * @return A <CODE>JavaCardType</CODE> if the factory can instantiate services for this card.
 * <CODE>CardType.UNSUPPORTED</CODE> if the factory does not know the card.
 */    
    protected CardType getCardType(CardID cid, CardServiceScheduler scheduler){
        return new JavaCardType();
    }
    
/** If the input parameter is a <CODE>JavaCardCardType</CODE> object, this method returns an anumeration object
 * with the <CODE>OCFCardAccessor</CODE> object listed. Subclasses of this class
 * may add subclasses of <CODE>OCFCardAccessor</CODE> to this list by using the <CODE>add</CODE> method.
 * @return An <CODE>Enumeration</CODE> of <CODE>OCFCardAccessor</CODE> class objects
 * @param type The <CODE>CardType</CODE> of the smart card for which the enumeration is requested.
 */    
    protected Enumeration getClasses(CardType type){
        return new Enumerator( NUMCARDSERVICES );
    }
    
    class Enumerator implements java.util.Enumeration {
        
        int count = 0;
        
        public Enumerator( int numservices ){
            count = numservices;
        }
        
        public boolean hasMoreElements(){
            return count > 0;
        }
        
        public Object nextElement(){
            if (count ==0)
                throw new NoSuchElementException("SecureOCFCardAccessorFactory");
            return services[--count];
        }
        
    }

}
