@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')

import groovyx.net.http.HTTPBuilder

/*def matched_terms = []
new File('./matched_sterms.txt').eachLine {
  matched_terms << it.trim().toLowerCase()
}
matched_terms = matched_terms.unique(true)*/

def matched_terms =[]
new File('CONCEPT_SYNONYM.csv').splitEachLine('\t') {
  matched_terms << it[1].trim().toLowerCase()
}
matched_terms = matched_terms.unique(true)

def search_terms = []
new File('./unmatched_sterms.txt').eachLine {
  if(!matched_terms.contains(it.trim().toLowerCase())) {
    search_terms << it.trim().toLowerCase()
  }
}
search_terms = search_terms.unique(true)

def distance(str1, str2) {
  def str1_len = str1.length()
  def str2_len = str2.length()

  int[][] distance = new int[str1_len + 1][str2_len + 1]
  (str1_len + 1).times { distance[it][0] = it }
  (str2_len + 1).times { distance[0][it] = it }
  (1..str1_len).each { i ->
	 (1..str2_len).each { j ->
		distance[i][j] = [distance[i-1][j]+1, distance[i][j-1]+1, str1[i-1]==str2[j-1]?distance[i-1][j-1]:distance[i-1][j-1]+1].min()
	 }
  }

  distance[str1_len][str2_len]
}

search_terms.each { s ->
  matched_terms.each { m ->
    def d = distance(s, m)
    if(s.length() > 3 && d < 2) {
      println "${s} -> ${m} (${d})"
    }
  } 
}


/*
def http = new HTTPBuilder('http://aber-owl.net/')
def medline = new HTTPBuilder('https://eutils.ncbi.nlm.nih.gov/')
    
search_terms.each { sTerm ->
  getMedline(medline, sTerm, {
    println 'done'
  })
}

def getCandidates(http, term, cb) {
  println term

  http.get(path: '/api/querynames/', query: [ query: term ]) { resp, json ->
    println json
  }
}

def getMedline(medline, term, cb) {
  def searchString = term
  medline.get(path: '/entrez/eutils/esearch.fcgi', query: [ email:'l.slater.1@bham.ac.uk', tool:'synval', db: 'pmc', term: searchString ]) { resp ->
    def body = resp.entity.content.text.replace('<!DOCTYPE eSearchResult PUBLIC "-//NLM//DTD esearch 20060628//EN" "https://eutils.ncbi.nlm.nih.gov/eutils/dtd/20060628/esearch.dtd">','')
    def res = new XmlSlurper().parseText(body)
    //cb(res.childNodes()[0].text())

    println res.childNodes()[1].text()

    cb()
  }
}
*/
