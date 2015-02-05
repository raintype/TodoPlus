package kr.co.nexon.todoplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.nexon.todoplus.Entity.SettingInfo;
import kr.co.nexon.todoplus.Entity.TaskInfo;
import kr.co.nexon.todoplus.Helper.CommonHelper;
import kr.co.nexon.todoplus.Helper.DBContactHelper;


public class LockScreenActivity extends Activity {
    Context context = this;
    private ArrayList<TaskInfo> taskInfoArrayList;

    private ShareActionProvider mShareActionProvider;

    SettingInfo settingInfo;

    Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);



        taskInfoArrayList = getTaskList();

        ViewPager vp = (ViewPager) findViewById(R.id.viewpager);

        // Set an OnPageChangeListener so we are notified when a new item is selected
        vp.setOnPageChangeListener(mOnPageChangeListener);

        // Finally set the adapter so the ViewPager can display items
        vp.setAdapter(mPagerAdapter);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<TaskInfo> getTaskList() {
        DBContactHelper db = new DBContactHelper(this);
        ArrayList<TaskInfo> taskInfoArrayList = null;

        taskInfoArrayList = db.getLockScreenTask();

        return taskInfoArrayList;
    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener
            = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // NO-OP
        }

        @Override
        public void onPageSelected(int position) {
            setShareIntent(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // NO-OP
        }
    };

    private final PagerAdapter mPagerAdapter = new PagerAdapter() {
        LayoutInflater mInflater;

        @Override
        public int getCount() {
            return taskInfoArrayList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Just remove the view from the ViewPager
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Ensure that the LayoutInflater is instantiated
            if (mInflater == null) {
                mInflater = LayoutInflater.from(LockScreenActivity.this);
            }

            // Get the item for the requested position
            final TaskInfo taskInfo = taskInfoArrayList.get(position);

            // The view we need to inflate changes based on the type of content
            // Inflate item layout for text
            TextView textView = (TextView) mInflater
                    .inflate(R.layout.item_text, container, false);

            // Set text content using it's resource id

            if (settingInfo.getIsDueDateDisplay()) {
                switch (taskInfo.getDateType()) {
                    case 0:
                        textView.setText(taskInfo.getName());
                        break;
                    case 1:
                    case 2:
                    case 3:
                        Calendar cal = taskInfo.getPeriod();
                        Calendar today = Calendar.getInstance();

                        long diffDay = CommonHelper.getDateDiff(cal, today);

                        String subText;

                        if (diffDay == 0) {
                            subText = "Today";
                        } else if (diffDay == 1) {
                            subText = "Tomorrow";
                        } else {
                            String month = new DateFormatSymbols(Locale.US).getShortMonths()[cal.get(Calendar.MONTH)];
                            String week = new DateFormatSymbols(Locale.US).getShortWeekdays()[cal.get(Calendar.DAY_OF_WEEK)];
                            int day = cal.get(Calendar.DAY_OF_MONTH);

                            subText = String.format("%s, %s %d", week, month, day);
                        }

                        String taskName = String.format("%s %s %s", taskInfo.getName(), CommonHelper.LINE_FEED, subText);

                        int size = 30;
                        int index = taskName.indexOf(CommonHelper.LINE_FEED);
                        SpannableStringBuilder builder = new SpannableStringBuilder(taskName);
                        builder.setSpan(new AbsoluteSizeSpan(size), index, taskName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textView.setText(builder);

                        break;
                }
            } else {
                textView.setText(taskInfo.getName());
            }
            //tv.setText(item.getName());

            // Add the view to the ViewPager
            container.addView(textView);
            return textView;

        }
    };

    private void setShareIntent(int position) {
        // BEGIN_INCLUDE(update_sap)
        if (mShareActionProvider != null) {
            // Get the currently selected item, and retrieve it's share intent
            TaskInfo item = taskInfoArrayList.get(position);
            //Intent shareIntent = item.getShareIntent(LockScreenActivity.this);

            // Now update the ShareActionProvider with the new share intent
            //mShareActionProvider.setShareIntent(shareIntent);
        }
        // END_INCLUDE(update_sap)
    }



    @Override
    protected void onDestroy() {
        if (mTimer != null)
            mTimer.cancel();
        super.onDestroy();

        finish();
    }

    @Override
    protected void onPause() {
        if (mTimer != null)
            mTimer.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        settingInfo = CommonHelper.getSettingInfo(context);
        if (settingInfo.getIsDayTimeDisplay()) {
            MainTimerTask timerTask = new MainTimerTask();
            mTimer = new Timer();
            mTimer.schedule(timerTask, 500, 3000);
            //navigator.scrollTo(0, 0);
        }

        super.onResume();
    }

    @Override
    protected void onUserLeaveHint() {
        finish();
        super.onUserLeaveHint();
    }

    public class MainTimerTask extends TimerTask {
        private Handler mHandler = new Handler();

        private Runnable mUpdateTimeTask = new Runnable() {

            public void run() {
                final Calendar c = Calendar.getInstance();

                int hour = c.get(Calendar.HOUR);
                if (hour == 0) {
                    hour = 12;
                }

                int minute = c.get(Calendar.MINUTE);
                String amPM = new DateFormatSymbols(Locale.US).getAmPmStrings()[c.get(Calendar.AM_PM)];
                String month = new DateFormatSymbols(Locale.US).getMonths()[c.get(Calendar.MONTH)];
                String week = new DateFormatSymbols(Locale.US).getWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
                int day = c.get(Calendar.DAY_OF_MONTH);

                String dateString = String.format("%s, %s %d", week, month, day);
                String timeString = String.format("%02d:%02d %s", hour, minute, amPM);

                // 시간의 AM, PM의 크기 조절
                SpannableStringBuilder builder = new SpannableStringBuilder(timeString);
                int amPmIndex =  timeString.indexOf(amPM);
                builder.setSpan(new AbsoluteSizeSpan(60),  + amPmIndex, amPmIndex+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                //dateTextView.setText(dateString);
                //timeTextView.setText(builder);
            }
        };

        public void run() {
            mHandler.post(mUpdateTimeTask);
        }
    }
}
