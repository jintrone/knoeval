package edu.msu.mi.cmap.simulation

import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.cmap.analysis.Toolkit
import edu.msu.mi.socnet.AttributeEdge
import edu.msu.mi.socnet.JGraphTUtils
import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.VertexFactory

/**
 * Created by josh on 6/26/15.
 *
 * Agents have sufficient capacity to model the world, and a single interest
 *
 *
 */
class NaiveSimulationFactory implements SimulationFactory {

    Random random

    @Override
    Graph<Agent, AttributeEdge> generatePopulationGraph(AgentSimulation simulation) {
        JGraphTUtils.createRandomUnweightedGraph(simulation.nagents, simulation.density, {
            return new Agent()
        } as VertexFactory<Agent>)
    }

    @Override
    def initializeAgents(AgentSimulation simulation) {
        List awarenessList = []
        List interestList = []
        def generateRandomList = { int size, int max, List holder ->
            def result = []
            (1..1).each {
                if (!holder) {
                    holder.addAll((0..<max) - result)
                    Collections.shuffle(holder)
                }
                result << holder.pop()
            }
            result
        }
        List agentList = simulation.population.vertexSet() as List
        Collections.shuffle(agentList)

        agentList.each { Agent a ->


            a.with {
                random = owner.getRandom()
                awareness = generateRandomList(simulation.numIndicatorNodes, simulation.world.nodecount, awarenessList.sort())
                //awareness = 0..<numIndicatorNodes as List
                model = new BinaryThresholdCmap(simulation.world.nodecount, simulation.density)
                interests = generateRandomList(1, simulation.world.getMaxVal(), interestList)
                update(simulation.world)
            }
        }
    }

    @Override
    List<Agent> chooseCommunicationPartner(Agent me, AgentSimulation simulation) {
        List neighbors = Graphs.neighborListOf(simulation.population, me)
        [neighbors[random.nextInt(neighbors.size())]]
    }

    @Override
    boolean communicate(Agent me, Agent other, AgentSimulation simulation) {
        double nscore = other.probe(simulation.world, me.interests[0])
        if (nscore > me.currentScore) {
            adapt(me,other.model,simulation)
            return true
        } else {
            return false
        }
    }

    @Override
    boolean think(Agent me, AgentSimulation simulation) {
        BinaryThresholdCmap test = new BinaryThresholdCmap(simulation.world.nodecount, simulation.density)
        List behavior = test.getBehavior(me.interests[0])
        double nscore = Toolkit.score(behavior, simulation.world.getBehavior(me.interests[0]), me.awareness)
        if (nscore > me.currentScore) {
            adapt(me,test,simulation)
            true
        } else {
            false
        }
    }

    public updateConnections(Agent agent, List<Agent> agents, AgentSimulation agentSimulation) {
         //no-op
    }


    @Override
    def adapt(Agent me, BinaryThresholdCmap target, AgentSimulation simulation) {
        me.approach(target, simulation.getAgentTemperature(me))
    }

    @Override
    def expose(Agent me, AgentSimulation simulation) {
        me.update(simulation.world)
    }
}
