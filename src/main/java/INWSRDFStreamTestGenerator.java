

import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Resource;
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
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

public class INWSRDFStreamTestGenerator extends RdfStream implements Runnable {

	/** The logger. */
	protected final Logger logger = LoggerFactory
	  .getLogger(INWSRDFStreamTestGenerator.class);

	private int c = 1;
	private boolean keepRunning = false;
	private static String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
	private static String dul = "http://www.loa-cnr.it/ontologies/DUL.owl#";
	private static String inwsCore = "http://inwatersense.uni-pr.edu/ontologies/inws-core.owl#";
	private String inwsPoll = "http://inwatersense.uni-pr.edu/ontologies/inws-pollutants.owl#";
	private String geo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	private static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private StreamerAdapter streamerAdapter;

	public INWSRDFStreamTestGenerator(String iri) {
		super(iri); // inws/stream
		try {
			Class c = Class.forName("Adapter");
			this.streamerAdapter = (StreamerAdapter)c.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		System.out.println("Streaming of observations started...");
	}

	public void pleaseStop() {
		keepRunning = false;
	}

	@Override
	public void run() {
		keepRunning = true;
		while (keepRunning) {
			ArrayList<RdfQuadruple> data = streamerAdapter.getData();
			for (int counter = 0; counter < data.size(); counter++) {
				this.put(data.get(counter));
			}

			try {
				Thread.sleep(1000);  //2000000=26 min.        //1 000 ms = 1 sec, 60 000 = 1 min, 100 000 = 1.5 min, 200 000 ms = 3.3 min,  300 000 ms= 5 min
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	//krijon OWL dokumente nga stream-at
	public static String dumpRelatedStaticKnowledge(int maxUser) {

		Model m = ModelFactory.createDefaultModel();
		for (int j=0;j<maxUser;j++) {
			for (int i=0;i<5;i++) {
				m.add(new ResourceImpl("http://inwatersense.uni-pr.edu/user" + j+i), new PropertyImpl("http://inwatersense.uni-pr.edu/follows"), new ResourceImpl("http://inwatersense.uni-pr.edu/user" + j));
			}
		}
		StringWriter sw = new StringWriter();
		String fileName = "data.rdf";
		try {
			FileWriter fw = new FileWriter( fileName );
			m.write(fw, "RDF/XML");
			m.write(sw, "RDF/XML");
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(INWSRDFStreamTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
		}
		return sw.toString();
	}

	double randomWithRange(double min, double max)
	{
		//double range = (max - min) + 1;
		//return (double)(Math.random() * range) + min;

		Random r = new Random();
		double num = min + (max - min) * r.nextDouble();
		double truncatedAVGval = new BigDecimal(num).setScale(3, RoundingMode.HALF_UP).doubleValue();
		return truncatedAVGval;
	}




	public static void main(String[] args) {

		System.out.println(dumpRelatedStaticKnowledge(10));

	}
}
