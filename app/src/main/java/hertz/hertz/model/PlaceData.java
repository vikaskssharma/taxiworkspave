package hertz.hertz.model;

public class PlaceData {

    String id;
    String desc;


    public PlaceData(String id, String d){
        this.id = id;
        this.desc = d;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
