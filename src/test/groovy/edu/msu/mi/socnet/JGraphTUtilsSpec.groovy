package edu.msu.mi.socnet

import junit.framework.Assert
import org.jgrapht.EdgeFactory
import org.jgrapht.VertexFactory
import org.jgrapht.WeightedGraph
import org.jgrapht.generate.RingGraphGenerator
import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.graph.SimpleDirectedWeightedGraph

import spock.lang.Specification


/**
 * Created by josh on 6/16/15.
 */
class JGraphTUtilsSpec extends Specification {

    def "test graph similarity cosine distance"() {

        setup:

        WeightedGraph<Integer,AttributeEdge> graph1 = new SimpleDirectedWeightedGraph<Integer,AttributeEdge>(AttributeEdge.class)
        WeightedGraph<Integer,AttributeEdge> graph2 = new SimpleDirectedWeightedGraph<Integer,AttributeEdge>(AttributeEdge.class)



        int id = 0;
        RingGraphGenerator generator = new RingGraphGenerator<Integer,AttributeEdge>(5)
        generator.generateGraph(graph1,{id++} as VertexFactory,null)
        generator.generateGraph(graph2,{id++} as VertexFactory,null)


        expect:
        JGraphTUtils.cosineSimilarity(graph1,graph2)==1.0

        when:
        graph1.setEdgeWeight(graph1.getEdge(0,1),0.9)


        then:
        double sim = JGraphTUtils.cosineSimilarity(graph1,graph2)
        sim<1.0 && sim> 0.9


        when:
        graph2.setEdgeWeight(graph2.getEdge(5,6),0.9)


        then:
        Assert.assertEquals(JGraphTUtils.cosineSimilarity(graph1,graph2),1.0,0.01)


        expect:
        Assert.assertEquals(JGraphTUtils.cosineSimilarity(graph1,graph2,null,[(0):6,(1):5,(2):7,(3):8,(4):9]),0.6,0.1)
        Assert.assertEquals(JGraphTUtils.cosineSimilarity(graph1,graph2,[(0):1,(1):0,(2):2,(3):3,(4):4],[(0):6,(1):5,(2):7,(3):8,(4):9]),1.0,0.01)





    }


}