import eu.larkc.csparql.core.ResultFormatter
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.ConsoleFormatter;
import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import eu.larkc.csparql.core.engine.RDFStreamFormatter;
import eu.larkc.csparql.readytogopack.streamer.BasicIntegerRDFStreamTestGenerator;
import eu.larkc.csparql.readytogopack.streamer.BasicRDFStreamTestGenerator;
import eu.larkc.csparql.readytogopack.streamer.CloudMonitoringRDFStreamTestGenerator;
import eu.larkc.csparql.readytogopack.streamer.DoorsTestStreamGenerator;
import eu.larkc.csparql.readytogopack.streamer.LBSMARDFStreamTestGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
//import static junit.framework.Assert.assertNotNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
class SWoT {
    private static Logger logger = LoggerFactory.getLogger(SWoT.class);
    private OWLOntology ontology;
    private OWLOntologyManager owlOntologyManager;
    private OWLDataFactory owlDataFactory;
    private SWRLRuleEngine ruleEngine;
    private SQWRLQueryEngine queryEngine;
    private PrefixManager prefixManager;
    private Boolean firstOntologyImport;
    private CsparqlEngine sparqlEngine;
    private OWLReasonerFactory reasonerFactory;
    private OWLReasoner owlReasoner;

    SWoT() {
        owlOntologyManager = OWLManager.createOWLOntologyManager();
        owlDataFactory = owlOntologyManager.getOWLDataFactory();
        firstOntologyImport = true;
    }

    // Load and create OWL Ontology
    void createNewOWL(String ontologyIRI) {
        IRI newOntologyIRI = IRI.create( ontologyIRI);
        this.ontology = this.owlOntologyManager.createOntology(newOntologyIRI);

    }
    void createLoadOWLFromFile(String path) {
        this.ontology = this.owlOntologyManager.loadOntologyFromOntologyDocument(new File(path));
    }

    void importOntology(String ontologyIRI, String ontologyPath) {
        InputStream inputCoreOntology = new FileInputStream(ontologyPath);
        IRI coreOntoIRI = IRI.create(ontologyIRI);
        SimpleIRIMapper iriMapper =  new SimpleIRIMapper(coreOntoIRI, IRI.create(new File(ontologyPath)));
        this.owlOntologyManager.getIRIMappers().add(iriMapper);
        OWLOntology owlCoreModel = this.owlOntologyManager.loadOntologyFromOntologyDocument(inputCoreOntology);
        if (firstOntologyImport){
            this.prefixManager = new DefaultPrefixManager(null, null, ontologyIRI);
            firstOntologyImport = false;
        }
        OWLDocumentFormat format = owlCoreModel.getOWLOntologyManager().getOntologyFormat(owlCoreModel);
        if (format.isPrefixOWLOntologyFormat())
            prefixManager.copyPrefixesFrom(format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());
        OWLImportsDeclaration importDeclaration = owlDataFactory.getOWLImportsDeclaration(coreOntoIRI);
        this.owlOntologyManager.applyChange(new AddImport(this.ontology, importDeclaration));
    }

    void printOntologyAndImports(@Nonnull OWLOntology ontology) {
        printOntology(owlOntologyManager, ontology);
        for (OWLOntology o : ontology.getImports()) {
            printOntology(manager, o);
        }
    }
    private  void printOntology(@Nonnull OWLOntology ontology) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        IRI documentIRI = owlOntologyManager.getOntologyDocumentIRI(ontology);
        System.out.println(ontologyIRI == null ? "anonymous" : ontologyIRI.toQuotedString());
        System.out.println(" from " + documentIRI.toQuotedString());
    }

    // SWRL & SQWRL
    void initSWRLEngine(){
        reasonerFactory = new StructuralReasonerFactory();
        owlReasoner = reasonerFactory.createReasoner(this.ontology);
        owlReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(this.ontology, this.prefixManager);
        queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(this.ontology, this.prefixManager);
    }

    void addNewRule(String ruleName, String rule) {
        ruleEngine.createSWRLRule(ruleName, rule);
    }

    void inferRule(){
        ruleEngine.infer();
    }

//    String queryAvgObs = null;
//    String queryIndObs = null;
//    String queryNAFObs = null;
//    RdfStream tg = null;

    // C-SPARQL

    void initSPARQLEngine(){
        sparqlEngine = new CsparqlEngineImpl();
        sparqlEngine.initialize(true);
    }

    // start RDF STREAMER

    CsparqlQueryResultProxy addQuery(String query){
        CsparqlQueryResultProxy resultProxy = null;
        try{
            resultProxy = this.sparqlEngine.registerQuery(query, false);
        } catch(ParseException ex){
            logger.error(ex.getMessage(), ex)
        }
        return resultProxy;
    }
//    if (resultProxy != null) {
//        resultProxy.addObserver(new RDFResultsFormatter(this.ruleEngine, this.ontology, this.owlReasoner, this.prefixManager, this.owlOntologyManager, this.owlDataFactory, this.queryEngine));
//    }
}

