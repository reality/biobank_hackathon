@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab('com.xlson.groovycsv:groovycsv:1.1')

import static com.xlson.groovycsv.CsvParser.parseCsv
import groovyx.net.http.HTTPBuilder
import groovy.json.*

def bbdata = parseCsv(new FileReader('BiobankCollections.csv'), separator: ',')
def biobanks = [:]

def http = new HTTPBuilder('http://data.bioontology.org')

bbdata.each {
  if(!biobanks.containsKey(it['Biobank Name'])) {
    biobanks[it['Biobank Name']] = [
      'desc': it['Description'],
      'snomedTerms': [],
      'otherTerms': [],
      'collections': []
    ] 
  }

  biobanks[it['Biobank Name']].collections << [
    'diagnosis': it['Diagnosis'],
    'snomedTerms': [],
    'otherTerms': [],
    'desc': it['Collection Description']
  ]
}

biobanks.each { name, bb ->
  annotate(http, bb.desc, { ids -> 
    bb.snomedTerms = ids.findAll { it.indexOf('SNOMED') != -1 }
    bb.otherTerms = ids.findAll { it.indexOf('SNOMED') == -1 }
  })

  bb.collections.each { cl ->
   annotate(http, cl.desc, { ids -> 
      cl.snomedTerms = ids.findAll { it.indexOf('SNOMED') != -1 }
      cl.otherTerms = ids.findAll { it.indexOf('SNOMED') == -1 }
    })
  }

  println bb
}

def annotate(http, text, cb) {
  http.get(path: '/annotator', query: [ apikey: 'aa3c6462-08f3-4991-862d-fa145ae6f746', text: text.replaceAll(' ', '+') ]) { resp, json ->
    cb(json.collect { it.annotatedClass['@id'] })
  }
}

new File('biobanks.json').text = new JsonBuilder(biobanks).toPrettyString()
