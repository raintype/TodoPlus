package kr.co.nexon.todoplus;

import android.content.*;

public class RestartReceiver extends BroadcastReceiver {
    static public final String ACTION_RESTART_SERVICE = "RestartReceiver.restart";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_RESTART_SERVICE)) {
            Intent i = new Intent(context, ScreenService.class);
            context.startService(i);
        }
    }
}
