@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab('com.xlson.groovycsv:groovycsv:1.1')

import static com.xlson.groovycsv.CsvParser.parseCsv
import groovyx.net.http.HTTPBuilder
import groovy.json.*

def biobanks = new JsonSlurper(type: JsonParserType.CHARACTER_SOURCE).parse(new File('biobanks.json'))
def http = new HTTPBuilder('http://data.bioontology.org/')

def c = 0
biobanks.each { name, bb ->
  c++

  bb.snomedLabels = [];
  bb.snomedTerms.each { iri ->
    getLabelsAndSyns(http, iri, { nSyns ->
      bb.snomedLabels += nSyns
    }) 
  }
  bb.snomedLabels.flatten()

  bb.collections.each { col ->
    col.snomedLabels = [];
    col.snomedTerms.each { iri ->
      getLabelsAndSyns(http, iri, { nSyns ->
        col.snomedLabels += nSyns
      }) 
    }
    col.snomedLabels.flatten()
    println col.snomedLabels
  }

  println "${c}/${biobanks.size()}"
}

def getLabelsAndSyns(http, iri, cb) {
//def iri2 = java.net.URLEncoder.encode(iri)
  //http://data.bioontology.org/ontologies/SNOMEDCT/classes/
  iri = iri.split('/').last()
  try {
    http.get(path: '/ontologies/SNOMEDCT/classes/' + iri + '/', query: [apikey:'aa3c6462-08f3-4991-862d-fa145ae6f746']) { resp, json ->
      cb([json.prefLabel] + json.synonym)
    }
  } catch(e) {
    cb([])
  }
}

new File('biobanks.json').text = new JsonBuilder(biobanks).toPrettyString()
