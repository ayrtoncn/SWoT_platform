import eu.larkc.csparql.cep.api.RdfQuadruple;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class NewAdapter extends StreamObservable{
    private static String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
    private static String dul = "http://www.loa-cnr.it/ontologies/DUL.owl#";
    private static String inwsCore = "http://inwatersense.uni-pr.edu/ontologies/inws-core.owl#";
    private String inwsPoll = "http://inwatersense.uni-pr.edu/ontologies/inws-pollutants.owl#";
    private String geo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    private static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private String streamURL;

    public NewAdapter() {

    }

    @Override
    public void run() {
        while(true) {
            JSONObject json = new JSONObject();
            streamURL = "http://127.0.0.1:5000/";
            try {
                URL url = new URL("http://127.0.0.1:5000/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int status = con.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                JSONParser parser = new JSONParser();
//			System.out.println(content);
                json = (JSONObject) parser.parse(content.toString());

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long tempTS;
            tempTS = System.currentTimeMillis();
            RdfQuadruple q;
            try {
                Integer n = Integer.parseInt((String) json.get("n"));
                Float val = Float.parseFloat((String) json.get("val"));
                String WQ = (String) json.get("WQ");
                String location_rnd = (String) json.get("location_rnd");
                //System.out.println("# "+ this.c +": " + System.currentTimeMillis());
                q = new RdfQuadruple(streamURL + "#obs_" + n, this.rdf + "type", this.ssn + "Observation", tempTS);
                this.put(q);
                //System.out.println(q.toString());
                q = new RdfQuadruple(streamURL + "#obs_" + n, this.ssn + "qualityOfObservation", this.inwsCore + WQ, tempTS);
                this.put(q);
                //System.out.println(q.toString());
                q = new RdfQuadruple(streamURL + "#obs_" + n, this.ssn + "observationResult", streamURL + "#so_" + n, tempTS);
                this.put(q);
                //System.out.println(q.toString());
                q = new RdfQuadruple(streamURL + "#so_" + n, this.ssn + "hasValue", streamURL + "#ov_" + n, tempTS);
                this.put(q);
                //System.out.println(q.toString());
                q = new RdfQuadruple(streamURL + "#ov_" + n, this.dul + "hasDataValue", val + "^^http://www.w3.org/2001/XMLSchema#double", tempTS);

                this.put(q);
                q = new RdfQuadruple(streamURL + "#obs_" + n, this.inwsCore + "observationResultLocation", this.inwsCore + "ms" + location_rnd, tempTS);
                this.put(q);
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
    }
}
