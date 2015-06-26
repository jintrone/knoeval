package edu.msu.mi.socnet

import org.jgrapht.WeightedGraph
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import org.jgrapht.graph.SimpleWeightedGraph

/**
 * Created by josh on 4/12/15.
 */
class NColExporter {


    static void write(WeightedGraph<String,AttributeEdge> g, File f) {
        f.withPrintWriter { PrintWriter out ->
            g.edgeSet().each { edge ->
                float weight = g.getEdgeWeight(edge)
                if (weight) {
                    out.println("${g.getEdgeSource(edge)}\t${g.getEdgeTarget(edge)}\t${g.getEdgeWeight(edge)}")
                }
            }
        }
    }

    static WeightedGraph<String,AttributeEdge> read(File f,boolean directed=false) {
        WeightedGraph<String,AttributeEdge> result = directed?new SimpleDirectedWeightedGraph<String,AttributeEdge>(AttributeEdge.class):new SimpleWeightedGraph<String,AttributeEdge>(AttributeEdge.class)
        f.splitEachLine(/\s/) { tokens ->

            JGraphTUtils.safeAdd(result,tokens[0],tokens[1],Double.parseDouble(tokens[2]))
        }
        result
    }
}
