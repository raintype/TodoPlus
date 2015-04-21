package kr.co.nexon.todoplus;

import android.content.*;

import kr.co.nexon.todoplus.Entity.SettingInfo;
import kr.co.nexon.todoplus.Helper.CommonHelper;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 기기 부팅 시 ScreenService 를 자동 시작하도록 설정
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SettingInfo settingInfo = CommonHelper.getSettingInfo(context);

            if (settingInfo.getIsLockScree()) {
                Intent i = new Intent(context, ScreenService.class);
                context.startService(i);
                MainActivity.isFirstRun = false;
            }
        }
    }
}