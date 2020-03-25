import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import org.apache.jena.atlas.json.JsonArray;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semarglproject.vocab.RDF;
import org.springframework.util.StopWatch;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.sqwrl.SQWRLQueryEngine;

import javax.annotation.Nonnull;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;

public class RDFResultsFormatter extends ResultFormatter {
	SWRLRuleEngine ruleEngine;
	OWLOntology ontology;
	OWLReasoner reasoner;
	DefaultPrefixManager prefixManager;
	OWLOntologyManager owlOntologyManager;
	OWLDataFactory owlDataFactory;
	SQWRLQueryEngine queryEngine;
	String converterPath;
	JSONArray results;
	private OWLOntology converterOntology;

	RDFResultsFormatter(SWRLRuleEngine ruleEngine, OWLOntology ontology, OWLReasoner reasoner,
						DefaultPrefixManager prefixManager, OWLOntologyManager owlOntologyManager, OWLDataFactory owlDataFactory, SQWRLQueryEngine queryEngine, String converterPath, JSONArray results) {
		this.ruleEngine = ruleEngine;
		this.ontology = ontology;
		this.reasoner = reasoner;
		this.prefixManager = prefixManager;
		this.owlOntologyManager = owlOntologyManager;
		this.owlDataFactory = owlDataFactory;
		this.queryEngine = queryEngine;
		this.converterPath = converterPath;
		this.results = results;
	}


	public void addOWLClass(String owlIndividualName, String owlClassName) {
		OWLIndividual owlIndividual = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualName));
		OWLClass owlClass = this.owlDataFactory.getOWLClass(owlClassName, this.prefixManager);
		OWLClassAssertionAxiom classAxiom = this.owlDataFactory.getOWLClassAssertionAxiom(owlClass, owlIndividual);
		this.owlOntologyManager.applyChange(new AddAxiom(this.ontology, classAxiom));
	}

	public void addOWLObjectProperty(String owlIndividualSubjectName, String owlObjectPropertyName,String owlIndividualObjectName) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLObjectProperty owlObjectProperty = owlDataFactory.getOWLObjectProperty(owlObjectPropertyName, prefixManager);
		OWLIndividual owlIndividualObject = owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualObjectName));
		OWLObjectPropertyAssertionAxiom assertionAxiom = owlDataFactory.getOWLObjectPropertyAssertionAxiom(owlObjectProperty, owlIndividualSubject, owlIndividualObject);
		owlOntologyManager.applyChange(new AddAxiom(ontology, assertionAxiom));
	}
	
	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,int value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		owlOntologyManager.applyChange(new AddAxiom(ontology, dataTypeAssertion));
	}

	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,double value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		owlOntologyManager.applyChange(new AddAxiom(ontology, dataTypeAssertion));
	}


	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,float value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		owlOntologyManager.applyChange(new AddAxiom(ontology, dataTypeAssertion));
	}

	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,boolean value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		owlOntologyManager.applyChange(new AddAxiom(ontology, dataTypeAssertion));
	}

	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,String value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		owlOntologyManager.applyChange(new AddAxiom(ontology, dataTypeAssertion));
	}

	void loadOWLFromFile(InputStream inputStream) throws OWLOntologyCreationException, FileNotFoundException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		this.converterOntology = man.loadOntologyFromOntologyDocument(inputStream);
		for (OWLAxiom ax : this.converterOntology.getAxioms()) {
			this.owlOntologyManager.applyChange(new AddAxiom(this.ontology, ax));
		}
	};

	public String getValue(String value){
		if(value.indexOf("^^") >= 0 ){
			String temp =  value.substring(value.indexOf("\"") + 1);
			return temp.substring(0, temp.indexOf("\""));
		}else{
			return value.substring(value.indexOf("#") + 1);
		}
	}
	@Override
	public void update(Observable o, Object arg){
		RDFTable res = (RDFTable) arg;

		System.out.println("+++++++ " + res.size() + " new result(s) at SystemTime=[" + System.currentTimeMillis() + "] +++++++");
		int numerator = 0;
		long totProcTime = 0;
		try {


			for (final RDFTuple t : res) {
				FileInputStream fileInputStream = new FileInputStream(this.converterPath);
				String text = getFileContent(fileInputStream);
				numerator++;
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date date = new Date();
				String obsTime = dateFormat.format(date);
				String arg0 = getValue(t.get(0));
				String arg1 = getValue(t.get(1));
				String arg2 = getValue(t.get(2));
				int n = (int) randomWithRange(0, 10000);
				System.out.println("#" + numerator + " (C-SPARQL) arg0: " + arg0 + " arg1: " + arg1 + " arg2: " + arg2 + " [" + obsTime + "]" + "n " + n);
				text = text.replaceAll("&arg0", arg0);
				text = text.replaceAll("&arg1", arg1);
				text = text.replaceAll("&arg2", arg2);
				text = text.replaceAll("&n", String.valueOf(n));
				text = text.replaceAll("&time", obsTime);
				InputStream newFileInputStream = new ByteArrayInputStream(text.getBytes());
				loadOWLFromFile(newFileInputStream);
				saveOWL(ontology.getOWLOntologyManager().getOntologyFormat(ontology), owlOntologyManager, ontology, "condition.owl");
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				this.ruleEngine.infer();
				stopWatch.stop();
				System.out.println("(C-SWRL) executed in " + ": " + stopWatch.getTotalTimeMillis() + " ms");
				totProcTime = totProcTime + stopWatch.getTotalTimeMillis();
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

//				OWLClass clstmpModerateBOD = owlDataFactory.getOWLClass("inwsr:ModerateBODMeasurement", prefixManager);
//				OWLClass clstmpGoodBOD = owlDataFactory.getOWLClass("inwsr:GoodBODMeasurement", prefixManager);
//				OWLClass clstmpHighBOD = owlDataFactory.getOWLClass("inwsr:HighBODMeasurement", prefixManager);
//				printClassIndividuals(clstmpModerateBOD,"MEDIUM");
//				printClassIndividuals(clstmpHighBOD, "HIGH");
//				printClassIndividuals(clstmpGoodBOD, "GOOD");
//				removeClassIndividuals(clstmpModerateBOD);
//				removeClassIndividuals(clstmpHighBOD);
//				removeClassIndividuals(clstmpGoodBOD);

				for(int i = 0; i < this.results.size(); i++){
					JSONObject result = (JSONObject)this.results.get(i);
					JSONArray states = (JSONArray)result.get("states");
					Boolean correct = true;
					OWLClass owlClass = null;
					String state = "";
					for(int j= 0; j< states.size(); j++) {
						owlClass= owlDataFactory.getOWLClass((String)states.get(j), prefixManager);
						NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(owlClass, false);
						if(individualsNodeSet.isEmpty()){
							correct = false;
						}else{
							state += states.get(j) + " ";
						}
					}
					if(result.get("action").equals("PRINT") && correct){
						printClassIndividuals(owlClass,state);

					}
					removeClassIndividuals(owlClass);

				}


				OWLClass tmpObservation = owlDataFactory.getOWLClass("ssn:Observation", this.prefixManager);
				for (OWLAxiom ax : this.converterOntology.getAxioms()) {
					owlOntologyManager.removeAxiom(ontology, ax);
				}
//				removeClassIndividuals(tmpObservation);
				saveOWL(ontology.getOWLOntologyManager().getOntologyFormat(ontology), owlOntologyManager, ontology, "result.owl");

			}
		}catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

	}

	private double randomWithRange(double min, double max) {
		double range = (max - min) + 1;
		return (Math.random() * range) + min;
	}

	public static String getFileContent(FileInputStream fis ) throws IOException
	{
		try( BufferedReader br = new BufferedReader( new InputStreamReader(fis )))
		{
			StringBuilder sb = new StringBuilder();
			String line;
			while(( line = br.readLine()) != null ) {
				sb.append( line );
				sb.append( '\n' );
			}
			return sb.toString();
		}
	}
	public String ReturnIRIResourceName(String iri) {
		return iri.substring(iri.indexOf("#") + 1);
	}

	public void removeClassIndividuals(OWLClass cl) {
		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(cl, false);
		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		for (OWLNamedIndividual ind : individuals) {
			//System.out.println("Class: "+ cl + "\nIndividual: " + ind);
			OWLClassAssertionAxiom newClassAxiom = owlDataFactory.getOWLClassAssertionAxiom(cl, ind);
			owlOntologyManager.removeAxiom(ontology, newClassAxiom);
		}
	}
	public void printClassIndividuals(OWLClass cl, String state) {
		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(cl, false);
		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		for (OWLNamedIndividual ind : individuals) {
			String indName = ind.getIRI().toString().substring(ind.getIRI().toString().indexOf("#") + 1);
			System.out.println(indName + " : " + state);
		}
	}

	private static void saveOWL(OWLDocumentFormat format, OWLOntologyManager manager, OWLOntology newOnto, String name) {
		File fileformated = new File("D:\\Code\\Semester 7\\Tugas Akhir\\InWaterSense\\Ontologies\\" + name);
		OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
		if (format.isPrefixOWLOntologyFormat()) {
			owlxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
		}
		try {
			manager.saveOntology(newOnto, owlxmlFormat, IRI.create(fileformated.toURI()));
		} catch (Exception e) {

		}
	}

}