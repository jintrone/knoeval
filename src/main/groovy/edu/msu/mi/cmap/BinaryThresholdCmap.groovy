package edu.msu.mi.cmap

import edu.msu.mi.socnet.AttributeEdge
import edu.msu.mi.socnet.JGraphTUtils
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.alg.cycle.JohnsonSimpleCycles
import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.Subgraph

/**
 * Created by josh on 6/3/15.
 */
public class BinaryThresholdCmap extends CmapSimulation {


    Map profile;

    public BinaryThresholdCmap(int nodecount, double density) {
        super(nodecount, density, CmapSimulation.threshold)
    }

    public initNetwork(double min = -1.0, double max = 1.0) {
        super.initNetwork(min, max)
        reset()

    }

    public void reset() {
        profile = [:]
    }

    public int simulate(int start) {
        int result = this.simulate(toBinary(start, nodecount))
        profile[start] = valueHistory
        result
    }

    public int getMaxVal() {
        return 2**nodecount - 1
    }

    public void createProfile() {
        def data = (0..getMaxVal()) as Set
        data.each { getBehavior(it) }
    }

    public SimpleDirectedGraph<Integer,AttributeEdge> createAttractorProfile() {
        createProfile()
        SimpleDirectedGraph<Integer, AttributeEdge> graph = new SimpleDirectedGraph<>(AttributeEdge.class)
        profile.values().each { List val ->
            int last = -1
            val.each { List vals ->
                int n = fromBinary(vals)
                if (last > -1 && n != last) {
                    JGraphTUtils.safeAdd(graph, last, n)
                }
                last = n
            }
        }
        graph
    }

    public List analyzeAttractorProfile() {
        List result = []
        List accounted = []

        def attractor = { gbasin,gterminus -> [type:(gterminus.vertexSet().size()>1?"CYCLE":"FIXED"),basin:gbasin,attractor:gterminus]}

        SimpleDirectedGraph<Integer,AttributeEdge> graph = createAttractorProfile()
        Set fixedpoints = graph.vertexSet().findAll { node ->
            graph.outDegreeOf(node)==0
        }
        ConnectivityInspector<Integer,AttributeEdge> ci = new ConnectivityInspector<>(graph)
        result+=fixedpoints.collect {
            Set basin = ci.connectedSetOf(it)
            accounted+=basin
            attractor(new Subgraph(graph,basin),new Subgraph(graph,[it] as Set))
        }

        List<List<Integer>> cycles = new JohnsonSimpleCycles<Integer,AttributeEdge>(graph).findSimpleCycles()
        result+=cycles.collect {
            Set basin = ci.connectedSetOf(it[0])
            accounted+=basin
            attractor(new Subgraph(graph,basin),new Subgraph(graph,it as Set))

        }

        result+=((0..getMaxVal() as List) - accounted).collect {
            attractor(new Subgraph(graph,[it] as Set),new Subgraph(graph,[it] as Set))
        }

        result


    }


    List getStandardWeightVector() {
        (0..<nodecount).sum {from->
            (0..<nodecount).collect {to->
                def edge = graph.getEdge(indexToVertex[from],indexToVertex[to])
                edge?graph.getEdgeWeight(edge):0.0

            }
        }
    }

    def String reportStandardEdgeWeights() {
        getStandardWeightVector().collect {
            "${it==0.0?"":it.round(2)}".padLeft(6)
        }.join(" ")

    }

    /**
     * Returns a list of lists of values corresponding to the indices of each node
     * in the underlying model
     * @param probe
     * @return
     */
    public List getBehavior(int probe) {
        profile[probe] ?: { simulate(probe); profile[probe] }()
    }

    def static toBinary = { val, digits ->


        def result = []
        while (--digits >= 0) {
            if (val >= 2**digits) {
                result << 1.0
                val -= 2**digits
            } else {
                result << 0.0
            }


        }
        return result
    }
    def static fromBinary = { List val ->
        def result = 0
        val.eachWithIndex { item, idx ->
            result += (item * 2**(val.size() - 1 - idx))
        }
        return result
    }


}
