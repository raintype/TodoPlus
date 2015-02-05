package kr.co.nexon.todoplus;


import android.app.Activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.nexon.todoplus.Entity.TaskInfo;
import kr.co.nexon.todoplus.Helper.CommonHelper;
import kr.co.nexon.todoplus.Helper.DBContactHelper;
//import kr.co.nexon.todoplus.Helper.PreferencesManager;
import kr.co.nexon.todoplus.Helper.SettingsManager;
import kr.co.nexon.todoplus.adapter.PackageAdapter;



public class MainActivity extends ActionBarActivity {
    public static MainActivity mainActivity;

    private static final int REQUEST_CODE_SETTINGS = 0;


    private PackageAdapter adapter;



    private List<TaskInfo> taskInfoList;

    static public SwipeListView swipeListView;

    private ProgressDialog progressDialog;


    DBContactHelper db;






    public int completedCount = 0;
    public int outstandingCount = 0;

    SwipeRefreshListFragmentFragment fragment;

    TextView dateTextView;
    TextView completedTextView;
    TextView outstandingTextTiew;

    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;


        taskInfoList = new ArrayList<>();

        adapter = new PackageAdapter(this, taskInfoList);


        swipeListView = (SwipeListView) findViewById(R.id.example_lv_list);

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            swipeListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                      long id, boolean checked) {
                    //mode.setTitle("Selected (" + swipeListView.getCountSelected() + ")");
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {

                        default:
                            return false;
                    }
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    //MenuInflater inflater = mode.getMenuInflater();
                    //inflater.inflate(R.menu.menu_choice_items, menu);
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

        //progressDialog = new ProgressDialog(this);
        //progressDialog.setMessage(getString(R.string.loading));
        //progressDialog.setCancelable(false);
        //progressDialog.show();








/*
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            fragment = new SwipeRefreshListFragmentFragment();

            transaction.replace(R.id.task_fragment, fragment);

            transaction.commit();


        }
*/


        ActionBar actionBar = getSupportActionBar();

        actionBar.setCustomView(R.layout.action_bar_title_layout);

        actionBar.setDisplayShowCustomEnabled(true);
        dateTextView =  (TextView) findViewById(R.id.date_text_view);
        completedTextView =  (TextView) findViewById(R.id.completed_text_view);
        outstandingTextTiew = (TextView) findViewById(R.id.outstanding_text_view);

        dateTextView.setText(CommonHelper.getCurrentDate());

        mFab = (FloatingActionButton)findViewById(R.id.fabbutton);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), AddTaskActivity.class);
                intent.putExtra("state","Test");
                startActivityForResult(intent, 1);
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


        switch (requestCode) {
            case REQUEST_CODE_SETTINGS:
                reload();
        }

        if (resultCode == RESULT_OK) {
            switch (data.getStringExtra("activityName"))
            {
                case "addTaskActivity":
                    int taskId = Integer.parseInt(data.getStringExtra("taskId"));


                    //fragment.Refresh();

                    break;
                case "modifyTAskActivity":

                    ArrayList<TaskInfo> taskInfoArrayList = null;


                    break;
            }
        }
    }

    private String getCurrentState() {
        return String.format("%d %s \r\n%d %s", completedCount, getString(R.string.completed), outstandingCount, getString(R.string.outstanding));
    }

    public void updateState(){

        completedTextView.setText(String.format("%d %s", completedCount, getString(R.string.completed)));
        outstandingTextTiew.setText(String.format("%d %s", outstandingCount, getString(R.string.outstanding)));
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


    public class ListAppTask extends AsyncTask<Void, Void, List<TaskInfo>> {

        protected List<TaskInfo> doInBackground(Void... args) {
            PackageManager appInfo = getPackageManager();
            List<ApplicationInfo> listInfo = appInfo.getInstalledApplications(0);
            Collections.sort(listInfo, new ApplicationInfo.DisplayNameComparator(appInfo));

            db = new DBContactHelper(getApplicationContext());
            List<TaskInfo> data = db.getAllTask();

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
            //if (PreferencesManager.getInstance(MainActivity.this).getShowAbout()) {
                //AboutDialog logOutDialog = new AboutDialog();
                //logOutDialog.show(getSupportFragmentManager(), "dialog");
            //}
        }
    }


}

