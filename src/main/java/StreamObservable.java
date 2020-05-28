import eu.larkc.csparql.cep.api.RdfQuadruple;

import java.util.Observable;

public abstract class StreamObservable extends Observable implements Runnable {


    public void put(RdfQuadruple q) {
        this.setChanged();
        this.notifyObservers(q);
    }
}
