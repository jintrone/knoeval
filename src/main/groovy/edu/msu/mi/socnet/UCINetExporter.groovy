package edu.msu.mi.socnet

import org.jgrapht.WeightedGraph

/**
 * Created by josh on 4/28/15.
 */
class UCINetExporter {
    static void write(WeightedGraph<String,AttributeEdge> g, File f) {
        System.setProperty("line.separator","\r\n");
        f.withPrintWriter { PrintWriter out ->
            List<String> nodes = g.vertexSet() as List
            out.println("DL n=${nodes.size()}")
            out.println("format = edgelist1")
            out.println("labels embedded:")
            out.println("data:")

            g.edgeSet().each { edge ->
                float weight = g.getEdgeWeight(edge)
                if (weight) {
                    out.println("${g.getEdgeSource(edge)} ${g.getEdgeTarget(edge)} ${g.getEdgeWeight(edge)}")
                }
            }
        }
    }


}
