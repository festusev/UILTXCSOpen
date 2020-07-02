package Outlet.uil;

import java.io.File;

public class Pair{
    public File key;
    public File value;

    public Pair(File key, File value){
        this.key = key;
        this.value = value;
    }

    public File getKey(){
        return key;
    }
    public File getValue(){
        return value;
    }
}
