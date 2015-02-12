package kr.co.nexon.todoplus.adapter;

import android.app.Activity;
import android.content.*;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.*;
import android.widget.*;
import com.fortysevendeg.swipelistview.SwipeListView;

import java.text.DateFormatSymbols;
import java.util.*;

import kr.co.nexon.todoplus.Entity.TaskInfo;
import kr.co.nexon.todoplus.Enums.DateType;
import kr.co.nexon.todoplus.Helper.*;
import kr.co.nexon.todoplus.MainActivity;
import kr.co.nexon.todoplus.ModifyTaskActivity;
import kr.co.nexon.todoplus.R;

public class PackageAdapter extends BaseAdapter {
    private List<TaskInfo> data;
    private Context context;
    private DBContactHelper db;

    public PackageAdapter(Context context, List<TaskInfo> data) {
        this.context = context;
        this.data = data;

        db = new DBContactHelper(context);
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
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.task_info, parent, false);

            holder = new ViewHolder();
            holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
            holder.star = (ImageView) convertView.findViewById(R.id.star);
            holder.lock = (ImageView) convertView.findViewById(R.id.lock);
            holder.date = (TextView) convertView.findViewById(R.id.date);

            holder.modifyTask = (LinearLayout) convertView.findViewById(R.id.modify_task);
            holder.removeTask = (LinearLayout) convertView.findViewById(R.id.remove_task);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((SwipeListView) parent).recycle(convertView, position);

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

        if (dateType == DateType.None) {
            holder.date.setText("");
        } else {
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
        }

        final int taskId = item.getId();

        holder.modifyTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.swipeListView.closeAnimate(position);

                Intent intent = new Intent(context, ModifyTaskActivity.class);
                intent.putExtra("taskId", taskId);
                intent.putExtra("position", position);

                ((Activity) context).startActivityForResult(intent, 1);
            }
        });

        holder.removeTask.setTag(position);
        holder.removeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int)v.getTag();
                TaskInfo taskInfo = (TaskInfo) holder.name.getTag();
                taskInfo.setUseYN(false);
                db.updateTaskInfo(taskInfo);

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

        LinearLayout modifyTask;
        LinearLayout removeTask;
    }
}