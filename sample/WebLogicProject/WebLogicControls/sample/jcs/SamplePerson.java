package sample.jcs;

import java.lang.String;

public class SamplePerson {

    private String name;

    public SamplePerson(String name) {
        this.name = name;
    }

    public String sayHello() {
        return "Hello " + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}