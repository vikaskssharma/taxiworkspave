package hertz.hertz.model;

/**
 * Created by Alienware Vin on 6/26/2015.
 */
public class User {

    String name, username, password;
    int age;

    public User (String name, int age, String username, String password){
        this.name = name;
        this.age = age;
        this.username = username;
        this.password = password;
    }

    public User (String username, String password){
        this.username = username;
        this.password = password;
        this.age = -1;
        this.name = "";
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
