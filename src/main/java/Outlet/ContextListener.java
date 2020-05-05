package Outlet;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener("application context listener")
public class ContextListener implements ServletContextListener {

    /**
     * Initialize log4j when the application is being started
     */
    //public static final Logger LOGGER = Logger.getLogger(ContextListener.class.getName());
    @Override
    public void contextInitialized(ServletContextEvent event) {
        //LOGGER.info("Initializing Server Context");
        SimpleDateFormat sdf = new SimpleDateFormat(Dynamic.DATETIME_FORMAT, Locale.ENGLISH);  // Lets us make dates easily
        sdf.setTimeZone(TimeZone.getTimeZone("CST"));
        Dynamic.sdf = sdf;
        try {
            Dynamic.cntdwnToCmp = sdf.parse(Dynamic.CNTDWNCMP_DATE);
            Dynamic.cntdwnToMCOver = sdf.parse(Dynamic.CNTDWNMCENDS_DATE);
            Dynamic.cntdwnToCMPOver = sdf.parse(Dynamic.CNTDWNCMPENDS_DATE);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Finally, schedule the VerificationFlusher class to be called every 15 minutes
        VerificationFlusher vflush = new VerificationFlusher();
        Timer time = new Timer();
        time.scheduleAtFixedRate(vflush, 1000*60*15, 1000*60*15);
    }
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do nothing
    }

}