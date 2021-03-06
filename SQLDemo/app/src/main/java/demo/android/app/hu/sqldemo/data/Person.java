package demo.android.app.hu.sqldemo.data;


import com.j256.ormlite.field.DatabaseField;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Peter on 2014.10.12..
 */
public class Person {

    // id is generated by the database and set on the object automagically
    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    String name;
    @DatabaseField
    Date birthDay;

    Person() {
        // needed by ormlite
    }

    public Person(String name, Date birthDay) {
        this.name = name;
        this.birthDay = birthDay;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(name);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");
        sb.append(", ").append("date=").append(dateFormatter.format(birthDay));
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getBirthDay() {
        return birthDay;
    }
}