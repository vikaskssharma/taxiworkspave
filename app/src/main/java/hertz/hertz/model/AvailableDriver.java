package hertz.hertz.model;

/**
 * Created by rsbulanon on 11/23/15.
 */
public class AvailableDriver {

    private String driverId;
    private String firstName;
    private String lastName;
    private String plateNo;
    private String contactNo;
    private boolean available;

    public AvailableDriver() {
    }

    public AvailableDriver(String driverId, String firstName, String lastName,
                           String plateNo, String contactNo, boolean available) {
        this.driverId = driverId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.plateNo = plateNo;
        this.contactNo = contactNo;
        this.available = available;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
