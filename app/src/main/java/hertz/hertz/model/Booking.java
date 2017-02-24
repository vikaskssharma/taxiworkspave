package hertz.hertz.model;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by rsbulanon on 1/5/16.
 */
@ParseClassName("Booking")
public class Booking extends ParseObject {

    public Booking() {
    }

    private ParseGeoPoint origin;
    private double destiLatitude;
    private double destiLongitude;
    private ParseUser user;
    private String from;
    private String to;
    private String bookedBy;
    private String status;
    private String driverName;
    private int hoursToRent;
    private String txType;

    public ParseGeoPoint getOrigin() {
        return getParseGeoPoint("origin");
    }

    public void setOrigin(ParseGeoPoint origin) {
        put("origin",origin);
    }

    public double getDestiLatitude() {
        return getNumber("destiLatitude").doubleValue();
    }

    public void setDestiLatitude(double destiLatitude) {
        put("destiLatitude",destiLatitude);
    }

    public double getDestiLongitude() {
        return getNumber("destiLongitude").doubleValue();
    }

    public void setDestiLongitude(double destiLongitude) {
        put("destiLongitude",destiLongitude);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user",user);
    }

    public String getFrom() {
        return getString("from");
    }

    public void setFrom(String from) {
        put("from",from);
    }

    public String getTo() {
        return getString("to");
    }

    public void setTo(String to) {
        put("to",to);
    }

    public String getBookedBy() {
        return getString("bookedBy");
    }

    public void setBookedBy(String bookedBy) {
        put("bookedBy",bookedBy);
    }

    public String getStatus() {
        return getString("status");
    }

    public void setStatus(String status) {
        put("status",status);
    }

    public String getDriverName() {
        return getString("driverName");
    }

    public void setDriverName(String driverName) {
        put("driverName",driverName);
    }

    public int getHoursToRent() {
        return getNumber("hoursToRent").intValue();
    }

    public void setHoursToRent(int hoursToRent) {
        put("hoursToRent",hoursToRent);
    }

    public String getTxType() {
        return getString("txType");
    }

    public void setTxType(String txType) {
        put("txType",txType);
    }
}
