package edu.msu.mi.cmap.analysis

import edu.msu.mi.cmap.simulation.AgentSimulation
import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.cmap.simulation.NaiveSimulationFactory
import edu.msu.mi.cmap.simulation.SNUpdatingSimulationFactory

/**
 * Created by josh on 6/3/15.
 */


def printAttractor(def v) {
    "${v.type} basin:${v.basin.vertexSet().size()-v.attractor.vertexSet().size()}  attractor:${v.attractor.vertexSet().size()}"
}

BinaryThresholdCmap model = new BinaryThresholdCmap(8,0.282)
AgentSimulation sim = new AgentSimulation(model,new SNUpdatingSimulationFactory(), 32,1.0)
//outputAgentSimilarity(sim)
sim.runSimulation(1000)
//println(model.analyzeAttractorProfile().collect {v->"${v.type} basin:${v.basin.vertexSet().size()-v.attractor.vertexSet().size()}  attractor:${v.attractor.vertexSet().size()}"}.join("\n"))
//outputAgentSimilarity(sim)
//sim.inspectAgents()

Map m = Toolkit.groupByAttractor(sim)
m.each {k,v->
    println "\n${printAttractor(k)}\n"
    sim.inspectAgents(v)

}
println "\n${model.reportStandardEdgeWeights()}"


