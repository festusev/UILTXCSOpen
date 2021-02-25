package Outlet;

//import Outlet.uil.CS;

import Outlet.uil.Competition;
import Outlet.uil.UIL;

import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener("application context listener")
public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            UIL.initialize();
            UserMap.initialize();
            UIL.initializeJudges();
            UIL.sortFRQResponses();
            Team.initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*
        // Finally, schedule the VerificationFlusher class to be called every 15 minutes
        VerificationFlusher vflush = new VerificationFlusher();
        Timer time = new Timer();
        time.scheduleAtFixedRate(vflush, 1000*60*15, 1000*60*15);*/
    }
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do nothing
    }

}