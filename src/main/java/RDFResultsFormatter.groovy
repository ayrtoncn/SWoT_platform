import eu.larkc.csparql.core.ResultFormatter;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.sqwrl.SQWRLQueryEngine;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;

abstract class RDFResultsFormatter extends ResultFormatter {
	SWRLRuleEngine ruleEngine;
	OWLOntology newOnto;
	OWLReasoner reasoner;
	DefaultPrefixManager prefixManager;
	OWLOntologyManager manager;
	OWLDataFactory owlDataFactory;
	SQWRLQueryEngine queryEngine;

	RDFResultsFormatter(SWRLRuleEngine ruleEngine, OWLOntology newOnto, OWLReasoner reasoner,
						DefaultPrefixManager prefixManager, OWLOntologyManager manager, OWLDataFactory owlDataFactory, SQWRLQueryEngine queryEngine) {
		this.ruleEngine = ruleEngine;
		this.newOnto = newOnto;
		this.reasoner = reasoner;
		this.prefixManager = prefixManager;
		this.manager = manager;
		this.owlDataFactory = owlDataFactory;
		this.queryEngine = queryEngine;
	}

	abstract void OWLFormatter();

	public void update(Observable o, Object arg){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = new Date();
		String obsTime = dateFormat.format(date);
		OWLFormatter();
	}

	public void addOWLClass(String owlIndividualName, String owlClassName) {
		OWLIndividual owlIndividual = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualName));
		OWLClass owlClass = this.owlDataFactory.getOWLClass(owlClassName, this.prefixManager);
		OWLClassAssertionAxiom classAxiom = this.owlDataFactory.getOWLClassAssertionAxiom(owlClass, owlIndividual);
		this.manager.applyChange(new AddAxiom(this.newOnto, classAxiom));
	}

	public void addOWLObjectProperty(String owlIndividualSubjectName, String owlObjectPropertyName,String owlIndividualObjectName) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLObjectProperty owlObjectProperty = owlDataFactory.getOWLObjectProperty(owlObjectPropertyName, prefixManager);
		OWLIndividual owlIndividualObject = owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualObjectName));
		OWLObjectPropertyAssertionAxiom assertionAxiom = owlDataFactory.getOWLObjectPropertyAssertionAxiom(owlObjectProperty, owlIndividualSubject, owlIndividualObject);
		manager.applyChange(new AddAxiom(newOnto, assertionAxiom));
	}
	
	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,int value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		manager.applyChange(new AddAxiom(newOnto, dataTypeAssertion));
	}

	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,double value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		manager.applyChange(new AddAxiom(newOnto, dataTypeAssertion));
	}


	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,float value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		manager.applyChange(new AddAxiom(newOnto, dataTypeAssertion));
	}

	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,boolean value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		manager.applyChange(new AddAxiom(newOnto, dataTypeAssertion));
	}

	public void addOWLDataProperty(String owlIndividualSubjectName, String owlObjectPropertyName,String value) {
		OWLIndividual owlIndividualSubject = this.owlDataFactory.getOWLNamedIndividual(IRI.create(owlIndividualSubjectName));
		OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(owlObjectPropertyName, prefixManager);
		OWLAxiom dataTypeAssertion = owlDataFactory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlIndividualSubject, value);
		manager.applyChange(new AddAxiom(newOnto, dataTypeAssertion));
	}

}