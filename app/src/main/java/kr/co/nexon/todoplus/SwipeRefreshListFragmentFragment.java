package kr.co.nexon.todoplus;



import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import kr.co.nexon.todoplus.Entity.TaskInfo;
import kr.co.nexon.todoplus.Helper.DBContactHelper;

/**
 * A sample which shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} within a
 * {@link android.support.v4.app.ListFragment} to add the 'swipe-to-refresh' gesture to a
 * {@link android.widget.ListView}. This is provided through the provided re-usable
 * {@link SwipeRefreshListFragment} class.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item. This item should be displayed in the Action Bar's overflow item.
 *
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 *
 * <p>This sample also provides the functionality to change the colors displayed in the
 * {@link android.support.v4.widget.SwipeRefreshLayout} through the options menu. This is meant to
 * showcase the use of color rather than being something that should be integrated into apps.
 */
public class SwipeRefreshListFragmentFragment extends SwipeRefreshListFragment {

    private static final String LOG_TAG = SwipeRefreshListFragmentFragment.class.getSimpleName();

    private static final int LIST_ITEM_COUNT = 20;

    DBContactHelper db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(false);

        db = new DBContactHelper(getActivity());
    }

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<TaskInfo> taskInfoArrayList = db.getAllTask();

        TaskInfoAdapter taskAdapter = new TaskInfoAdapter(getActivity(), R.layout.task_info, taskInfoArrayList, "Test");

        MainActivity mainActivity = MainActivity.mainActivity;

        if (mainActivity != null) {
            mainActivity.completedCount = 0;
            mainActivity.outstandingCount = 0;

            for (TaskInfo taskInfo : taskInfoArrayList) {
                if (taskInfo.getCompleted()) {
                    mainActivity.completedCount++;
                } else {
                    mainActivity.outstandingCount++;
                }
            }

            mainActivity.updateState();
        }

        // Set the adapter between the ListView and its backing data.
        setListAdapter(taskAdapter);


        // BEGIN_INCLUDE (setup_refreshlistener)
        /**
         * Implement {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });
        // END_INCLUDE (setup_refreshlistener)
    }
    // END_INCLUDE (setup_views)

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.main_menu, menu);
    }

    // BEGIN_INCLUDE (setup_refresh_menu_listener)
    /**
     * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout
     * progress bar, then initiate the background task that refreshes the content.
     *
     * <p>A color scheme menu item used for demonstrating the use of SwipeRefreshLayout's color
     * scheme functionality. This kind of menu item should not be incorporated into your app,
     * it just to demonstrate the use of color. Instead you should choose a color scheme based
     * off of your application's branding.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE (setup_refresh_menu_listener)

    // BEGIN_INCLUDE (initiate_refresh)
    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");

        /**
         * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
         */
        new DummyBackgroundTask().execute();
    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)
    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(List<TaskInfo> result) {
        Log.i(LOG_TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        ArrayAdapter<TaskInfo> adapter = (ArrayAdapter<TaskInfo>) getListAdapter();
        adapter.clear();
        
        MainActivity mainActivity = MainActivity.mainActivity;

        if (mainActivity != null) {
            mainActivity.completedCount = 0;
            mainActivity.outstandingCount = 0;

            for (int i = result.size() -1 ; i >= 0 ; i--) {
                adapter.add(result.get(i));

                if (result.get(i).getCompleted()){
                    mainActivity.completedCount++;
                } else {
                    mainActivity.outstandingCount++;
                }
            }

            mainActivity.updateState();
        }

        setRefreshing(false);
    }
    // END_INCLUDE (refresh_complete)

    public void Refresh(){
        Log.i(LOG_TAG, "Refresh menu item selected");

        // We make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
        if (!isRefreshing()) {
            setRefreshing(true);
        }

        // Start our refresh background task
        initiateRefresh();
    }

    /**
     * Dummy {@link android.os.AsyncTask} which simulates a long running task to fetch new cheeses.
     */
    private class DummyBackgroundTask extends AsyncTask<Void, Void, List<TaskInfo>> {

        static final int TASK_DURATION = 2 * 1000; // 3 seconds

        @Override
        protected List<TaskInfo> doInBackground(Void... params) {
            // Sleep for a small amount of time to simulate a background-task
            try {
                Thread.sleep(TASK_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            db = new DBContactHelper(getActivity());
            ArrayList<TaskInfo> taskInfoArrayList= db.getAllTask();

            return taskInfoArrayList;
        }

        @Override
        protected void onPostExecute(List<TaskInfo> result) {
            super.onPostExecute(result);

            // Tell the Fragment that the refresh has completed
            onRefreshComplete(result);
        }

    }

}

