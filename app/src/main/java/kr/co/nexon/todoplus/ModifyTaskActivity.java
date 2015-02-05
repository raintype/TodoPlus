package kr.co.nexon.todoplus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import kr.co.nexon.todoplus.Entity.TaskInfo;
import kr.co.nexon.todoplus.Enums.DateType;
import kr.co.nexon.todoplus.Helper.CommonHelper;
import kr.co.nexon.todoplus.Helper.DBContactHelper;


public class ModifyTaskActivity extends ActionBarActivity {
    public static ModifyTaskActivity modifyTaskActivity;

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

    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.modify_task);


        Intent intent = getIntent();
        int id = intent.getIntExtra("taskId", -1);

        position = intent.getIntExtra("position", -1);

        if ( id == -1) {
            // Todo Error Message Display

            finish();
        }

        db = new DBContactHelper(this);

        try {
            taskInfo = db.getTask(id);
        } catch (Exception ex) {
            Log.e("ModifyTaskActivity", ex.getMessage());
        }

        editText = (EditText)findViewById(R.id.edit_Text);
        editText.setText(taskInfo.getName());

        resultButton = (Button)findViewById(R.id.result_button);
        resultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = editText.getText().toString();

                if (name.length() > 0) {
                    taskInfo.setName(name);

                    db.updateTaskInfo(taskInfo);

                    Intent intent = new Intent();
                    intent.putExtra("activityName", "modifyTAskActivity");
                    intent.putExtra("taskId", String.valueOf(taskInfo.getId()));
                    intent.putExtra("position", String.valueOf(position));


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

        DateType dateType = DateType.values()[taskInfo.getDateType()];

        switch (dateType) {
            case None:
                setDateTypeDisplay(dateType);

                break;
            case Calendar:
            case Today:
            case Tomorrow:
                Calendar cal = taskInfo.getPeriod();
                Calendar todayCal = Calendar.getInstance();

                long diffDay = CommonHelper.getDateDiff(cal, todayCal);

                if (diffDay == 0) {
                    setDateTypeDisplay(DateType.Today);
                } else if (diffDay == 1) {
                    setDateTypeDisplay(DateType.Tomorrow);
                } else {
                    setDateTypeDisplay(DateType.Calendar);
                }

                break;
        }

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

                DatePickerDialog datePickerDialog = new DatePickerDialog(ModifyTaskActivity.this, dateListener, year, month, day);

                datePickerDialog.show();
            }
        });

        today.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DateType dateType = DateType.Today;
                setDateTypeDisplay(dateType);

                Calendar calendar = Calendar.getInstance();

                taskInfo.setDateType(dateType.ordinal());
                taskInfo.setPeriod(calendar);
            }
        });

        tomorrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DateType dateType = DateType.Tomorrow;
                setDateTypeDisplay(dateType);

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_WEEK, 1);

                taskInfo.setDateType(dateType.ordinal());
                taskInfo.setPeriod(calendar);
            }
        });


        none.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DateType dateType = DateType.None;
                setDateTypeDisplay(dateType);

                taskInfo.setDateType(dateType.ordinal());
            }
        });

        star = (ImageView)findViewById(R.id.star);

        setStarDisplay(taskInfo.getImportant());

        star.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                taskInfo.setImportant(!taskInfo.getImportant());

                setStarDisplay(taskInfo.getImportant());
            }
        });

        lock = (ImageView)findViewById(R.id.lock);

        setLock(taskInfo.getSecret());

        lock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                taskInfo.setSecret(!taskInfo.getSecret());

                setLock(taskInfo.getSecret());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    // 이벤트 처리
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
                calendar.setBackgroundColor(notSelectedTextColor);
                today.setTextColor(notSelectedTextColor);
                tomorrow.setTextColor(notSelectedTextColor);

                none.setTextColor(selectedTextColor);
                break;
            case Calendar:
                calendar.setBackgroundColor(selectedTextColor);
                today.setTextColor(notSelectedTextColor);
                tomorrow.setTextColor(notSelectedTextColor);
                none.setTextColor(notSelectedTextColor);
                break;
            case Today:
                calendar.setBackgroundColor(notSelectedTextColor);
                today.setTextColor(selectedTextColor);
                tomorrow.setTextColor(notSelectedTextColor);
                none.setTextColor(notSelectedTextColor);
                break;
            case Tomorrow:
                calendar.setBackgroundColor(notSelectedTextColor);
                today.setTextColor(notSelectedTextColor);
                tomorrow.setTextColor(selectedTextColor);
                none.setTextColor(notSelectedTextColor);
                break;
        }
    }


    private void setStarDisplay(boolean isImportant) {
        if (isImportant) {
            star.setBackgroundColor(getResources().getColor(R.color.main));
        } else {
            star.setBackgroundColor(getResources().getColor(R.color.disabled));
        }
    }

    private void setLock(boolean isSecret) {
        if (isSecret) {
            lock.setBackgroundColor(getResources().getColor(R.color.main));
        } else {
            lock.setBackgroundColor(getResources().getColor(R.color.disabled));
        }
    }
}
