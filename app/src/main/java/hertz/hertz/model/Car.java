package hertz.hertz.model;

/**
 * Created by tonnyquintos on 11/7/15.
 */
public class Car {

    String name;
    int price3Hours, price5Hours, priceExcess;
    int image;
    String desc;


    public Car(String name, int p3, int p5, int pExcess, int image, String desc){
        this.name = name;
        this.price3Hours = p3;
        this.price5Hours = p5;
        this.priceExcess = pExcess;
        this.image = image;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice3Hours() {
        return price3Hours;
    }

    public void setPrice3Hours(int price3Hours) {
        this.price3Hours = price3Hours;
    }

    public int getPrice5Hours() {
        return price5Hours;
    }

    public void setPrice5Hours(int price5Hours) {
        this.price5Hours = price5Hours;
    }

    public int getPriceExcess() {
        return priceExcess;
    }

    public void setPriceExcess(int priceExcess) {
        this.priceExcess = priceExcess;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
