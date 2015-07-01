package edu.msu.mi.cmap.simulation

import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.cmap.CmapSimulation
import edu.msu.mi.socnet.AttributeEdge
import org.jgrapht.Graph

/**
 * Created by josh on 6/26/15.
 */
interface SimulationFactory {

    public void setRandom(Random rand)

    public Graph<Agent,AttributeEdge> generatePopulationGraph(AgentSimulation simulation)
    public initializeAgents(AgentSimulation simulation)
    public List<Agent> chooseCommunicationPartner(Agent me, AgentSimulation simulation)
    public boolean communicate(Agent me, Agent other, AgentSimulation simulation)
    public boolean think(Agent me, AgentSimulation simulation)
    public adapt(Agent me, BinaryThresholdCmap target, AgentSimulation simulation)
    public expose(Agent me, AgentSimulation simulation)
    public updateConnections(Agent agent, List<Agent> agents, AgentSimulation agentSimulation)
}
