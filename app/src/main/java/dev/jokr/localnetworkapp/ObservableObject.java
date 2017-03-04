package dev.jokr.localnetworkapp;

import java.util.Observable;

/**
 * Created by razon30 on 26-02-17.
 */

public class ObservableObject extends Observable {

    public static ObservableObject instance = new ObservableObject();

    public  static ObservableObject getInstance(){
        return instance;
    }

    private ObservableObject(){}

    public void updatevalue(Object data){
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }

}
