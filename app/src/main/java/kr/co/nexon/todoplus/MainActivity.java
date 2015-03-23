package kr.co.nexon.todoplus;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;

import com.fortysevendeg.swipelistview.*;

import java.util.*;

import kr.co.nexon.todoplus.Entity.*;
import kr.co.nexon.todoplus.Helper.*;
import kr.co.nexon.todoplus.adapter.PackageAdapter;

public class MainActivity extends ActionBarActivity {
    public static MainActivity mainActivity;
    final Context context = this;

    private PackageAdapter adapter;

    private List<TaskInfo> taskInfoList;

    static public SwipeListView swipeListView;

    private ProgressDialog progressDialog;

    DBContactHelper db;

    private LinearLayout left_drawer;
    private DrawerLayout mDrawerLayout;

    public int completedCount = 0;
    public int outstandingCount = 0;

    TextView dateTextView;
    TextView completedTextView;
    TextView outstandingTextView;

    FloatingActionButton mFab;

    Switch switchLockScreen;
    Switch switchDayTime;
    Switch switchDueDate;
    Button button_delete_all;

    SettingInfo settingInfo;

    public static boolean isFirstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        db = new DBContactHelper(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.action_bar_title_layout);

        actionBar.setDisplayShowCustomEnabled(true);
        dateTextView =  (TextView) findViewById(R.id.date_text_view);
        completedTextView =  (TextView) findViewById(R.id.completed_text_view);
        outstandingTextView = (TextView) findViewById(R.id.outstanding_text_view);

        dateTextView.setText(CommonHelper.getCurrentDate());

        taskInfoList = new ArrayList<>();

        adapter = new PackageAdapter(this, taskInfoList);


        swipeListView = (SwipeListView) findViewById(R.id.example_lv_list);

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            swipeListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    swipeListView.unselectedChoiceStates();
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }
           });
        }

        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
/*
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("swipe", String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));
            }

            @Override
            public void onClickBackView(int position) {
                Log.d("swipe", String.format("onClickBackView %d", position));
            }
*/
            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    taskInfoList.remove(position);
                }
                adapter.notifyDataSetChanged();
            }

        });

        swipeListView.setAdapter(adapter);

        reload();

        new ListAppTask().execute();


        mFab = (FloatingActionButton)findViewById(R.id.fabbutton);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), AddTaskActivity.class);
                intent.putExtra("state","Test");
                startActivityForResult(intent, 1);
            }
        });

        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        left_drawer = (LinearLayout)findViewById(R.id.left_drawer);
        View leftMenuView  = vi.inflate(R.layout.left_menu, null);
        left_drawer.addView(leftMenuView);

        settingInfo = CommonHelper.getSettingInfo(this);

        switchLockScreen = (Switch)findViewById(R.id.switchLockScreen);
        switchDayTime = (Switch)findViewById(R.id.switchDayTime);
        switchDueDate = (Switch)findViewById(R.id.switchDueDate);
        button_delete_all = (Button)findViewById(R.id.button_delete_all);

        switchLockScreen.setChecked(settingInfo.getIsLockScree());

        if (settingInfo.getIsLockScree()) {
            if (isFirstRun) {
                setLockScreen(true);
                isFirstRun = false;
            }
        }

        switchDayTime.setChecked(settingInfo.getIsDayTimeDisplay());
        switchDueDate.setChecked(settingInfo.getIsDueDateDisplay());

        switchLockScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                settingInfo.setIsLockScreen(((Switch) v).isChecked());

                CommonHelper.setSettingInfo(context, settingInfo);

                setLockScreen(settingInfo.getIsLockScree());
            }
        });

        switchDayTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                settingInfo.setIsDayTimeDisplay(((Switch) v).isChecked());

                CommonHelper.setSettingInfo(context, settingInfo);
            }
        });

        switchDueDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                settingInfo.setIsDueDateDisplay(((Switch) v).isChecked());

                CommonHelper.setSettingInfo(context, settingInfo);
            }
        });

        button_delete_all.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
                alt_bld.setMessage(R.string.confirm_remove_all_completed_task).setCancelable(false).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.removeAllCompletedTask();

                        new ListAppTask().execute();
                        swipeListView.closeOpenedItems();

                        mDrawerLayout.closeDrawers();
                    }
                }).setNegativeButton(R.string.button_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );

                AlertDialog alert = alt_bld.create();
                // Title for AlertDialog
                alert.setTitle(R.string.confirm);
                // Icon for AlertDialog
                //alert.setIcon(R.drawable.icon);
                alert.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (data.getStringExtra("activityName"))
            {
                case "addTaskActivity":
                    new ListAppTask().execute();

                    break;
                case "modifyTAskActivity":
                    int position = Integer.parseInt(data.getStringExtra("position"));

                    swipeListView.closeAnimate(position);

                    new ListAppTask().execute();

                    break;
            }
        }
    }

    public void updateState(){
        completedTextView.setText(String.format("%d %s", completedCount, getString(R.string.completed)));
        outstandingTextView.setText(String.format("%d %s", outstandingCount, getString(R.string.outstanding)));
    }

    private void reload() {
        SettingsManager settings = SettingsManager.getInstance();
        swipeListView.setSwipeMode(settings.getSwipeMode());
        swipeListView.setSwipeActionLeft(settings.getSwipeActionLeft());
        swipeListView.setSwipeActionRight(settings.getSwipeActionRight());
        swipeListView.setOffsetLeft(convertDpToPixel(settings.getSwipeOffsetLeft()));
        swipeListView.setOffsetRight(convertDpToPixel(settings.getSwipeOffsetRight()));
        swipeListView.setAnimationTime(settings.getSwipeAnimationTime());
        swipeListView.setSwipeOpenOnLongPress(settings.isSwipeOpenOnLongPress());
    }

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    private void setLockScreen(boolean enable) {
        Intent intent;
        if (enable) {
            intent = new Intent(context, ScreenService.class);
            startService(intent);
            Toast.makeText(getBaseContext(), "Lock Screen On", Toast.LENGTH_SHORT).show();
        } else {
            intent = new Intent(context, ScreenService.class);
            stopService(intent);
            Toast.makeText(getBaseContext(), "Lock Screen Off", Toast.LENGTH_SHORT).show();
        }
    }

    public class ListAppTask extends AsyncTask<Void, Void, List<TaskInfo>> {

        protected List<TaskInfo> doInBackground(Void... args) {
            List<TaskInfo> data = db.getAllTask();

            completedCount = 0;
            outstandingCount = 0;
            for(TaskInfo taskInfo :data){
                if (taskInfo.getCompleted()) {
                    completedCount++;
                } else{
                    outstandingCount++;
                }
            }
            return data;
        }

        protected void onPostExecute(List<TaskInfo> result) {
            taskInfoList.clear();
            taskInfoList.addAll(result);
            adapter.notifyDataSetChanged();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            updateState();
        }
    }
}

