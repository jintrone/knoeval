package edu.msu.mi.cmap.analysis

import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.socnet.JGraphTUtils

/**
 * Created by josh on 6/28/15.
 */


def createNetwork = {
    def result = new BinaryThresholdCmap(8,0.2)
    def graph = JGraphTUtils.createFixedDegreeRandomWeightedGraph(8,2)
    JGraphTUtils.randomizeWeights(graph,-1.0,1.0)
    result.setGraph(graph)
    result
}



BinaryThresholdCmap model1 = createNetwork()
BinaryThresholdCmap model2 = createNetwork()

double score = Toolkit.attractorSimilarity(model1,model2)
int iterations = 0

println "Initial score ${score}"
while (score < 1 && iterations++<100) {
    //JGraphTUtils.approach(model1.graph,model2.graph,model1.indexToVertex,model2.indexToVertex,0.5)
    JGraphTUtils.approach(model1.graph,model2.graph,0.25)
    //JGraphTUtils.adopt(model1.graph,model2.graph,1)
    model1.reset()
    model2.reset()
    score = Toolkit.attractorSimilarity(model1,model2)
    println "${iterations}. ${score}"
   // println "${model1.reportStandardEdgeWeights()}"
   // println "${model2.reportStandardEdgeWeights()}"
}

Toolkit.attractorSimilarity(model1,model2)
JGraphTUtils.approach(model1.graph,model2.graph,1.0)
model1.reset()
println "score ${Toolkit.attractorSimilarity(model1,model2)}"
println "${model1.reportStandardEdgeWeights()}"
println "${model2.reportStandardEdgeWeights()}"