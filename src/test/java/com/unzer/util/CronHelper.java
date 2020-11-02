package com.unzer.util;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

public class CronHelper {
    private ZonedDateTime currentUtcDate;
    private StringBuilder cronExpression = new StringBuilder();
    private StringBuilder secondsExpression = new StringBuilder();
    private StringBuilder minutesExpression = new StringBuilder();
    private StringBuilder hourExpression = new StringBuilder();
    Logger log = Logger.getAnonymousLogger();

    @SneakyThrows
    public CronHelper() {
        currentUtcDate = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public static CronHelper createNew() {
        return new CronHelper();
    }

    public CronHelper afterSeconds(int seconds) {
        currentUtcDate = currentUtcDate.plusSeconds(seconds);
        secondsExpression.append(currentUtcDate.getSecond()).append(",");
        return this;
    }

    public CronHelper and() {
        return this;
    }

    public CronHelper inCurrentMinute() {
        minutesExpression.append(currentUtcDate.getMinute());
        return this;
    }

    public CronHelper inCurrentHour() {
        hourExpression.append(currentUtcDate.getHour());
        return this;
    }

    public String getExpression() {
        if (secondsExpression.length() == 0)
            secondsExpression.append("*");
        else
            secondsExpression.deleteCharAt(secondsExpression.lastIndexOf(","));;

            if (minutesExpression.length() == 0)
            minutesExpression.append("*");
        if (hourExpression.length() == 0)
            hourExpression.append("*");


        String cronString = cronExpression.append(secondsExpression).append(" ")
                .append(minutesExpression).append(" ")
                .append(hourExpression).append(" ")
                .append(currentUtcDate.getDayOfMonth())
                .append(" ").append(currentUtcDate.getMonthValue())
                .append(" ? ").append(currentUtcDate.getYear()).toString();

        log.info("Cron expression under use:"+cronString);
        return cronString;
    }

    public String getUtcDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentUtcDate.format(formatter);
    }


}
