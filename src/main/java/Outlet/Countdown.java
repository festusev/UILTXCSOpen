package Outlet;

import Outlet.challenge.ScoreEngine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Countdown {
    public static final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public SimpleDateFormat sdf;

    public final String DATE_STRING;
    public Date date = null;     // Automatically set on startup. A date object of DATE_STRING

    public final String ID; // The html id of the countdown object
    public String onDone = "updateNav();";  // Javascript statements to execute when done

    // Constructor called if we are making a timer
    public Countdown (long timerLength, long started, String id){
        DATE_STRING = "";
        date = new Date(started + timerLength);
        ID = id;
    }
    public Countdown (String toDate, String id) {
        sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.ENGLISH);  // Lets us make dates easily
        sdf.setTimeZone(TimeZone.getTimeZone("CST"));
        DATE_STRING = toDate;
        try {
            date = sdf.parse(DATE_STRING);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ID = id;
    }

    public Countdown (String toDate) {  // If we don't want to actually have a countdown
        DATE_STRING = toDate;
        ID = "";
    }

    // Creates a date from epoch milli
    public Countdown (long toDate, String id) {
        date = new Date(toDate);
        ID = id;

        DATE_STRING = "";
    }
    public static long getNow(){
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "America/Chicago" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        return zdt.toInstant().toEpochMilli();
    }
    // Returns true if the countdown is over
    public boolean done(){
        return date.getTime() - getNow() <= 0;
    }

    // Returns true if the timer is in the overflow time. Technically, time is up, but there is a threshold in milliseconds
    // when this returns true. Returns false if time is not up.
    public boolean inOverflow(long thresholdLength) {
        long time = date.getTime();
        long now = getNow();
        long diff = now - time;
        return (diff > 0) && (diff < thresholdLength);
    }

    public String getScript(String onDone) {
        if(date == null) return "";

        long diff = date.getTime() - getNow();

        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
        int diffHours = (int) (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        int diffMin = (int) ((diff % (1000 * 60 * 60)) / (1000 * 60));
        int diffSec = (int) ((diff % (1000 * 60)) / 1000);

        return diffDays + "<span>d</span> " + diffHours +"<span>h</span> " + diffMin + "<span>m</span> " + diffSec + "<span>s</span>" +
                "<script style='display:none'>" +
                "(function() {" +
                "var countdownDate"+ID+" = "+diff+";" +
                "var compOpen"+ID+" = false;" +
                "var cntdwnLoaded"+ID+" = new Date().getTime();" +
                "var x"+ID+" = setInterval(function() {" +
                "    var now = new Date().getTime();" +
                "    var distance = countdownDate"+ID+" - (now-cntdwnLoaded"+ID+");" +
                "    var days = Math.floor(distance / (1000 * 60 * 60 * 24));" +
                "    var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));" +
                "    var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));" +
                "    var seconds = Math.floor((distance % (1000 * 60)) / 1000);" +
                "    if(countdownDate"+ID+" - (now-cntdwnLoaded"+ID+")< 0){" +
                "       document.getElementById('"+ID+"').innerHTML =  '0<span>d</span> 0<span>h</span> 0<span>m</span> 0<span>s</span>';" +
                "       clearInterval(x"+ID+");" + onDone +
                "    } else {" +
                "       document.getElementById('"+ID+"').innerHTML = days + '<span>d</span> ' + hours + '<span>h</span> '" +
                "            + minutes + '<span>m</span> ' + seconds + '<span>s</span>';" +
                "    }" +
                "}, 1000);})();</script>";
    }

    public String toString(){
        return getScript(onDone);
    }

    // Returns a new countdown object 'add' milliseconds after the start of 'countdown'
    public static Countdown add(Countdown countdown, long add, String id) {
        return new Countdown(countdown.date.getTime() + add, id);
    }

    public static Countdown getEarliest(Countdown c1, Countdown c2) {
        if(c1.date.getTime() < c2.date.getTime()) return c1;
        return c2;
    }

    public static Countdown getLatest(Countdown c1, Countdown c2) {
        if(c1.date.getTime() >= c2.date.getTime()) return c1;
        return c2;
    }
}