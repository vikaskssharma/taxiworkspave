package hertz.hertz.model;

public class Driver {


    String fName, lName;
    String number;
    String carType;

    public Driver(String fName, String lName, String number, String carType){
        this.fName = fName;
        this.lName = lName;
        this.number = number;
        this.carType = carType;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
