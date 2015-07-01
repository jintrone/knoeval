package edu.msu.mi.cmap.analysis

import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.cmap.simulation.Agent
import edu.msu.mi.cmap.simulation.AgentSimulation

/**
 * Created by josh on 6/22/15.
 */
class Toolkit {


    public static groupByAttractor(AgentSimulation simulation) {


        Map result = simulation.world.analyzeAttractorProfile().collectEntries { [it,[]] }

        simulation.population.vertexSet().each { Agent a->
           result.each {k,v->
               if (!k.basin.vertexSet().disjoint(a.interests)) {
                   v<<a
               }
           }
        }
        result

    }


    public static double score(List test, List truth, List awareness = null) {
        double result = 0
        if (awareness == null) {
            awareness = 0..<(test ?: truth)[0].size()
        }
        (1..<Math.min(truth.size(), test.size())).each { idx ->
            def truval = truth[idx] // should be an array of binary
            def testval = test[idx]

            //1-normalized hamming distance
            result += awareness.sum { idx2 ->
                (1 - Math.abs(truval[idx2] - testval[idx2]))
            } / awareness.size()

        }
        return result / (Math.max(test.size(), truth.size()) - 1)
    }

    public static double attractorSimilarity(BinaryThresholdCmap from, BinaryThresholdCmap to, boolean verbose = false) {
        double max = Math.min(from.maxVal,to.maxVal)
        (0..max).sum {
            if (verbose) println "Check behavior $it: "
            def f = from.getBehavior(it)
            def t = to.getBehavior(it)
            if (verbose) println "$f"
            if (verbose) println "$t"
            def s = score(f,t)
            if (verbose) println "Score $s"
            s
        }/max
    }




}
