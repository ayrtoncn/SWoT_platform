import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.util.PrintUtil;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import streamer.INWSRDFStreamTestGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;

public class Main {
	private Model model;
	public String iri = "http://inwatersense.uni-pr.edu/stream";
	public  String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
	public  String dul = "http://www.loa-cnr.it/ontologies/DUL.owl#";
	public  String inwsCore = "http://inwatersense.uni-pr.edu/ontologies/inws-core.owl#";
	public String inwsPoll = "http://inwatersense.uni-pr.edu/ontologies/inws-pollutants.owl#";
	public String geo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	public  String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public Main() {
		model = ModelFactory.createDefaultModel();
	}
	public void addStatement(String s, String p, String o){
		Resource subject = model.createResource(s);
		Property predicate = model.createProperty(p);
		RDFNode object = model.createResource(o);
		Statement stmt = model.createStatement(subject, predicate, object);
		model.add(stmt);
	}

	public void addStatement(String s, String p, Double o){
		Resource subject = model.createResource(s);
		Property predicate = model.createProperty(p);
		RDFNode object = model.createTypedLiteral(o);
		Statement stmt = model.createStatement(subject, predicate, object);
		model.add(stmt);
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
		Main main = new Main();
		int n = 1;
		String WQ = "BiochemicalOxygenDemand";
		Double val = main.randomWithRange(0.7, 2);
		int location_rnd = 9;
		main.addStatement(main.iri + "#obs_"+n, main.rdf + "type", main.ssn + "Observation");
		main.addStatement(main.iri + "#obs_"+n,main.ssn + "qualityOfObservation", main.inwsCore + WQ);
		main.addStatement(main.iri + "#obs_"+n,main.ssn + "observationResult", main.iri + "#so_"+n);
		main.addStatement(main.iri + "#so_"+n,main.ssn + "hasValue", main.iri + "#ov_"+n);
		main.addStatement(main.iri + "#ov_"+n,main.dul + "hasDataValue", val);
		main.addStatement(main.iri + "#obs_"+n,main.inwsCore + "observationResultLocation", main.inwsCore + "ms" + location_rnd);
		StringWriter sw = new StringWriter();
		String fileName = "data.rdf";
		try {
			FileWriter fw = new FileWriter( fileName );
			RDFDataMgr.write(fw, main.model,Lang.NQUADS);
			RDFDataMgr.write(sw, main.model,Lang.NQUADS);
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(INWSRDFStreamTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
		}
		long tempTS = System.currentTimeMillis();
//		StreamRDFWriter.write ( out, m.getGraph (), jlang.getLeft () );
//		Dataset dataset = DatasetFactory.create(main.model);
//		String queryString = "CONSTRUCT { GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} } WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
//		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
//		try  {
//			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
//			Iterator<Quad> quads = qexec.execConstructQuads();
//			PrintUtil.printOut(quads);
//		}

	}
}
