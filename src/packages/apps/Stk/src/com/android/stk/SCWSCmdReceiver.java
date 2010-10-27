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

import com.android.internal.telephony.gsm.stk.AppInterface;
import com.android.internal.telephony.gsm.stk.StkLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SCWSCmdReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(AppInterface.SCWS_CMD_ACTION)) {
            Bundle args = new Bundle();
            args.putParcelable("Cmd", intent.getParcelableExtra("SCWS CMD"));
            context.startService(new Intent(context, SCWSProxy.class)
                    .putExtras(args));
        }

        if (action.equals(AppInterface.STK_SESSION_END_ACTION)) {
            context.startService(new Intent(context, SCWSProxy.class));
        }
    }
}
