package kr.co.nexon.todoplus;


import android.content.*;
import android.telephony.*;

public class ScreenReceiver extends BroadcastReceiver {
    TelephonyManager telephonyManager = null;
    boolean isPhoneIdle = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            if (telephonyManager == null) {
                telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
            }

            if (isPhoneIdle) {
                Intent i = new Intent(context, LockScreenActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                MainActivity mainActivity = MainActivity.mainActivity;
                if (mainActivity != null)
                    mainActivity.finish();

                AddTaskActivity addTaskActivity = AddTaskActivity.addTaskActivity;
                if (addTaskActivity != null)
                    addTaskActivity.finish();

                ModifyTaskActivity modifyTaskActivity = ModifyTaskActivity.modifyTaskActivity;
                if (modifyTaskActivity != null)
                    modifyTaskActivity.finish();

                context.startActivity(i);
            }
        }
    }

    private PhoneStateListener phoneListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber){
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE :
                    isPhoneIdle = true;
                    break;
                case TelephonyManager.CALL_STATE_RINGING :
                    isPhoneIdle = false;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK :
                    isPhoneIdle = false;
                    break;
            }
        }
    };
}
