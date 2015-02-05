package kr.co.nexon.todoplus.adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.fortysevendeg.swipelistview.SwipeListView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kr.co.nexon.todoplus.Entity.TaskInfo;
import kr.co.nexon.todoplus.Enums.DateType;
import kr.co.nexon.todoplus.Helper.CommonHelper;
import kr.co.nexon.todoplus.Helper.DBContactHelper;
import kr.co.nexon.todoplus.MainActivity;
import kr.co.nexon.todoplus.ModifyTaskActivity;
import kr.co.nexon.todoplus.R;


public class PackageAdapter extends BaseAdapter {

    private List<TaskInfo> data;
    private Context context;

    public PackageAdapter(Context context, List<TaskInfo> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public TaskInfo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final TaskInfo item = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.task_info, parent, false);


            holder = new ViewHolder();
            holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
            holder.star = (ImageView) convertView.findViewById(R.id.star);
            holder.lock = (ImageView) convertView.findViewById(R.id.lock);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.back = (LinearLayout) convertView.findViewById(R.id.back);


            holder.modifyTask = (TextView) convertView.findViewById(R.id.modify_task);
            holder.removeTask = (TextView) convertView.findViewById(R.id.remove_task);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        ((SwipeListView) parent).recycle(convertView, position);


        //TaskInfo taskInfo = taskInfoList.get(position);






        holder.code.setText(" (" + item.getId() + ")");

        if (!item.getImportant()) {
            holder.star.setVisibility(View.GONE);
        } else {
            holder.star.setVisibility(View.VISIBLE);
        }

        if (!item.getSecret()) {
            holder.lock.setVisibility(View.GONE);
        } else {
            holder.lock.setVisibility(View.VISIBLE);
        }

        holder.name.setText(item.getName());
        holder.name.setChecked(item.getCompleted());
        holder.name.setTag(item);

        if ((holder.name.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        if (holder.name.isChecked()) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.name.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;

                TaskInfo taskInfo = (TaskInfo) checkBox.getTag();
                taskInfo.setCompleted(checkBox.isChecked());

                // DB Update
                DBContactHelper db = new DBContactHelper(context);
                db.updateTaskInfo(taskInfo);

                if (taskInfo.getCompleted()) {
                    checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    ((MainActivity) context).completedCount++;
                    ((MainActivity) context).outstandingCount--;
                } else {
                    checkBox.setPaintFlags(checkBox.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
                    ((MainActivity) context).completedCount--;
                    ((MainActivity) context).outstandingCount++;
                }

                ((MainActivity) context).updateState();
            }
        });


        DateType dateType = DateType.values()[item.getDateType()];

        switch (dateType) {
            case None:
                holder.date.setText("");
                break;
            case Calendar:
            case Today:
            case Tomorrow:
                Calendar cal = item.getPeriod();
                Calendar todayCal = Calendar.getInstance();

                long diffDay = CommonHelper.getDateDiff(cal, todayCal);

                if (diffDay == 0) {
                    holder.date.setText(R.string.today);
                } else if (diffDay == 1) {
                    holder.date.setText(R.string.tomorrow);
                } else {
                    String month = new DateFormatSymbols(Locale.US).getShortMonths()[cal.get(Calendar.MONTH)];
                    String week = new DateFormatSymbols(Locale.US).getShortWeekdays()[cal.get(Calendar.DAY_OF_WEEK)];
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    String dateString = String.format("%s, %s %d", week, month, day);

                    holder.date.setText(dateString);
                }

                break;
        }

        final int taskId = item.getId();

        holder.modifyTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TaskInfo taskInfo = (TaskInfo) v.getTag(1);
                MainActivity.swipeListView.closeAnimate(position);

                try {
                    Intent intent = new Intent(context, ModifyTaskActivity.class);
                    intent.putExtra("taskId", taskId);
                    intent.putExtra("position", position);
                    ((Activity) context).startActivityForResult(intent, 1);
                }
                catch (Exception ex) {
                    Log.e("TaskInfoAdapter", ex.getMessage());
                }
            }
        });


        holder.removeTask.setTag(position);
        holder.removeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int)v.getTag();
                MainActivity.swipeListView.dismiss(position);
            }
        });


        return convertView;
    }

    static class ViewHolder {
        TextView code;
        CheckBox name;
        ImageView star;
        ImageView lock;
        TextView date;


        TextView modifyTask;
        TextView removeTask;


        LinearLayout back;
    }

    private boolean isPlayStoreInstalled() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }

}