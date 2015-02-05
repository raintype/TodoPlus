package kr.co.nexon.todoplus.Entity;

/**
 * Created by raintype on 2015-01-26.
 */
public class SettingInfo {
    boolean IsLockScreen;
    boolean IsDayTimeDisplay;
    boolean IsDueDateDisplay;

    public boolean getIsLockScree() { return IsLockScreen; }
    public boolean getIsDayTimeDisplay() { return IsDayTimeDisplay; }
    public boolean getIsDueDateDisplay() { return IsDueDateDisplay; }

    public void setIsLockScreen(boolean value) { IsLockScreen = value; }
    public void setIsDayTimeDisplay(boolean value) { IsDayTimeDisplay = value; }
    public void setIsDueDateDisplay(boolean value) { IsDueDateDisplay = value; }

    public SettingInfo() {
        IsLockScreen = false;
        IsDayTimeDisplay = false;
        IsDueDateDisplay = false;
    }
}
