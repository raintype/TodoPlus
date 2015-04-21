package kr.co.nexon.todoplus;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;

import android.view.*;
import android.widget.*;

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
    SettingInfo settingInfo;

    Timer mTimer;

    TextView dateTextView;
    TextView timeTextView;
    ImageView navigator;
    ImageView navigator_left;
    ImageView navigator_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);


        // Step 1. Display Date&Time
        dateTextView = (TextView) findViewById(R.id.DateTextView);
        timeTextView = (TextView) findViewById(R.id.TimeTextView);
        dateTextView.setText("");
        timeTextView.setText("");

        settingInfo = CommonHelper.getSettingInfo(context);
        if (settingInfo.getIsDayTimeDisplay()) {
            // DateTime Display 를 위해 Schedule 등록
            MainTimerTask timerTask = new MainTimerTask();
            mTimer = new Timer();
            mTimer.schedule(timerTask, 500, 1000);
        }

        // Step2. Display Todo
        taskInfoArrayList = getTaskList();

        ViewPager vp = (ViewPager) findViewById(R.id.viewpager);

        // Set an OnPageChangeListener so we are notified when a new item is selected
        vp.setOnPageChangeListener(mOnPageChangeListener);

        // Finally set the adapter so the ViewPager can display items
        vp.setAdapter(mPagerAdapter);

        // Step 4. Display Navigator
        navigator = (ImageView)findViewById(R.id.navigator);
        navigator_left = (ImageView)findViewById(R.id.navigator_left);
        navigator_right = (ImageView)findViewById(R.id.navigator_right);

        // default navigator position
        // scrollTo 값은 center 값은 0
        // center를 기준으로 왼쪽(위)으로 이동 시 +1 증가
        // center를 기준으로 오른쪽(아래)으로 이동 시 -1 감소
        navigator.scrollTo(0, 0);
        navigator.setOnTouchListener(new View.OnTouchListener() {
            int prePositionX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int positionX;
                int positionY;

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        navigator_left.setImageResource(R.drawable.navi_left_on);
                        navigator_right.setImageResource(R.drawable.navi_right_on);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        boolean isVibration = false;
                        // event.getX()값은 왼쪽이 기준으로 0 부터 1씩 증가 한다.
                        int eventX = (int) (event.getX());
                        int eventY = (int)(event.getY());

                        if( eventX < navigator_left.getWidth()) {
                            positionX =  (navigator.getWidth()/2) - (navigator_left.getWidth()/2);
                            positionY = 0;

                            if (prePositionX != positionX)
                                isVibration = true;
                        } else if ( eventX >  (navigator.getWidth() - navigator_right.getWidth())) {

                            positionX =  (navigator.getWidth()/2) * -1 + navigator_right.getHeight()/2;
                            positionY = 0;

                            if (prePositionX != positionX)
                                isVibration = true;
                        } else {
                            positionX = (eventX * -1) + (navigator.getWidth() / 2);
                            positionY = (eventY * -1) + (navigator.getHeight()/2);
                        }

                        // navigator 가 특정 Y 영역을 벗어 나지 못하도록 처리
                        int positionYLimit = navigator.getHeight()/2 - ( navigator.getHeight()/4);
                        if ( positionY > positionYLimit) {
                            positionY = positionYLimit;
                        } else if ( positionY < positionYLimit*-1) {
                            positionY = positionYLimit*-1;
                        }

                        prePositionX = positionX;

                        if (isVibration) {
                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibe.vibrate(100);
                        }

                        navigator.scrollTo(positionX, positionY);
                        break;
                    case MotionEvent.ACTION_UP:
                        positionX = (int) (event.getX());

                        if (positionX < navigator_left.getWidth()) {
                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            startActivity(intent);

                            finish();
                        } else if (positionX > (navigator.getWidth() - navigator_left.getWidth())) {
                            finish();

                        } else {
                            navigator.scrollTo(0, 0);
                        }

                        navigator_left.setImageResource(R.drawable.navi_left);
                        navigator_right.setImageResource(R.drawable.navi_right);

                        break;
                } //end switch

                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: // 이전 버튼 막기
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private ArrayList<TaskInfo> getTaskList() {
        DBContactHelper db = new DBContactHelper(this);
        ArrayList<TaskInfo> taskInfoArrayList = db.getLockScreenTask();

        return taskInfoArrayList;
    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // NO-OP
        }

        @Override
        public void onPageSelected(int position) {
            // NO-OP
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

            // Add the view to the ViewPager
            container.addView(textView);
            return textView;

        }
    };

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
            navigator.scrollTo(0, 0);
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

                dateTextView.setText(dateString);
                timeTextView.setText(builder);
            }
        };

        public void run() {
            mHandler.post(mUpdateTimeTask);
        }
    }
}
