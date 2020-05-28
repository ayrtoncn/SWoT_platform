

import java.io.File;
import java.io.StringWriter;

import java.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Random;
import java.util.logging.Level;

public class RDFStreamer extends RdfStream implements Observer {

	protected final Logger logger = LoggerFactory.getLogger(RDFStreamer.class);

	private boolean keepRunning = false;
	private StreamObservable streamObservable;

	public RDFStreamer(String iri, String className) {
		super(iri); // inws/stream
		try {
			String classLocation = new File("").getAbsolutePath();
			Class c = Class.forName(className);
			File f = new File(classLocation);
			URL[] cp = {f.toURI().toURL()};
			URLClassLoader urlcl = new URLClassLoader(cp);
			this.streamObservable = (StreamObservable)urlcl.loadClass(className).newInstance();
			this.streamObservable.addObserver(this);
			final Thread t = new Thread((Runnable) this.streamObservable);
			t.start();
//			this.streamerAdapter = (StreamerAdapter)c.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		System.out.println("Streaming of observations started...");
	}

	public void pleaseStop() {
		keepRunning = false;
	}

//	@Override
//	public void run() {
//		keepRunning = true;
//		while (keepRunning) {
//			ArrayList<RdfQuadruple> data = streamerAdapter.getData();
//			for (int counter = 0; counter < data.size(); counter++) {
//				this.put(data.get(counter));
//			}
//
//			try {
//				Thread.sleep(20000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//
//		}
//	}
	@Override
	public void update(Observable o, Object arg){
		RdfQuadruple rdfQuadruple = (RdfQuadruple) arg;
		this.put(rdfQuadruple);
	}
}
