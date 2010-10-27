/*
 * Copyright (C) 2010 Giesecke & Devrient GmbH
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

package com.android.stk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.android.internal.telephony.gsm.stk.AppInterface;
import com.android.internal.telephony.gsm.stk.StkLog;
import com.android.internal.telephony.gsm.stk.StkCmdMessage;
import com.android.internal.telephony.gsm.stk.StkResponseMessage;
import com.android.internal.telephony.gsm.stk.InterfaceTransportLevel.TransportProtocol;

import java.io.*;
import java.net.*;

public class SCWSProxy extends Service {
    private static AppInterface mStkService = null;
    private static ServerSocket mServerSocket = null;
    private static volatile Socket[] mSocket = {null, null};
    private static byte[][] mBuf = new byte[2][2048];
    private static int[] mPos = {0, 0};
    private static int[] mLen = {0, 0};

    private static class ServerThread extends Thread {
        private int channel;

        public ServerThread(int channel) {
            this.channel = channel;
        }

        public void run() {
            StkResponseMessage resMsg;

            if (mSocket[channel] == null) {
                // listen
                resMsg = new StkResponseMessage(
                        "d60b99010a82028281b8024" +
                        Integer.toHexString(channel + 1) + "00");
                mStkService.onCmdResponse(resMsg);

                try {
                    mSocket[channel] = mServerSocket.accept();
                } catch(IOException e) {
                    return;
                }

                // established
                resMsg = new StkResponseMessage(
                        "d60b99010a82028281b8028" +
                        Integer.toHexString(channel + 1) + "00");
                mStkService.onCmdResponse(resMsg);
            }

            // wait 'til some data is ready
            try {
                mLen[channel] =
                        mSocket[channel].getInputStream().read(mBuf[channel]);
            } catch(IOException e) {
                return;
            }

            // sanity check
            if (mLen[channel] <= 0)
                return;

            mPos[channel] = 0;
            int available = 0xff;
            if (mLen[channel] < available)
                available = mLen[channel];

            // data available
            resMsg = new StkResponseMessage(
                    "d60e99010982028281b8028" +
                    Integer.toHexString(channel + 1) + "00b701" +
                    Integer.toHexString(available + 0x100).substring(1));
            mStkService.onCmdResponse(resMsg);
        }
    }

    private static class ClientThread extends Thread {
        private int channel;

        public ClientThread(int channel) {
            this.channel = channel;
        }

        public void run() {
            if (mSocket[channel] == null)
                return;

            // wait 'til some data is ready
            try {
                mLen[channel] =
                        mSocket[channel].getInputStream().read(mBuf[channel]);
            } catch(IOException e) {
                return;
            }

            // sanity check
            if (mLen[channel] <= 0)
                return;

            mPos[channel] = 0;
            int available = 0xff;
            if (mLen[channel] < available)
                available = mLen[channel];

            // data available
            StkResponseMessage resMsg = new StkResponseMessage(
                    "d60e99010982028281b8028" +
                    Integer.toHexString(channel + 1) + "00b701" +
                    Integer.toHexString(available + 0x100).substring(1));
            mStkService.onCmdResponse(resMsg);
        }
    }

    public void onCreate() {
        mStkService = com.android.internal.telephony.gsm.stk.StkService
                .getInstance();
    }

    public void onStart(Intent intent, int StartId) {
        if (mStkService == null)
            return;

        Bundle args = intent.getExtras();
        if (args == null) {
            StkLog.d(this, "SESSION_END");

            if (mSocket[0] != null) {
                try {
                    mSocket[0].close();
                } catch(IOException e) {
                    return;
                }

                mSocket[0] = null;

                ServerThread serverThread = new ServerThread(0);
                serverThread.start();
            }

            if (mSocket[1] != null) {
                ClientThread clientThread = new ClientThread(1);
                clientThread.start();
            }

            return;
        }

        StkCmdMessage cmdMsg = (StkCmdMessage) args.getParcelable("Cmd");
        StkLog.d(this, cmdMsg.getCmdType().name());

        switch (cmdMsg.getCmdType()) {
            case OPEN_CHANNEL:
                int channel = cmdMsg.getChannelSettings().channel - 1;
if (cmdMsg.getChannelSettings().protocol ==
        TransportProtocol.TCP_SERVER) {
                if ((channel != 0) || (mServerSocket != null))
                    return;

                try {
                    mServerSocket = new ServerSocket(
                            cmdMsg.getChannelSettings().port);
                } catch(IOException e) {
                    return;
                }

                ServerThread serverThread = new ServerThread(channel);
                serverThread.start();
} else
if (cmdMsg.getChannelSettings().protocol == 
        TransportProtocol.TCP_CLIENT_REMOTE) {
                if ((channel != 1) || (mSocket[channel] != null))
                   return;

                try {
                    byte[] addr = cmdMsg.getChannelSettings().addr;
                    mSocket[channel] =
                            new Socket(InetAddress.getByAddress(addr),
                            cmdMsg.getChannelSettings().port);
                } catch(IOException e) {
                    return;
                }

                // established
                StkResponseMessage resMsg = new StkResponseMessage(
                        "d60b99010a82028281b8028" +
                        Integer.toHexString(channel + 1) + "00");
                mStkService.onCmdResponse(resMsg);

                ClientThread clientThread = new ClientThread(channel);
                clientThread.start();
} else
if (cmdMsg.getChannelSettings().protocol ==
        TransportProtocol.TCP_CLIENT_LOCAL) {
                if ((channel != 1) || (mSocket[channel] != null))
                   return;

                try {
                    byte[] addr = cmdMsg.getChannelSettings().addr;
                    mSocket[channel] =
                            new Socket(InetAddress.getByAddress(addr),
                            cmdMsg.getChannelSettings().port);
                } catch(IOException e) {
                    return;
                }

                // established
                StkResponseMessage resMsg = new StkResponseMessage(
                        "d60b99010a82028281b8028" +
                        Integer.toHexString(channel + 1) + "00");
                mStkService.onCmdResponse(resMsg);

                ClientThread clientThread = new ClientThread(channel);
                clientThread.start();
} else {
                return;
}
                break;
            case CLOSE_CHANNEL:
                channel = cmdMsg.getDataSettings().channel - 1;
                if (channel != 1)
                    return;

                if (mSocket[channel] == null)
                    return;

                try {
                    mSocket[channel].close();
                } catch(IOException e) {
                    return;
                }

                mSocket[channel] = null;

                // closed
                StkResponseMessage resMsg = new StkResponseMessage(
                        "d60b99010a82028281b8020" +
                        Integer.toHexString(channel + 1) + "00");
                mStkService.onCmdResponse(resMsg);
                break;
            case RECEIVE_DATA:
                channel = cmdMsg.getDataSettings().channel - 1;
                if ((channel < 0) || (channel > 1))
                    return;

                if (mLen[channel] == 0)
                    return;

                int requested = cmdMsg.getDataSettings().length;
                if (requested > mLen[channel])
                    requested = mLen[channel];

                mLen[channel] -= requested;
                int available = 0xff;
                if (mLen[channel] < available)
                    available = mLen[channel];

                byte[] data = new byte[requested];
                System.arraycopy(mBuf[channel], mPos[channel], data, 0,
                        requested);
                mPos[channel] += requested;

                resMsg = new StkResponseMessage(cmdMsg);
                resMsg.setChannelData(data, available);
                mStkService.onCmdResponse(resMsg);
                break;
            case SEND_DATA:
                channel = cmdMsg.getDataSettings().channel - 1;
                if ((channel < 0) || (channel > 1))
                    return;

                if (mSocket[channel] == null)
                    return;

                try {
                    mSocket[channel].getOutputStream()
                            .write(cmdMsg.getDataSettings().data);
                } catch(IOException e) {
                    return;
                }

                resMsg = new StkResponseMessage(cmdMsg);
                resMsg.setChannelData(null, 0xff);
                mStkService.onCmdResponse(resMsg);
                break;
            default:
                break;
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
