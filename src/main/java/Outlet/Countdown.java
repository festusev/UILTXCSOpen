package Outlet;

import Outlet.challenge.ScoreEngine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Countdown {
    public final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
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

        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            public void run() {
                Scoreboard.generateScoreboard();
            }
        };

        timer.schedule(task, date); // Update scoreboard when this countdown finishes
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

    public String toString(){
        long diff = date.getTime() - getNow();

        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
        int diffHours = (int) (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        int diffMin = (int) ((diff % (1000 * 60 * 60)) / (1000 * 60));
        int diffSec = (int) ((diff % (1000 * 60)) / 1000);

        return diffDays + "<span>d</span> " + diffHours +"<span>h</span> " + diffMin + "<span>m</span> " + diffSec + "<span>s</span>" +
                "<script style='display:none'>" +
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
                "    document.getElementById('"+ID+"').innerHTML = days + '<span>d</span> ' + hours + '<span>h</span> '" +
                "        + minutes + '<span>m</span> ' + seconds + '<span>s</span>';" +
                "    if(countdownDate"+ID+" - (now-cntdwnLoaded"+ID+")< 0){" +
                "       clearInterval(x"+ID+");" + onDone +
                "    }" +
                "}, 1000);</script>";
    }
}