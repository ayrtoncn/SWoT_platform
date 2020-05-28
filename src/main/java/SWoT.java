import com.google.gson.JsonObject;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;

import javax.annotation.Nonnull;
import java.io.*;
import java.text.ParseException;
import java.util.Iterator;

import org.json.simple.*;
import org.json.simple.parser.*;

class SWoT {
    private static Logger logger = LoggerFactory.getLogger(SWoT.class);
    private OWLOntology ontology;
    private OWLOntology ontologyResult;
    private OWLOntologyManager owlOntologyManager;
    private OWLDataFactory owlDataFactory;
    private SWRLRuleEngine ruleEngine;
    private SQWRLQueryEngine queryEngine;
    private DefaultPrefixManager prefixManager;
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
        IRI newOntologyIRI = IRI.create(ontologyIRI);
        try{
            this.ontology = this.owlOntologyManager.createOntology(newOntologyIRI);
        }catch (OWLOntologyCreationException e){

        }

    }

    void loadOWLFromFile(String path, String defaultPrefix) throws OWLOntologyCreationException, FileNotFoundException {
        this.ontology = this.owlOntologyManager.loadOntologyFromOntologyDocument(new FileInputStream(path));
        this.prefixManager = new DefaultPrefixManager(null, null, defaultPrefix);
        OWLDocumentFormat format = this.ontology.getOWLOntologyManager().getOntologyFormat(this.ontology);
        if (format.isPrefixOWLOntologyFormat())
            prefixManager.copyPrefixesFrom(format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());

        this.ontologyResult = this.owlOntologyManager.loadOntologyFromOntologyDocument(new FileInputStream(path));
    };

    void importOntology(String ontologyIRI, String ontologyPath) throws FileNotFoundException, OWLOntologyCreationException {
        InputStream inputCoreOntology = new FileInputStream(ontologyPath);
        IRI coreOntoIRI = IRI.create(ontologyIRI);
        SimpleIRIMapper iriMapper = new SimpleIRIMapper(coreOntoIRI, IRI.create(new File(ontologyPath)));
        this.owlOntologyManager.getIRIMappers().add(iriMapper);
        OWLOntology owlCoreModel = this.owlOntologyManager.loadOntologyFromOntologyDocument(inputCoreOntology);
        if (firstOntologyImport) {
            this.prefixManager = new DefaultPrefixManager(null, null, ontologyIRI);
            firstOntologyImport = false;
        }
        OWLDocumentFormat format = owlCoreModel.getOWLOntologyManager().getOntologyFormat(owlCoreModel);
        if (format.isPrefixOWLOntologyFormat()){
            prefixManager.copyPrefixesFrom(format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());
        }
        OWLImportsDeclaration importDeclaration = owlDataFactory.getOWLImportsDeclaration(coreOntoIRI);
        this.owlOntologyManager.applyChange(new AddImport(this.ontology, importDeclaration));
    }

    void printOntologyAndImports(@Nonnull OWLOntology ontology) {
        printOntology(ontology);
        for (OWLOntology o : ontology.getImports()) {
            printOntology(o);
        }
    }

    private void printOntology(@Nonnull OWLOntology ontology) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        IRI documentIRI = owlOntologyManager.getOntologyDocumentIRI(ontology);
        System.out.println(ontologyIRI == null ? "anonymous" : ontologyIRI.toQuotedString());
        System.out.println(" from " + documentIRI.toQuotedString());
    }

    // SWRL & SQWRL
    void initSWRLEngine() {
        reasonerFactory = new StructuralReasonerFactory();
        owlReasoner = reasonerFactory.createReasoner(this.ontology);
        owlReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(this.ontology, this.prefixManager);
        queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(this.ontology, this.prefixManager);
    }

    void addNewRule(String ruleName, String rule) throws SWRLParseException {
        ruleEngine.createSWRLRule(ruleName, rule);
    }

    void inferRule() {
        ruleEngine.infer();
    }

//    String queryAvgObs = null;
//    String queryIndObs = null;
//    String queryNAFObs = null;
//    RdfStream tg = null;

    // C-SPARQL

    void initSPARQLEngine() {
        sparqlEngine = new CsparqlEngineImpl();
        sparqlEngine.initialize(true);
    }

    // start RDF STREAMER

    CsparqlQueryResultProxy addQuery(String query) throws ParseException {
        CsparqlQueryResultProxy resultProxy = null;
        resultProxy = this.sparqlEngine.registerQuery(query, false);
        return resultProxy;
    }

    void addStreamer(String URI, String className){

        RdfStream rdfStream = new RDFStreamer(URI, className);
        sparqlEngine.registerStream(rdfStream);
//
//        final Thread t = new Thread((Runnable) rdfStream);
//        t.start();
    }
    void addObserver(CsparqlQueryResultProxy resultProxy, String converterPath, JSONArray results){
        if (resultProxy != null) {
            resultProxy.addObserver(new RDFResultsFormatter(this.ruleEngine, this.ontology,this.ontologyResult, this.owlReasoner, this.prefixManager, this.owlOntologyManager, this.owlDataFactory, this.queryEngine, converterPath, results));
        }
    }
    public OWLOntology getOntology() {
        return ontology;
    }

    public OWLOntologyManager getOwlOntologyManager() {
        return owlOntologyManager;
    }

    public OWLDataFactory getOwlDataFactory() {
        return owlDataFactory;
    }

    public SWRLRuleEngine getRuleEngine() {
        return ruleEngine;
    }

    public SQWRLQueryEngine getQueryEngine() {
        return queryEngine;
    }

    public DefaultPrefixManager getPrefixManager() {
        return prefixManager;
    }

    public Boolean getFirstOntologyImport() {
        return firstOntologyImport;
    }

    public CsparqlEngine getSparqlEngine() {
        return sparqlEngine;
    }

    public OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public OWLReasoner getOwlReasoner() {
        return owlReasoner;
    }

    public static void main(String[] args) {
        String jsonLocation = args[0];
        jsonLocation = new File("").getAbsolutePath() + "\\"  + jsonLocation;
        JSONParser jsonParser =  new JSONParser();
        SWoT swot = new SWoT();
        try{
            Object obj = jsonParser.parse(new FileReader(jsonLocation));
            JSONObject jsonObject = (JSONObject)obj;
            System.out.println("Json Loaded");

            String SWRLOntologyLocation = (String)jsonObject.get("SWRLOntologyLocation");

            String defaultPrefix = (String)jsonObject.get("default_prefix");
            swot.loadOWLFromFile(SWRLOntologyLocation, defaultPrefix);
            swot.printOntologyAndImports(swot.getOntology());
            System.out.println("OWL Loaded");


            swot.initSWRLEngine();
            System.out.println("SWRL Engine Start");

            JSONArray rules = (JSONArray)jsonObject.get("rules");
            Iterator rulesIterator = rules.iterator();
            while(rulesIterator.hasNext()) {
                JSONObject rule = ((JSONObject)rulesIterator.next());
                swot.addNewRule((String)rule.get("ruleName"),(String)rule.get("rule"));
            }
            swot.inferRule();
            System.out.println("Rules Loaded");

            swot.initSPARQLEngine();

            JSONArray streamerAdapters = (JSONArray)jsonObject.get("StreamerAdapter");
            Iterator StreamerAdaptersIterator = streamerAdapters.iterator();
            while(StreamerAdaptersIterator.hasNext()) {
                JSONObject streamerAdapter = (JSONObject) StreamerAdaptersIterator.next();
                swot.addStreamer((String)streamerAdapter.get("URI"),(String)streamerAdapter.get("AdapterClassName"));
            }
            System.out.println("Streamer Adapter Loaded");

            JSONArray queries = (JSONArray)jsonObject.get("queries");
            JSONArray results = (JSONArray)jsonObject.get("results");
            Iterator queriesIterator = queries.iterator();
            while(queriesIterator.hasNext()) {
                CsparqlQueryResultProxy c = swot.addQuery((String)queriesIterator.next());
                String converterPath = (String)jsonObject.get("SWRLOntologyConverterLocation");
                swot.addObserver(c,converterPath, results);
            }
            System.out.println("Queries Loaded");
            while(true){}

        }catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (SWRLParseException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
}

