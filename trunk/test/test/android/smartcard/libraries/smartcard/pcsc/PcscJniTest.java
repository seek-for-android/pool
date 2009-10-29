/*
 * Copyright 2009 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.smartcard.libraries.smartcard.pcsc;

import junit.framework.TestCase;

import android.smartcard.libraries.smartcard.pcsc.PcscException;
import android.smartcard.libraries.smartcard.pcsc.PcscJni;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Disposition;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Protocol;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Scope;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.ShareMode;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Status;

/**
 * JUnit tests for the class 'android.smartcard.libraries.smartcard.pcsc.PcscJni'.
 */
public class PcscJniTest extends TestCase
{
    long _context;
    String _reader;
    long _hCard;
    int _protocol;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        _context = PcscJni.establishContext(Scope.User);
        _reader = PcscJni.listReaders(_context, null)[0];
    }
    
    protected void tearDown() throws Exception
    {
        disconnect();
        PcscJni.releaseContext(_context);
        super.tearDown();
    }

    private void connect() throws PcscException
    {
        int[] protocol = new int[1];
        protocol[0] = Protocol.T0 | Protocol.T1;
        _hCard = PcscJni.connect(_context, _reader, ShareMode.Shared, protocol);
        _protocol = protocol[0];
    }
    
    private void disconnect() throws PcscException
    {
        if (_hCard != 0)
        {
            try
            {
                PcscJni.disconnect(_hCard, Disposition.Leave);
            }
            catch (PcscException ignore) {}
            _hCard = 0;
        }
    }
    
    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.getLoadError'
     */
    public void testGetLoadError() throws PcscException
    {
        // Case 1
        assertNull(PcscJni.getLoadError());
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.isLoaded'
     */
    public void testIsLoaded() throws PcscException
    {
        // Case 1
        assertTrue(PcscJni.isLoaded());
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.beginTransaction(long)'
     */
    public void testBeginTransaction() throws PcscException
    {
        // Case 1
        connect();
        PcscJni.beginTransaction(_hCard);
        PcscJni.endTransaction(_hCard, Disposition.Leave);
        
        // Case 2
        try
        {
            PcscJni.beginTransaction(_context);
            fail("beginTransaction() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.cancel(long)'
     */
    public void testCancel() throws PcscException
    {
        // Case 1
        PcscJni.cancel(_context);
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.connect(long, String, int, int[])'
     */
    public void testConnect() throws PcscException
    {
        // Case 1
        int[] protocol = new int[] { Protocol.T0 | Protocol.T1 };
        long hCard = PcscJni.connect(_context, _reader, ShareMode.Shared, protocol);
        assertTrue(hCard != 0);
        PcscJni.disconnect(hCard, Disposition.Leave);

        // Case 2
        try
        {
            PcscJni.connect(0, _reader, ShareMode.Shared, protocol);
            fail("connect() did not raise an exception for illegal context");
        }
        catch (PcscException expected) {}
        
        // Case 3
        try
        {
            PcscJni.connect(_context, null, ShareMode.Shared, protocol);
            fail("connect() did not raise an exception for illegal reader");
        }
        catch (PcscException expected) {}
        
        // Case 4
        try
        {
            PcscJni.connect(_context, "4711", ShareMode.Shared, protocol);
            fail("connect() did not raise an exception for illegal reader");
        }
        catch (PcscException expected) {}
        
        // Case 5
        try
        {
            PcscJni.connect(_context, _reader, 0x4711, protocol);
            fail("connect() did not raise an exception for illegal share mode");
        }
        catch (PcscException expected) {}
        
        // Case 6
        try
        {
            PcscJni.connect(_context, _reader, ShareMode.Shared, null);
            fail("connect() did not raise an exception for illegal protocol");
        }
        catch (PcscException expected) {}
        
        // Case 7
        try
        {
            PcscJni.connect(_context, _reader, ShareMode.Shared, new int[0]);
            fail("connect() did not raise an exception for illegal protocol");
        }
        catch (PcscException expected) {}
        
        // Case 8
        try
        {
            PcscJni.connect(_context, _reader, ShareMode.Shared, new int[] { 0 });
            fail("connect() did not raise an exception for illegal protocol");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.control(long, int, Bytes)'
     */
    public void testControl() throws PcscException
    {
        int controlCode = 0x42000D48;
        // Case 1
        connect();
        byte[] response = PcscJni.control(_hCard, controlCode, null);
        assertNotNull(response);
        
        // Case 2
        response = PcscJni.control(_hCard, controlCode, new byte[] { 0x00 });
        assertNotNull(response);
        
        // Case 3
        try
        {
            PcscJni.control(_context, controlCode, null);
            fail("control() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
        
        // Case 4
        try
        {
            PcscJni.control(_hCard, 0, null);
            fail("control() did not raise an exception for illegal control code");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.disconnect(long, int)'
     */
    public void testDisconnect() throws PcscException
    {
        // Case 1
        connect();
        PcscJni.disconnect(_hCard, Disposition.Leave);
        _hCard = 0;

        // Case 2
        connect();
        try
        {
            PcscJni.disconnect(_context, Disposition.Leave);
            fail("disconnect() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.endTransaction(long, int)'
     */
    public void testEndTransaction() throws PcscException
    {
        // Case 1
        connect();
        PcscJni.beginTransaction(_hCard);
        try
        {
            PcscJni.endTransaction(_context, Disposition.Leave);
            fail("endTransaction() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
        
        // Case 2
        PcscJni.endTransaction(_hCard, Disposition.Leave);
        
        // Case 3
        try
        {
            PcscJni.endTransaction(_hCard, Disposition.Leave);
            // fail("endTransaction() did not raise an exception for illegal cardState");
        }
        catch (PcscException expected) {}    
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.establishContext(int)'
     */
    public void testEstablishContext() throws PcscException
    {
        // Case 1
        long context = PcscJni.establishContext(Scope.System);
        assertTrue(context != 0);
        PcscJni.releaseContext(context);
        
        // Case 2
        try
        {
            PcscJni.establishContext(0x4711);
            fail("establishContext() did not raise an exception for illegal scope");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.getAttrib(long, int)'
     */
    public void testGetAttrib() throws PcscException
    {
        // Case 1
        connect();
        byte[] attr = PcscJni.getAttrib(_hCard, 0x010100);
        assertTrue(attr.length > 0);
        
        // Case 2
        try
        {
            PcscJni.getAttrib(_context, 0x010100);
            fail("getAttrib() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
        
        // Case 3
        try
        {
            PcscJni.getAttrib(_hCard, 0);
            fail("getAttrib() did not raise an exception for illegal identifier");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.getStatus(long, int, String[], int[], String[])'
     */
    public void testGetStatus() throws PcscException
    {
        // Case 1
    	String[] readerNames = PcscJni.listReaders(_context, null);
        int[] currentStatus = new int[readerNames.length];
        int[] eventStatus = new int[readerNames.length];
        assertTrue(PcscJni.getStatus(_context, 0, readerNames, currentStatus, eventStatus));
        assertTrue((eventStatus[0] & Status.Present) == Status.Present);

        // Case 2
        assertFalse(PcscJni.getStatus(_context, 0, readerNames, eventStatus, eventStatus));
        assertTrue((eventStatus[0] & Status.Present) == Status.Present);

    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.getStatusChange(long, int, String, int[])'
     */
    public void testGetStatusChange() throws PcscException
    {
        // Case 1
        int[] status = new int[] { 0, 0 };
        byte[] atr = PcscJni.getStatusChange(_context, 0, _reader, status);
        assertTrue(status[0] != 0);
        assertTrue(status[1] != 0);
        assertTrue(atr.length > 0);

        // Case 2
        int currentState = status[0];
        status[1] = 1;
        atr = PcscJni.getStatusChange(_context, 1, _reader, status);
        assertEquals(currentState, status[0]);
        assertEquals(0, status[1]);
        assertNull(atr);

        // Case 3
        status[0] = 0;
        status[1] = 0;
        atr = PcscJni.getStatusChange(_context, -1, _reader, status);
        assertTrue(status[0] != 0);
        assertTrue(status[1] != 0);
        assertTrue(atr.length > 0);

        // Case 4
        status[0] = 0;
        try
        {
            PcscJni.getStatusChange(0, 0, _reader, status);
            fail("getStatusChange() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}

        // Case 5
        try
        {
            PcscJni.getStatusChange(_context, 0, "4711", status);
            fail("getStatusChange() did not raise an exception for illegal reader");
        }
        catch (PcscException expected) {}

        // Case 6
        try
        {
            PcscJni.getStatusChange(_context, 0, null, status);
            fail("getStatusChange() did not raise an exception for illegal reader");
        }
        catch (PcscException expected) {}

        // Case 7
        try
        {
            PcscJni.getStatusChange(_context, 0, _reader, new int[1]);
            fail("getStatusChange() did not raise an exception for illegal status");
        }
        catch (PcscException expected) {}

        // Case 8
        try
        {
            PcscJni.getStatusChange(_context, 0, _reader, null);
            fail("getStatusChange() did not raise an exception for illegal status");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.listReaders(long, String[])'
     */
    public void testListReaders() throws PcscException
    {
        // Case 1
        String[] readers = PcscJni.listReaders(_context, null);
        assertTrue(readers.length > 0);

        // Case 2
        readers = PcscJni.listReaders(_context, new String[0]);
        assertTrue(readers.length > 0);

        // Case 3
        readers = PcscJni.listReaders(_context, new String[] {""});
        assertTrue(readers.length > 0);

        // Case 4
        readers = PcscJni.listReaders(_context, new String[] {"4711"});
        assertTrue(readers.length > 0);

        // Case 5
        connect();
        try
        {
            PcscJni.listReaders(_hCard, null);
            fail("listReaders() did not raise an exception for illegal context");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.reconnect(long, int, int, int[])'
     */
    public void testReconnect() throws PcscException
    {
        // Case 1
        connect();
        int[] protocol = new int[] { Protocol.T0 | Protocol.T1 };
        PcscJni.reconnect(_hCard, ShareMode.Shared, Disposition.Leave, protocol);

        // Case 2
        try
        {
            PcscJni.reconnect(_context, ShareMode.Shared, Disposition.Leave, protocol);
            fail("reconnect() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}

        // Case 3
        try
        {
            PcscJni.reconnect(_hCard, 0x4711, Disposition.Leave, protocol);
            fail("reconnect() did not raise an exception for illegal share mode");
        }
        catch (PcscException expected) {}

        // Case 4
        try
        {
            PcscJni.reconnect(_hCard, ShareMode.Shared, Disposition.Leave, null);
            fail("reconnect() did not raise an exception for illegal protocol");
        }
        catch (PcscException expected) {}

        // Case 5
        try
        {
            PcscJni.reconnect(_hCard, ShareMode.Shared, Disposition.Leave, new int[0]);
            fail("reconnect() did not raise an exception for illegal protocol");
        }
        catch (PcscException expected) {}

        // Case 6
        try
        {
            PcscJni.reconnect(_hCard, ShareMode.Shared, Disposition.Leave, new int[] { 0 });
            fail("reconnect() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.releaseContext(long)'
     */
    public void testReleaseContext() throws PcscException
    {
        // Case 1
        long context = PcscJni.establishContext(Scope.System);
        assertTrue(context != 0);
        PcscJni.releaseContext(context);
        
        // Case 2
        try
        {
            PcscJni.releaseContext(0);
            fail("releaseContext() did not raise an exception for illegal context");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.status(long, int[])'
     */
    public void testStatus() throws PcscException
    {
        // Case 1
        connect();
        int[] status1 = new int[1];
        int[] protocol1 = new int[1];
        byte[] atr = PcscJni.status(_hCard, status1, protocol1);
        assertTrue(Status.Specific == status1[0] || Status.Negotiable == status1[0]);
        assertEquals(Protocol.T0, protocol1[0]);
        assertTrue(atr.length > 0);
        
        // Case 2
        atr = PcscJni.status(_hCard, status1, null);
        assertTrue(Status.Specific == status1[0] || Status.Negotiable == status1[0]);
        assertTrue(atr.length > 0);

        // Case 3
        atr = PcscJni.status(_hCard, null, protocol1);
        assertEquals(Protocol.T0, protocol1[0]);
        assertTrue(atr.length > 0);

        // Case 4
        int[] status0 = new int[0];
        int[] protocol0 = new int[0];
        atr = PcscJni.status(_hCard, status0, protocol0);
        assertTrue(atr.length > 0);

        // Case 5
        atr = PcscJni.status(_hCard, null, null);
        assertTrue(atr.length > 0);

        // Case 6
        try
        {
            PcscJni.status(_context, status1, protocol1);
            fail("status() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
    }

    /*
     * Test method for 'android.smartcard.libraries.smartcard.pcsc.PcscJni.transmit(long, int, Bytes)'
     */
    public void testTransmit() throws PcscException
    {
        // Case 1
        connect();
        byte[] response = PcscJni.transmit(_hCard, _protocol, new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x00 });
        assertTrue(response.length > 0);
        
        // Case 2
        try
        {
            PcscJni.transmit(_context, _protocol, new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x00 });
            fail("transmit() did not raise an exception for illegal handle");
        }
        catch (PcscException expected) {}
        
        // Case 3
        try
        {
            PcscJni.transmit(_hCard, 0x4711, new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x00 });
            fail("transmit() did not raise an exception for illegal protocol");
        }
        catch (PcscException expected) {}
        
        // Case 4
        try
        {
            PcscJni.transmit(_hCard, _protocol, new byte[] { 0x00, (byte) 0xA4, 0x04 });
            fail("transmit() did not raise an exception for illegal command");
        }
        catch (PcscException expected) {}
        
        // Case 5
        try
        {
            PcscJni.transmit(_hCard, _protocol, null);
            fail("transmit() did not raise an exception for illegal command");
        }
        catch (PcscException expected) {}
    }
}
