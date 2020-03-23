import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;
import org.semanticweb.owlapi.apibinding.OWLManager;
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

	RDFResultsFormatter(SWRLRuleEngine ruleEngine, OWLOntology ontology, OWLReasoner reasoner,
						DefaultPrefixManager prefixManager, OWLOntologyManager owlOntologyManager, OWLDataFactory owlDataFactory, SQWRLQueryEngine queryEngine, String converterPath) {
		this.ruleEngine = ruleEngine;
		this.ontology = ontology;
		this.reasoner = reasoner;
		this.prefixManager = prefixManager;
		this.owlOntologyManager = owlOntologyManager;
		this.owlDataFactory = owlDataFactory;
		this.queryEngine = queryEngine;
		this.converterPath = converterPath;
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
		OWLOntology owlOntology = man.loadOntologyFromOntologyDocument(inputStream);
		for (OWLAxiom ax : owlOntology.getAxioms()) {
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
			FileInputStream fileInputStream = new FileInputStream(this.converterPath);
			String text = getFileContent(fileInputStream);

			for (final RDFTuple t : res) {
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
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				this.ruleEngine.infer();
				stopWatch.stop();
				System.out.println("(C-SWRL) executed in " + ": " + stopWatch.getTotalTimeMillis() + " ms");
				totProcTime = totProcTime + stopWatch.getTotalTimeMillis();
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

				OWLClass clstmpModerateBOD = owlDataFactory.getOWLClass("inwsr:ModerateBODMeasurement", prefixManager);
				OWLClass clstmpGoodBOD = owlDataFactory.getOWLClass("inwsr:GoodBODMeasurement", prefixManager);
				OWLClass clstmpHighBOD = owlDataFactory.getOWLClass("inwsr:HighBODMeasurement", prefixManager);
				printModerateClassIndividuals(clstmpModerateBOD);  //prints also the pollution sources
				printGoodHighClassIndividuals(clstmpHighBOD, "HIGH");//direct and indirect instances
				printGoodHighClassIndividuals(clstmpGoodBOD, "GOOD");  //direct and indirect instances
				removeClassIndividuals(clstmpModerateBOD);
				removeClassIndividuals(clstmpHighBOD);
				removeClassIndividuals(clstmpGoodBOD);

				OWLClass tmpObservation = owlDataFactory.getOWLClass("ssn:Observation", this.prefixManager);
				removeClassIndividuals(tmpObservation);
				//TODO: find way to remove load ontology

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
	public void printModerateClassIndividuals(OWLClass cl) {
		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(cl, false);
		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		for (OWLNamedIndividual ind : individuals) {
			String indName = ind.getIRI().toString().substring(ind.getIRI().toString().indexOf("#") + 1);
			System.out.println("MODERATE status detected: " + indName);
			// look up all property assertions
			OWLObjectProperty foundPollutionSourcesOP = owlDataFactory.getOWLObjectProperty("inwsr:foundPollutionSources", prefixManager);
			for (OWLObjectProperty op : ontology.getObjectPropertiesInSignature()) {
				assert op != null;
				NodeSet<OWLNamedIndividual> foundPollSourcesNodeSet = reasoner.getObjectPropertyValues(ind, op);
				for (OWLNamedIndividual pollSourcesInd : foundPollSourcesNodeSet.getFlattened()) {
					if (op.getIRI().equals(foundPollutionSourcesOP.getIRI())) {
						//assertNotNull(value);
						// use the value individuals
						System.out.println("Pollution source: " + ReturnIRIResourceName(pollSourcesInd.getIRI().toString()).replace('_', ' '));
					}
				}
			}
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
	public void printGoodHighClassIndividuals(OWLClass cl, String statusName) {
		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(cl, false);
		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		for (OWLNamedIndividual ind : individuals) {
			String indName = ind.getIRI().toString().substring(ind.getIRI().toString().indexOf("#") + 1);
			if (!indName.equals("GoodBODMeasurement_11")) {
				System.out.println(statusName + " status detected: " + indName);
			}

			// look up all property assertions
//            OWLObjectProperty foundPollutionSourcesOP = _owlDataFactory.getOWLObjectProperty("inwsr:foundPollutionSources", _prefixManager);
//            for (OWLObjectProperty op : _newOnto.getObjectPropertiesInSignature()) {
//                assert op != null;
//                NodeSet<OWLNamedIndividual> foundPollSourcesNodeSet = _reasoner.getObjectPropertyValues(ind, op);
//                for (OWLNamedIndividual pollSourcesInd : foundPollSourcesNodeSet.getFlattened()) {
//                    if(op.getIRI().equals(foundPollutionSourcesOP.getIRI()))
//                    {
//                        //assertNotNull(value);
//                        // use the value individuals
//                        System.out.println("Pollution source: " + pollSourcesInd);
//                    }
//                }
//            }
		}
	}

}