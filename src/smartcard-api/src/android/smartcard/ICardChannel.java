/*
 * Copyright (C) 2011, The Android Open Source Project
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
/*
 * Contributed by: Giesecke & Devrient GmbH.
 */

package android.smartcard;

/**
 * Abstraction of a basic or logical card channel, managed by a
 * <code>SmartcardClient</code> instance, connected to the smartcard service.
 */
public interface ICardChannel {

    /**
     * Closes this channel.
     * 
     * @throws CardException if closing the channel failed.
     */
    void close() throws CardException;

    /**
     * Returns <code>true</code> if this channel is closed, <code>false</code>
     * if this is open.
     * 
     * @return <code>true</code> if this channel is closed, <code>false</code>
     *         if this is open.
     */
    boolean isClosed();

    /**
     * Returns <code>true</code> if this is a logical channel,
     * <code>false</code> if this is a basic channel.
     * 
     * @return <code>true</code> if this is a logical channel,
     *         <code>false</code> if this is a basic channel.
     */
    boolean isLogicalChannel();

    /**
     * Transmits the specified command APDU and returns the response APDU.
     * MANAGE channel commands are not allowed. Applet selection commands are
     * not allowed if this is a logical channel.
     * 
     * @param command the command APDU to be transmitted.
     * @return the response APDU.
     * @throws NullPointerException if command is <code>null</code>.
     * @throws IllegalArgumentException if command is shorter than 4 bytes.
     * @throws IllegalStateException if the channel is in closed state.
     * @throws CardException if command transmission failed or the command is
     *             not allowed.
     */
    byte[] transmit(byte[] command) throws CardException;
}
