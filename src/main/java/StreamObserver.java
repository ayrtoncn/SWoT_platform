import java.util.Observable;
import java.util.Observer;

public class StreamObserver implements Observer{
    private Observable ov = null;
    public StreamObserver(Observable ov)
    {
        this.ov = ov;
    }
    @Override
    public void update(Observable o, Object arg){

    }
}
