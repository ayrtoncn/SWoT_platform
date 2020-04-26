import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import javafx.util.Pair;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.kafka.clients.producer.*;
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
import org.apache.http.client.methods.CloseableHttpResponse;
import javax.annotation.Nonnull;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.swrlapi.sqwrl.SQWRLResult;

public class RDFResultsFormatter extends ResultFormatter {
	SWRLRuleEngine ruleEngine;
	OWLOntology ontology;
	OWLOntology ontologyResult;
	OWLReasoner reasoner;
	DefaultPrefixManager prefixManager;
	OWLOntologyManager owlOntologyManager;
	OWLDataFactory owlDataFactory;
	SQWRLQueryEngine queryEngine;
	String converterPath;
	JSONArray results;
	private OWLOntology converterOntology;
//	private final CloseableHttpClient httpClient = HttpClients.createDefault();

	RDFResultsFormatter(SWRLRuleEngine ruleEngine, OWLOntology ontology, OWLOntology ontologyResult, OWLReasoner reasoner,
						DefaultPrefixManager prefixManager, OWLOntologyManager owlOntologyManager, OWLDataFactory owlDataFactory, SQWRLQueryEngine queryEngine, String converterPath, JSONArray results) {
		this.ruleEngine = ruleEngine;
		this.ontology = ontology;
		this.ontologyResult = ontologyResult;
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

				saveOWL(this.ontology.getOWLOntologyManager().getOntologyFormat(this.ontologyResult), this.owlOntologyManager, ontologyResult, "result3.owl");

				OWLClass clstmpModerateBOD = owlDataFactory.getOWLClass("inwsr:ModerateBODMeasurement", prefixManager);
				OWLClass clstmpGoodBOD = owlDataFactory.getOWLClass("inwsr:GoodBODMeasurement", prefixManager);
				OWLClass clstmpHighBOD = owlDataFactory.getOWLClass("inwsr:HighBODMeasurement", prefixManager);
				printClassIndividuals(clstmpModerateBOD,"MEDIUM");
				printClassIndividuals(clstmpHighBOD, "HIGH");
				printClassIndividuals(clstmpGoodBOD, "GOOD");
//				removeClassIndividuals(clstmpModerateBOD);
//				removeClassIndividuals(clstmpHighBOD);
//				removeClassIndividuals(clstmpGoodBOD);

				for(int i = 0; i < this.results.size(); i++){
					JSONObject result = (JSONObject)this.results.get(i);
					JSONArray states = (JSONArray)result.get("states");

					OWLClass owlClass = null;
					String state = "";
					NodeSet<OWLNamedIndividual> individualsNodeSet = null;
					for(int j= 0; j< states.size(); j++) {
						Boolean correct = true;
						owlClass= owlDataFactory.getOWLClass((String)states.get(j), prefixManager);
						individualsNodeSet = reasoner.getInstances(owlClass, false);
						if(individualsNodeSet.isEmpty()){
							correct = false;
						}else{
							state += states.get(j) ;
						}
						if(result.get("action").equals("WEB_SERVICE") && correct){
							if (result.get("METHOD").equals("GET")){
								String url = (String) result.get("URL");
								ArrayList<Pair<String,String>> headers = new ArrayList();
								JSONObject headerJson = (JSONObject)result.get("HEADERS");
								for(Iterator iterator = headerJson.keySet().iterator(); iterator.hasNext();) {
									String key = (String) iterator.next();
									Pair<String,String> p = new Pair<>(key, (String)headerJson.get(key));
								}
//							sendGet(url, headers);
							}else if (result.get("METHOD").equals("POST")){
								String url = (String) result.get("URL");
								String body = (String) result.get("BODY");
//							sendPost(url, body);
							}

						} else if(result.get("action").equals("STREAM") && correct){
							String kafkaBrokers = (String) result.get("KAFKA_BROKERS");
							String clientID = (String) result.get("CLIENT_ID");
							Integer msgCount = 10;
							String topicName = (String) result.get("TOPIC_NAME");
							Producer<Long, String> producer = this.createProducer(kafkaBrokers, clientID);
							Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
							for (OWLNamedIndividual ind : individuals) {
								String indName = ind.getIRI().toString().substring(ind.getIRI().toString().indexOf("#") + 1);
								String dataStream = "{\"Class\": \"" + state + "\", \"Individual\": \"" + indName + "\"}";
								ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topicName,dataStream);
								try {
									RecordMetadata metadata = producer.send(record).get();
								}
								catch (ExecutionException e) {
									System.out.println("Error in sending record");
									System.out.println(e);
								}
								catch (InterruptedException e) {
									System.out.println("Error in sending record");
									System.out.println(e);
								}
							}


						}
						removeClassIndividuals(owlClass);
					}



				}


				OWLClass tmpObservation = owlDataFactory.getOWLClass("ssn:Observation", this.prefixManager);
//				for (OWLAxiom ax : this.converterOntology.getAxioms()) {
//					owlOntologyManager.removeAxiom(ontology, ax);
//				}
				removeClassIndividuals(tmpObservation);
				saveOWL(ontologyResult.getOWLOntologyManager().getOntologyFormat(ontologyResult), owlOntologyManager, ontologyResult, "result.owl");
				saveOWL(ontology.getOWLOntologyManager().getOntologyFormat(ontology), owlOntologyManager, ontology, "result2.owl");

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
		File fileformated = new File("E:\\Code\\Semester 7\\Tugas Akhir\\InWaterSense\\Ontologies\\" + name);
		OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
		if (format.isPrefixOWLOntologyFormat()) {
			owlxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
		}
		try {
			manager.saveOntology(newOnto, owlxmlFormat, IRI.create(fileformated.toURI()));
		} catch (Exception e) {

		}
	}

//	private void sendGet(String url, ArrayList<Pair<String,String>> headers_array) {
//
//		HttpGet request = new HttpGet(url);
//
//		// add request headers
//		for (Pair <String,String> temp : headers_array)
//		{
//			request.addHeader(temp.getKey(),temp.getValue());
//		}
//
//		try (CloseableHttpResponse response = httpClient.execute(request)) {
//
//			// Get HttpResponse Status
//			HttpEntity entity = response.getEntity();
//			Header headers = entity.getContentType();
//
//			if (entity != null) {
//				// return it as a String
//				String result = EntityUtils.toString(entity);
//				System.out.println(result);
//			}
//
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	private void sendPost(String url_path,String body) {
//		try {
//			//Change the URL with any other publicly accessible POST resource, which accepts JSON request body
//			URL url = new URL(url_path);
//
//			HttpURLConnection con = (HttpURLConnection) url.openConnection();
//			con.setRequestMethod("POST");
//
//			con.setRequestProperty("Content-Type", "application/json; utf-8");
//			con.setRequestProperty("Accept", "application/json");
//
//			con.setDoOutput(true);
//
//			//JSON String need to be constructed for the specific resource.
//			//We may construct complex JSON using any third-party JSON libraries such as jackson or org.json
//
//			try (OutputStream os = con.getOutputStream()) {
//				byte[] input = body.getBytes("utf-8");
//				os.write(input, 0, input.length);
//			}
//
//			int code = con.getResponseCode();
//			System.out.println(code);
//
//			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
//				StringBuilder response = new StringBuilder();
//				String responseLine = null;
//				while ((responseLine = br.readLine()) != null) {
//					response.append(responseLine.trim());
//				}
//
//
//			}
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public static Producer<Long, String> createProducer(String KAFKA_BROKERS, String CLIENT_ID) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
//		props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		//props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
		return new KafkaProducer<>(props);
	}


}