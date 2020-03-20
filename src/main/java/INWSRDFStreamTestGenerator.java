package streamer;
/*
 * @(#)TestGenerator.java   1.0   18/set/2009
 *
 * Copyright 2009-2009 Politecnico di Milano. All Rights Reserved.
 *
 * This software is the proprietary information of Politecnico di Milano.
 * Use is subject to license terms.
 *
 * @(#) $Id$
 */


import java.io.StringWriter;

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
import java.util.Random;
import java.util.logging.Level;

public class INWSRDFStreamTestGenerator extends RdfStream implements Runnable {

	/** The logger. */
	protected final Logger logger = LoggerFactory
	  .getLogger(INWSRDFStreamTestGenerator.class);

	private int c = 1;
	private boolean keepRunning = false;
	private String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
	private String dul = "http://www.loa-cnr.it/ontologies/DUL.owl#";
	private String inwsCore = "http://inwatersense.uni-pr.edu/ontologies/inws-core.owl#";
	private String inwsPoll = "http://inwatersense.uni-pr.edu/ontologies/inws-pollutants.owl#";
	private String geo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	private String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public INWSRDFStreamTestGenerator(final String iri) {
		super(iri); // inws/stream
		System.out.println("Streaming of observations started...");
	}

	public void pleaseStop() {
		keepRunning = false;
	}

	@Override
	public void run() {
		keepRunning = true;
		long tempTS;
		int n;
		double val = 0;

		//choose WQ parameter randomly
		String[] waterQualityArray = new String[]
		  {
//                  "pH",
			"BiochemicalOxygenDemand",
//                  "Arsenic",
//                  "BenthicInvertebrateFauna",
//                  "ChromiumIII",
//                  "Cyanide",
//                  "Diazinon",
//                  "Dimethoate",
//                  "Fluoride",
//                  "TotalAmmonia",
//                  "Glyphosate"
		  };
		int randomWQ;
		String WQ;
		int location_rnd;
		int ms_number = 10;
		while (keepRunning) {
			tempTS = System.currentTimeMillis();
			RdfQuadruple q;
			randomWQ = (int) randomWithRange(0, waterQualityArray.length);

			// n - per numer te individual-it, val - per vlera te observimeve
			n = (int) randomWithRange(0, 10000);
			//lokacioni
			location_rnd = (int) randomWithRange(9, ms_number+1);
			WQ = waterQualityArray[randomWQ];

			//generate sensor values based on WQ thresholds
			switch(WQ){
				case "Arsenic": val = randomWithRange(10, 30); break;
				case "BenthicInvertebrateFauna": val = randomWithRange(0.4, 0.9); break;
				case "ChromiumIII": val = randomWithRange(0, 5); break;
				case "Cyanide": val = randomWithRange(5, 12); break;
				case "Diazinon": val = randomWithRange(0, 0.015); break;
				case "pH": val = randomWithRange(1, 14); break;
				case "Dimethoate": val = randomWithRange(0, 1.2); break;
				case "BiochemicalOxygenDemand":
					val = randomWithRange(0.7, 2);    //bone 1 -> 0
					break;
				case "Fluoride": val = randomWithRange(300, 650); break;
				case "Glyphosate": val = randomWithRange(40, 75); break;
				case "TotalPhosphorus": val = randomWithRange(0, 15); break;
				case "TotalAmmonia": val = randomWithRange(0.001, 0.080); break;
				case "Copper": val = randomWithRange(0, 15); break;
				case "WaterHardness": val = randomWithRange(0, 200); break;
			}

			//System.out.println("# "+ this.c +": " + System.currentTimeMillis());
			q = new RdfQuadruple(super.getIRI() + "#obs_"+n, this.rdf + "type", this.ssn + "Observation", tempTS);
			this.put(q);
			//System.out.println(q.toString());
			q = new RdfQuadruple(super.getIRI() + "#obs_"+n, this.ssn + "qualityOfObservation", this.inwsCore + WQ, tempTS);
			this.put(q);
			//System.out.println(q.toString());
			q = new RdfQuadruple(super.getIRI() + "#obs_"+n, this.ssn + "observationResult", super.getIRI() + "#so_"+n, tempTS);
			this.put(q);
			//System.out.println(q.toString());
			q = new RdfQuadruple(super.getIRI() + "#so_"+n, this.ssn + "hasValue", super.getIRI() + "#ov_"+n, tempTS);
			this.put(q);
			//System.out.println(q.toString());
			q = new RdfQuadruple(super.getIRI() + "#ov_"+n, this.dul + "hasDataValue", val + "^^http://www.w3.org/2001/XMLSchema#double", tempTS);
			this.put(q);
			q = new RdfQuadruple(super.getIRI()+"#obs_" + n, this.inwsCore + "observationResultLocation", this.inwsCore + "ms" + location_rnd , tempTS);
			this.put(q);

			c++;

			try {
				Thread.sleep(1000);  //2000000=26 min.        //1 000 ms = 1 sec, 60 000 = 1 min, 100 000 = 1.5 min, 200 000 ms = 3.3 min,  300 000 ms= 5 min
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			location_rnd++;
			randomWQ++;
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
