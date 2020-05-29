Config File
===============
JSON
----

```json
{
    "StreamerAdapter": [
        {
		  "URI"               : "IRI that link to CSPARQL query",
		  "AdapterClassName"  : "Streamer Adapter class name for input data"
        }
    ],
    "SWRLOntologyLocation": "Ontology Absolute Path",
    "rules":[
        {
		  "ruleName"  : "rule name",
		  "rule"      : "rule content"
        },
    ],
    "queries" :[
        "CSPARQL Query",
    ],
    "SWRLOntologyConverterLocation" : "Rdf to OWL converter Absolute Path",
    "results": [
	{
		"states": [
			"OWLClass Name"
		],
		"action": "WEB_SERVICE/STREAM",
		"URL": "URLPATH",
		"METHOD": "GET/POST"
	}
    ]
}

```

Converter File
===============
OWL
----
```
| Variable | Usage                                       |
|----------|---------------------------------------------|
| &arg[n]  | take the nth value from the C-SPARQL output |
| &n       | random number                               |
```
