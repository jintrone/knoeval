package edu.msu.mi.cmap

import edu.msu.mi.socnet.AttributeEdge
import edu.msu.mi.socnet.AttributeVertex
import edu.msu.mi.socnet.JGraphTUtils
import org.jgrapht.graph.SimpleDirectedWeightedGraph

/**
 * Created by josh on 6/3/15.
 */
class CmapSimulation {


    SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge> graph
    int nodecount
    double density
    int max_reps = 10000
    Closure aggregationFunction
    List valueHistory = []
    Map vertexToIndex = [:]
    Map indexToVertex = [:]
    double maxEdgeWeight = 0.0
    double minEdgeWeight = 0.0




    public CmapSimulation(int nodecount, double density, Closure aggregation = { List vals -> vals.sum() }) {
        this.nodecount = nodecount
        this.density = density
        this.aggregationFunction = aggregation
        initNetwork()
    }

    public initNetwork(double min = -1.0, double max = 1.0) {
        this.maxEdgeWeight = max
        this.minEdgeWeight = min
        SimpleDirectedWeightedGraph<AttributeVertex,AttributeEdge> graph =  JGraphTUtils.createRandomWeightedGraph(nodecount, density)
        JGraphTUtils.randomizeWeights(graph, min, max)
        setInitialNetwork(graph)
    }

    public setInitialNetwork(SimpleDirectedWeightedGraph<AttributeVertex,AttributeEdge> graph) {
        vertexToIndex.clear()
        indexToVertex.clear()
        this.graph = graph
        //make sure we have a stable sort
        graph.vertexSet().eachWithIndex { AttributeVertex entry, int i ->
            vertexToIndex[(entry)]=i
            indexToVertex[(i)]=entry

        }

    }

    public static Closure sumfx = { List vals, CmapSimulation model=null ->
        vals.sum()
    }

    public static Closure avgfx = { List vals, CmapSimulation model=null ->
        vals.sum() / vals.size()
    }

    public static Closure threshold = { List vals, CmapSimulation model=null ->
        vals.sum() > ((model?.minEdgeWeight+model?.maxEdgeWeight)/2) ? 1.0 : 0.0
    }

    public void exportValues(File output) {
        output.withWriterAppend { out ->
            out.println "${(["id,nodes,density"]+vertexToIndex*.id).join(",")}"
            valueHistory.eachWithIndex { item, idx ->
                out.println(([nodecount, density, idx] + item).join(","))
            }
        }

    }

    public int simulate(double min, double max) {

        Random r = new Random()
        List starting = graph.vertexSet().collect { item ->

            min + (r.nextDouble() * (max - min))
        }
        return simulate(starting)
    }

    public int simulate(List starting = null) {
        //Map indices = [:]

        List values = []
        Set hist = [] as Set
        hist << starting


        int x = 0
        while (x++ < max_reps) {
            values = []
            List last = hist.last()
            vertexToIndex.keySet().each { AttributeVertex item->
                Set edges = graph.incomingEdgesOf(item)
                if (edges) {
                    values << aggregationFunction(edges.collect { AttributeEdge edge ->
                        last[vertexToIndex[graph.getEdgeSource(edge)]] * graph.getEdgeWeight(edge)
                    },this)
                } else {
                    values << last[vertexToIndex[item]]
                }
            }
            if (values in hist) {
                valueHistory = hist as List
                valueHistory<< values
                break
            } else {
                hist << values
            }
        }
        return x

    }





}
