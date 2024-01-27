package com.vatsal.android.digitaldetox.models;

import java.util.Calendar;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@Entity(tableName = "events", primaryKeys = {"startTime", "appName"})
public class DisplayEventEntity {

    @NonNull
    public String appName;
    public String date;
    public long startTime;
    public long endTime;
    public int ongoing;
    public String packageName;

    @Ignore
    public Drawable appIcon;

    public DisplayEventEntity() {

    }

    @Ignore
    public DisplayEventEntity(String packageName, long startTime, long endTime) {
        this.packageName = packageName;
        this.startTime = startTime;
        this.endTime = endTime;
        Date date1 = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        date = dateFormat.format(date1);
    }

    @Ignore
    public DisplayEventEntity(String packageName, long startTime, int ongoing) {
        this.packageName = packageName;
        this.startTime = startTime;
        this.ongoing = ongoing;
        Date date1 = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        date = dateFormat.format(date1);
    }
}
