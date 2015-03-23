package kr.co.nexon.todoplus;

import android.content.*;

import kr.co.nexon.todoplus.Entity.SettingInfo;
import kr.co.nexon.todoplus.Helper.CommonHelper;

public class RestartReceiver extends BroadcastReceiver {
    static public final String ACTION_RESTART_SERVICE = "RestartReceiver.restart";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_RESTART_SERVICE)) {
            SettingInfo settingInfo = CommonHelper.getSettingInfo(context);
            if (settingInfo.getIsLockScree()) {
                Intent i = new Intent(context, ScreenService.class);
                context.startService(i);
                MainActivity.isFirstRun = false;
            }
        }
    }
}
