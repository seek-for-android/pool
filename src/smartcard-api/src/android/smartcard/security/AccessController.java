/*
 * Copyright 2010 Giesecke & Devrient GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package android.smartcard.security;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.smartcard.CardException;
import android.smartcard.IChannel;
import android.smartcard.ISmartcardServiceCallback;
import android.smartcard.ITerminal;
import android.smartcard.SmartcardError;
import android.smartcard.Util;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.security.AccessControlException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

public class AccessController {

    protected AccessControlDB mAaccessControlDB = null;

    protected PackageManager mPackageManager = null;

    protected String ACCESS_CONTROLLER_TAG = "AccessController";

    public static final byte[] ACCESS_CONTROL_AID = new byte[] {
            (byte) 0xD2, (byte) 0x76, (byte) 0x00, (byte) 0x01, (byte) 0x18, (byte) 0xAA,
            (byte) 0xFF, (byte) 0xFF, (byte) 0x49, (byte) 0x10, (byte) 0x48, (byte) 0x89,
            (byte) 0x01
    };

    public AccessController(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    public byte[] getAccessControlAID() {
        return ACCESS_CONTROL_AID;
    }

    protected boolean checkAPKPCert(Certificate apCert, Certificate apkpCert) {

        if (apkpCert.equals(apCert)) {
            return true;
        }

        try {
            PublicKey apPublicKey = apCert.getPublicKey();
            apkpCert.verify(apPublicKey);
            return true;
        } catch (Exception e) {
            // APKP Certificate couldn't be verified
        }

        return false;
    }

    protected Certificate getAPCert() throws AccessControlException, CardException,
            CertificateException {
        byte[] apCertBytes = mAaccessControlDB.readAPCertificate();
        if (apCertBytes.length == 0) {
            return null;
        }
        return decodeCertificate(apCertBytes);
    }

    protected Certificate getAPKPCert(IChannel channel, String callerPackageName)
            throws CertificateException, NoSuchAlgorithmException, AccessControlException,
            CardException {

        List<PackageInfo> pkgInfoList = mPackageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_GIDS
                        | PackageManager.GET_SIGNATURES);

        PackageInfo foundPkgInfo = null;
        for (PackageInfo pkgInfo : pkgInfoList) {
            if (callerPackageName.equals(pkgInfo.packageName)) {
                foundPkgInfo = pkgInfo;
                break;
            }
        }
        if (foundPkgInfo == null) {
            return null;
        }

        for (Signature signature : foundPkgInfo.signatures) {
            return decodeCertificate(signature.toByteArray());
        }

        return null;
    }

    public static Certificate decodeCertificate(byte[] certData) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certFactory
                .generateCertificate(new ByteArrayInputStream(certData));
        return cert;
    }

    public void checkCommand(IChannel channel, byte[] command) {

        ChannelAccess ca = channel.getChannelAccess();
        String msg = "Access denied: command not allowed: ";
        String reason = ca.getReason();
        if (reason.length() > 0) {
            reason = ": " + reason;
        }
        if (ca == null) {
            
            throw new AccessControlException(msg + "ChannelAccess not defined");
        }
        if (ca.isUnlimitedAccess()) {
            
            return;
        }
        if (ca.isNoAccess()) {
            
            throw new AccessControlException(msg + "No access" + reason);
        }
        if (ca.isUseAccessConditions()) {
            AccessCondition[] accessConditions = ca.getAccessConditions();
            if (accessConditions == null || accessConditions.length == 0) {
                
                throw new AccessControlException(msg + "ACL not available" + reason);
            }
            for (AccessCondition ac : accessConditions) {
                if (CommandApdu.compareHeaders(command, ac.getMask(), ac.getApdu())) {
                    
                    return;
                }
            }
            

            throw new AccessControlException(msg + "ACL does not match" + reason);
        }

        
        throw new AccessControlException(msg + "Channel state unknown" + reason);
    }

    public ChannelAccess enableAccessConditions(ITerminal terminal, byte[] aid,
            String callerPackageName, ISmartcardServiceCallback callback, SmartcardError error) {
        ChannelAccess channelAccess = new ChannelAccess();
        IChannel channel = null;
        long hChannel = 0;
        try {
            

            
            hChannel = terminal.openLogicalChannel(getAccessControlAID(), callback);
            

        } catch (Exception e) {
            String msg = e.toString();
            msg = "Access Control Applet couldn't be selected: " + msg;
            
            if (e instanceof NoSuchElementException) {
                // Access Control Applet is not available => grant full access
                
                channelAccess.setUnlimitedAccess(true);
            } else {
                // no free channel available or another error => No access
                closeChannel(terminal.getChannel(hChannel));
                
                if (e instanceof MissingResourceException)
                    throw new MissingResourceException(msg, "", "");
                throw new AccessControlException(msg);
            }

            
            closeChannel(terminal.getChannel(hChannel));
            

            return channelAccess;
        }

        try {
            
            channel = terminal.getChannel(hChannel);
            channelAccess = internalEnableAccessConditions(channel, aid, callerPackageName);

        } catch (Exception e) {
            String msg = e.toString();
            error.setError(AccessControlException.class, msg);
            
            channelAccess.setNoAccess(true, msg);
            closeChannel(terminal.getChannel(hChannel));
            throw new AccessControlException(msg);
        }

        
        closeChannel(terminal.getChannel(hChannel));
        

        return channelAccess;
    }

    protected void closeChannel(IChannel channel) {
        try {
            if (channel != null && channel.getChannelNumber() != 0) {
                channel.close();
            }
        } catch (CardException e) {
        }
    }

    protected ChannelAccess internalEnableAccessConditions(IChannel channel, byte[] aid,
            String callerPackageName) throws NoSuchAlgorithmException, AccessControlException,
            CardException, CertificateException {

        ChannelAccess channelAccess = new ChannelAccess();

        if (channel == null) {
            throw new AccessControlException("Channel must be specified");
        }
        if (callerPackageName == null || callerPackageName.length() == 0) {
            throw new AccessControlException("CallerPackageName must be specified");
        }
        if (aid == null || aid.length == 0) {
            throw new AccessControlException("AID must be specified");
        }
        if (aid.length < 5 || aid.length > 16) {
            throw new AccessControlException("AID has an invalid length");
        }

        try {
            mAaccessControlDB = new AccessControlDB(channel);
            
            mAaccessControlDB.selectAID(aid);
            
        } catch (Throwable exp) {

            

            throw new AccessControlException("AID not found");
        }

        

        // AID found => check APKP certificate with AP certificate
        
        Certificate apkpCert = getAPKPCert(channel, callerPackageName);
        
        // APKP certificates must be available => otherwise Exception
        if (apkpCert == null) {
            throw new AccessControlException("APKP Certificate is invalid");
        }

        /**
         * Note: This loop is needed as workaround for a bug in Android 2.3.
         * After a failed certificate verification in a previous step the
         * MessageDigest.getInstance("SHA") call will fail with the
         * AlgorithmNotSupported exception. But a second try will normally
         * succeed.
         */
        MessageDigest md = null;
        for (int i = 0; i < 10; i++) {
            try {
                md = MessageDigest.getInstance("SHA");
                break;
            } catch (Exception e) {
            }
        }
        if (md == null) {
            throw new AccessControlException("Hash can not be computed");
        }

        
        byte[] apkCertHash = md.digest(apkpCert.getEncoded());
        

        
        AccessCondition[] accessConditions = mAaccessControlDB.readAPKACRecord(apkCertHash);
        
        if (accessConditions == null || accessConditions.length == 0) {
            // check if there exist an ACL for all APKs
            
            accessConditions = mAaccessControlDB.readAPKACRecord(getEmptyHash());
            
        }
        if (accessConditions == null || accessConditions.length == 0) {
            // => ACL not available
            
            throw new AccessControlException("ACL not available");
        } else {
            // => ACL available
            
            Certificate apCert = getAPCert();
            
            if (apCert == null) {
                // => AP certificate is not available
                
                channelAccess.setUseAccessConditions(true);
                channelAccess.setAccessConditions(accessConditions);
                return channelAccess;
            } else {
                // => AP certificate is available
                
                if (checkAPKPCert(apCert, apkpCert)) {

                    
                    // => APK Certificate verification successful
                    
                    channelAccess.setUseAccessConditions(true);
                    channelAccess.setAccessConditions(accessConditions);
                    return channelAccess;
                } else {

                    
                    // => APK certificate verification not successful
                    
                    throw new AccessControlException("APK Certificate verification not successful");
                }
            }
        }
    }

    protected byte[] getEmptyHash() {
        return new byte[] {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
    }

}
