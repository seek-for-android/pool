package com.gieseckedevrient.android.servicelayertester.testrunners;

import java.io.IOException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEDiscovery;
import org.simalliance.openmobileapi.SERecognizerByAID;
import org.simalliance.openmobileapi.SEService;

import com.gieseckedevrient.android.servicelayertester.TestActivity;

public abstract class TestRunner extends Thread {
    /**
     * The SEService that will be used.
     */
    protected SEService mSeService;

    /**
     * The Channel that will be used.
     */
    protected Channel mChannel;

    /**
     * The calling activity.
     */
    protected TestActivity mActivity;

    /**
     * Initializes a new instance of the TestRunner class.
     *
     * @param seService The SEService to use.
     * @param activity The calling activity.
     */
    public TestRunner(SEService seService, TestActivity activity) {
        mSeService = seService;
        mActivity = activity;
    }

    /**
     * Opens a Channel to the SE which contains the specified AID.
     *
     * @param aid The AID of the desired applet.
     *
     * @return An open channel to the specified applet.
     *
     * @throws IllegalArgumentException If the specified aid is not correct.
     * @throws IOException Lower-level API exception.
     */
    public Channel openChannelToApplet(byte[] aid)
            throws IllegalArgumentException, IOException {
        SERecognizerByAID seRecognizerByAid = new SERecognizerByAID(aid);
        SEDiscovery seDiscovery = new SEDiscovery(
                mSeService, seRecognizerByAid);
        Reader reader = seDiscovery.getFirstMatch();
        if (reader == null) {
            return null;
        }

        return reader.openSession().openLogicalChannel(aid);
    }

    @Override
    public abstract void run();

    /**
     * Ends the test.
     */
    public void endTest() {
        if (mChannel != null && !mChannel.isClosed()) {
            mChannel.close();
        }
        mActivity.testEnded(true);
    }

    /**
     * Ends the test.
     */
    public void endTest(String errorMessage) {
        if (mChannel != null && !mChannel.isClosed()) {
            mChannel.close();
        }
        mActivity.logError(errorMessage);
        mActivity.testEnded(false);
    }

    /**
     * Ends the test.
     */
    public void endTest(Exception e) {
        if (mChannel != null && !mChannel.isClosed()) {
            mChannel.close();
        }
        e.printStackTrace();
        mActivity.logError(e.getMessage() + "\nSee log for details.");
        mActivity.testEnded(false);
    }
}
