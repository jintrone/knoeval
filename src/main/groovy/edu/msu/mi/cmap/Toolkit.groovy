package edu.msu.mi.cmap

/**
 * Created by josh on 6/22/15.
 */
class Toolkit {


    public static groupByAttractor(AgentSimulation simulation) {


        Map result = simulation.simulation.analyzeAttractorProfile().collectEntries { [it,[]] }

        simulation.graph.vertexSet().each { Agent a->
           result.each {k,v->
               if (!k.basin.vertexSet().disjoint(a.interests)) {
                   v<<a
               }
           }
        }
        result

    }




}
