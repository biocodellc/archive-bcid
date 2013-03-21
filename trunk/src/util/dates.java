package util;

import java.lang.String;import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
* Convenience class for working with dates related to bcids.
 */
public class dates {

     /**
     * Record all notions of now() for bcids in a consistent manner
     *
     * @return A string representation of this time (now)
     */
    public String now() {
        SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssZ");
        formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatUTC.format(new Date());
    }
}
