package edu.msu.mi.cmap.simulation

import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.socnet.AttributeEdge
import edu.msu.mi.socnet.JGraphTUtils
import org.jgrapht.EdgeFactory
import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.VertexFactory
import org.jgrapht.generate.CompleteGraphGenerator
import org.jgrapht.graph.SimpleWeightedGraph

/**
 * Created by josh on 6/26/15.
 */
class SNUpdatingSimulationFactory extends NaiveSimulationFactory {

    Graph<Agent, AttributeEdge> generatePopulationGraph(AgentSimulation simulation) {
        CompleteGraphGenerator<Agent,AttributeEdge> generator = new CompleteGraphGenerator<>(simulation.nagents)
        Graph<Agent,AttributeEdge> result = new SimpleWeightedGraph<Agent, AttributeEdge>(AttributeEdge.class)
        generator.generateGraph(result,{return new Agent()} as VertexFactory<Agent>,null)
        result.edgeSet().each {
            result.setEdgeWeight(it,0.5)
        }
        result
    }

    public updateConnections(Agent agent, List<Agent> agents, AgentSimulation agentSimulation) {
        agentSimulation.population.edgesOf(agent).each { edge ->
            Agent other = Graphs.getOppositeVertex(agentSimulation.population,edge,agent)
            double nweight = agentSimulation.population.getEdgeWeight(edge)
            if (other in agents) {
                if (nweight < (1.0/1.1)) nweight = Math.min(nweight*1.1,1.0)

            } else {
                if (nweight > 0.05) nweight = Math.max(nweight*0.9,0.05)
            }
            agentSimulation.population.setEdgeWeight(edge,nweight)
        }
    }

    List<Agent> chooseCommunicationPartner(Agent me, AgentSimulation simulation) {
        List neighbors = Graphs.neighborListOf(simulation.population, me)
        List<Double> vals = neighbors.inject([]) { r , Agent other->
            Object e = simulation.population.getEdge(me,other)
            r<< (r?r.last():0)+simulation.population.getEdgeWeight(e)

        }
        double roll = random.nextDouble()*vals.sum()
        [neighbors[vals.findIndexOf {roll < it}-1]]

    }


}
