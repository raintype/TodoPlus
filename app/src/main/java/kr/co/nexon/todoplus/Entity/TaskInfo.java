package kr.co.nexon.todoplus.Entity;


import android.renderscript.Int2;

import java.util.*;

/**
 * Created by raintype on 2015-01-20.
 */
public class TaskInfo {
    private int id;
    private String name;
    private Calendar period;
    private int dateType;
    private boolean important;
    private boolean secret;
    private boolean completed;
    private boolean useYN;

    public  int getId() { return id; }
    public  String getName() {
        return  name;
    }

    public Calendar getPeriod() { return  period; }

    public int getDateType() { return dateType; }

    public boolean getImportant() {
        return important;
    }

    public  boolean getSecret() {
        return secret;
    }

    public  boolean getCompleted() { return completed; }

    public  boolean getUseYN() { return  useYN; }

    public  void setName(String name) {
        this.name = name;
    }

    public  void setId(int id) { this.id = id; }

    public  void setPeriod(Calendar period) {
        this.period = period;
    }

    public  void setDateType(int dateType) { this.dateType = dateType; }

    public  void setImportant(boolean important) {
        this.important = important;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public  void setCompleted(boolean completed) { this.completed = completed; }

    public  void setUseYN(boolean useYN) { this.useYN = useYN; }

    public  TaskInfo() {
        name = "Empty Name";
        period = Calendar.getInstance();
        dateType = 0;
        important = false;
        secret = false;
        completed = false;
        useYN = true;
    }

    public  TaskInfo(String name, Calendar period, int dateType, boolean important, boolean secret, boolean completed, boolean useYN) {
        this.name = name;
        this.period = period;
        this.dateType = dateType;
        this.important = important;
        this.secret = secret;
        this.completed = completed;
        this.useYN = useYN;
    }


}