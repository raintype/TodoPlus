package kr.co.nexon.todoplus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.util.Calendar;

import kr.co.nexon.todoplus.Entity.TaskInfo;
import kr.co.nexon.todoplus.Enums.DateType;
import kr.co.nexon.todoplus.Helper.DBContactHelper;

public class AddTaskActivity extends ActionBarActivity {
    public static AddTaskActivity addTaskActivity;

    EditText editText;
    Button resultButton;
    TextView today;
    TextView tomorrow;
    TextView none;
    ImageView calendar;
    ImageView star;
    ImageView lock;

    DBContactHelper db;

    TaskInfo taskInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.add_task);

        addTaskActivity = this;

        db = new DBContactHelper(this);
        taskInfo = new TaskInfo();

        editText = (EditText)findViewById(R.id.edit_Text);

        resultButton = (Button)findViewById(R.id.result_button);
        resultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = editText.getText().toString();

                if (name.length() > 0) {
                    taskInfo.setName(name);
                    long resultValue = db.addTask(taskInfo);

                    Intent intent = new Intent();
                    intent.putExtra("activityName", "addTaskActivity");
                    intent.putExtra("taskId", String.valueOf(resultValue));

                    setResult(RESULT_OK,intent);

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Require Task Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        calendar = (ImageView)findViewById(R.id.calendar);
        today = (TextView)findViewById(R.id.today);
        tomorrow = (TextView)findViewById(R.id.tomorrow);
        none = (TextView)findViewById(R.id.none);

        final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                DateType dateType = DateType.Calendar;
                setDateTypeDisplay(dateType);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);

                taskInfo.setDateType(dateType.ordinal());
                taskInfo.setPeriod(calendar);
            }
        };

        calendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this, dateListener, year, month, day);

                datePickerDialog.show();
            }
        });

        today.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClickDate(DateType.Today);
            }
        });
        tomorrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClickDate(DateType.Tomorrow);
            }
        });
        none.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClickDate(DateType.None);
            }
        });

        star = (ImageView)findViewById(R.id.star);
        star.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                taskInfo.setImportant(!taskInfo.getImportant());

                setStarDisplay(taskInfo.getImportant());
            }
        });

        lock = (ImageView)findViewById(R.id.lock);
        lock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                taskInfo.setSecret(!taskInfo.getSecret());

                setLockDisplay(taskInfo.getSecret());
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
    public boolean onSupportNavigateUp()
    {
        finish();

        return super.onSupportNavigateUp();
    }

    private void setDateTypeDisplay(DateType dateType) {
        int selectedTextColor = getResources().getColor(R.color.main);
        int notSelectedTextColor = getResources().getColor(R.color.disabled);

        switch (dateType) {
            case None :
                calendar.setImageResource(R.drawable.ic_cal_off);
                today.setTextColor(notSelectedTextColor);
                tomorrow.setTextColor(notSelectedTextColor);

                none.setTextColor(selectedTextColor);
                break;
            case Calendar:
                calendar.setImageResource(R.drawable.ic_cal_on);
                today.setTextColor(notSelectedTextColor);
                tomorrow.setTextColor(notSelectedTextColor);
                none.setTextColor(notSelectedTextColor);
                break;
            case Today:
                calendar.setImageResource(R.drawable.ic_cal_off);
                today.setTextColor(selectedTextColor);
                tomorrow.setTextColor(notSelectedTextColor);
                none.setTextColor(notSelectedTextColor);
                break;
            case Tomorrow:
                calendar.setImageResource(R.drawable.ic_cal_off);
                today.setTextColor(notSelectedTextColor);
                tomorrow.setTextColor(selectedTextColor);
                none.setTextColor(notSelectedTextColor);
                break;
        }
    }

    private void setStarDisplay(boolean isImportant) {
        if (isImportant) {
            star.setImageResource(R.drawable.ic_star_on);
        } else {
            star.setImageResource(R.drawable.ic_star_off);
        }
    }

    private void setLockDisplay(boolean isSecret) {
        if (isSecret) {
            lock.setImageResource(R.drawable.ic_lock_on);
        } else {
            lock.setImageResource(R.drawable.ic_lock_off);
        }
    }

    private  void ClickDate(DateType dateType){
        Calendar calendar = Calendar.getInstance();
        setDateTypeDisplay(dateType);
        taskInfo.setDateType(dateType.ordinal());

        switch (dateType)
        {
            case Today:
                taskInfo.setPeriod(calendar);

                break;
            case Tomorrow:
                calendar.add(Calendar.DAY_OF_WEEK, 1);
                taskInfo.setPeriod(calendar);
                break;
            default:
                break;
        }
    }
}
