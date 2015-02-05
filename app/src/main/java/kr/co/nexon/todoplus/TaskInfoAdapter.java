package kr.co.nexon.todoplus;


import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;
import kr.co.nexon.todoplus.Entity.*;
import kr.co.nexon.todoplus.Enums.*;
import kr.co.nexon.todoplus.Helper.*;
import kr.co.nexon.todoplus.*;

/**
 * Created by raintype on 2015-01-21.
 * http://www.mysamplecode.com/2012/07/android-listview-checkbox-example.html
 */
public class TaskInfoAdapter extends ArrayAdapter<TaskInfo> {
    ArrayList<TaskInfo> taskInfoList;
    Context context;
    String state;

    public TaskInfoAdapter(Context context, int textViewResourceId, ArrayList<TaskInfo> taskInfoList, String state) {
        super(context, textViewResourceId, taskInfoList);
        this.taskInfoList = new ArrayList<>();
        this.taskInfoList.addAll(taskInfoList);
        this.state = state;
        this.context = context;
    }

    private class ViewHolder {
        TextView code;
        CheckBox name;
        ImageView star;
        ImageView lock;
        TextView date;
    }

    @Override
    public void add(TaskInfo object) {
        taskInfoList.add(0, object);

        super.add(object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.task_info, null);

            holder = new ViewHolder();
            holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
            holder.star = (ImageView) convertView.findViewById(R.id.star);
            holder.lock = (ImageView) convertView.findViewById(R.id.lock);
            holder.date = (TextView) convertView.findViewById(R.id.date);

            convertView.setTag(holder);

            holder.name.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    CheckBox checkBox = (CheckBox) v ;
                    TaskInfo taskInfo = (TaskInfo) checkBox.getTag();

                    try {
                        //Intent intent = new Intent(context, ModifyTaskActivity.class);
                        //intent.putExtra("state", state);
                        //intent.putExtra("taskId", taskInfo.getId());
                        //((Activity) context).startActivityForResult(intent, 1);
                    }
                    catch (Exception ex) {
                        Log.e("TaskInfoAdapter", ex.getMessage());
                    }

                    return  true;
                }
            });

            convertView.setOnTouchListener(new View.OnTouchListener() {
                float xAtDown;
                float xAtUp;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        xAtDown = event.getX();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        xAtUp = event.getX();

                        if (xAtDown < xAtUp) {
                            AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

                            ViewHolder viewHolder = (ViewHolder) v.getTag();
                            final TaskInfo taskInfo = (TaskInfo) viewHolder.name.getTag();
                            taskInfo.setUseYN(false);
/*
                            alt_bld.setMessage(R.string.confirm_remove_task).setCancelable(false).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    DBContactHelper db = new DBContactHelper(context);
                                    db.updateTaskInfo(taskInfo);

                                    ((MainActivity) context).updateTaskList();
                                }
                            }).setNegativeButton(R.string.button_cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = alt_bld.create();
                            // Title for AlertDialog
                            alert.setTitle(R.string.confirm);
                            // Icon for AlertDialog
                            //alert.setIcon(R.drawable.icon);
                            alert.show();
*/
                        }

                        xAtDown = 0;
                    }
                    return true;
                }
            });

            holder.name.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v ;

                    TaskInfo taskInfo = (TaskInfo) checkBox.getTag();
                    taskInfo.setCompleted(checkBox.isChecked());

                    // DB Update
                    DBContactHelper db = new DBContactHelper(context);
                    db.updateTaskInfo(taskInfo);

                    if (taskInfo.getCompleted()) {
                        checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        //((MainActivity)context).completedCount++;
                        //((MainActivity)context).outstandingCount--;
                    } else  {
                        checkBox.setPaintFlags(checkBox.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
                        //((MainActivity)context).completedCount--;
                        //((MainActivity)context).outstandingCount++;
                    }

                    //((MainActivity)context).updateState();
                }
            });
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        TaskInfo taskInfo = taskInfoList.get(position);
        holder.code.setText(" (" +  taskInfo.getId() + ")");

        if (!taskInfo.getImportant()) {
            holder.star.setVisibility(View.GONE);
        } else {
            holder.star.setVisibility(View.VISIBLE);
        }

        if (!taskInfo.getSecret()) {
            holder.lock.setVisibility(View.GONE);
        } else {
            holder.lock.setVisibility(View.VISIBLE);
        }

        holder.name.setText(taskInfo.getName());
        holder.name.setChecked(taskInfo.getCompleted());
        holder.name.setTag(taskInfo);

        if ((holder.name.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() &(~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        if (holder.name.isChecked()) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        DateType dateType = DateType.values()[taskInfo.getDateType()];

        switch (dateType) {
            case None:
                holder.date.setText("");
                break;
            case Calendar:
            case Today:
            case Tomorrow:
                Calendar cal = taskInfo.getPeriod();
                Calendar todayCal = Calendar.getInstance();

                long diffDay = CommonHelper.getDateDiff(cal, todayCal);

                if (diffDay == 0)
                {
                    //holder.date.setText(R.string.today);
                } else if (diffDay == 1) {
                    //holder.date.setText(R.string.tomorrow);
                } else {
                    String month = new DateFormatSymbols(Locale.US).getShortMonths()[cal.get(Calendar.MONTH)];
                    String week = new DateFormatSymbols(Locale.US).getShortWeekdays()[cal.get(Calendar.DAY_OF_WEEK)];
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    String dateString = String.format("%s, %s %d", week, month, day);

                    holder.date.setText(dateString);
                }

                break;
        }

        return convertView;
    }
}
