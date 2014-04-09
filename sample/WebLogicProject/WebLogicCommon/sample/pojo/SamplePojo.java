package sample.pojo;

import sample.rename.me.SampleRename;

public class SamplePojo {

    private String name;

    private SampleRename sampleRename = new sample.rename.me.SampleRename();

    public String getName() {
        return sampleRename.getName();
    }

    public void setName(String name) {
        this.name = name;
        sampleRename.setName(name);
    }
}
