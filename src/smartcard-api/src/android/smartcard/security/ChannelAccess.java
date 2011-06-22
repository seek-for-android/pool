/*
 * Copyright 2010 Giesecke & Devrient GmbH.
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

package android.smartcard.security;

public class ChannelAccess {

    protected boolean mUnlimitedAccess = false;

    protected boolean mNoAccess = false;

    protected boolean mUseAccessConditions = false;

    protected int mCallingPid = 0;

    protected String mReason = "";

    protected AccessCondition[] mAccessConditions = null;

    public boolean isUnlimitedAccess() {
        return mUnlimitedAccess;
    }

    public void setUnlimitedAccess(boolean unlimitedAccess) {
        this.mUnlimitedAccess = unlimitedAccess;
    }

    public boolean isNoAccess() {
        return mNoAccess;
    }

    public void setNoAccess(boolean noAccess, String reason) {
        this.mNoAccess = noAccess;
        this.mReason = reason;
    }

    public boolean isUseAccessConditions() {
        return mUseAccessConditions;
    }

    public void setUseAccessConditions(boolean useAccessConditions) {
        this.mUseAccessConditions = useAccessConditions;
    }

    public void setCallingPid(int callingPid) {
        this.mCallingPid = callingPid;
    }

    public int getCallingPid() {
        return mCallingPid;
    }

    public String getReason() {
        return mReason;
    }

    public AccessCondition[] getAccessConditions() {
        return mAccessConditions;
    }

    public void setAccessConditions(AccessCondition[] accessConditions) {
        mAccessConditions = accessConditions;
    }

}
