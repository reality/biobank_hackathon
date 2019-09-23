import groovy.json.*

def allNewLabels = []

def biobanks = new JsonSlurper(type: JsonParserType.CHARACTER_SOURCE).parse(new File('biobanks.json'))
biobanks.each { name, bb ->
  allNewLabels += bb.snomedLabels.collect { it.toLowerCase() } 
  bb.collections.each { cl -> allNewLabels += cl.snomedLabels.collect{ it.toLowerCase() }}
}
allNewLabels = allNewLabels.flatten().unique(false)

println allNewLabels

println allNewLabels.size()

def c = 0
def f = 0
new File('unmatched_sterms.txt').eachLine { 

  c++
  if(allNewLabels.contains(it.trim().toLowerCase())) {
    f++
  }
}

println "${f}/${c}"
