package edu.msu.mi.cmap.simulation

import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.cmap.analysis.Toolkit
import edu.msu.mi.socnet.AttributeEdge
import org.jgrapht.Graph


/**
 * Created by josh on 6/3/15.
 */
class AgentSimulation {

    Graph<Agent, AttributeEdge> population
    BinaryThresholdCmap world
    int nagents
    double density
    int numIndicatorNodes
    Random rand
    double fitnessDelta = 0
    double currentFitness = 0
    int nroundsSinceChange = 0
    int trials
    SimulationFactory factory


    public AgentSimulation(BinaryThresholdCmap world, SimulationFactory factory, nagents, double density, Integer numIndicatorNodes = null) {
        this.world = world
        this.nagents = nagents
        this.density = density
        this.numIndicatorNodes = (numIndicatorNodes == null) ? world.nodecount : numIndicatorNodes
        rand = new Random()
        this.factory = factory
        factory.setRandom(rand)
        initPopulationGraph()
        initAgents()
    }


    public void initPopulationGraph() {
        population = factory.generatePopulationGraph(this)
    }

    public void initAgents() {
        factory.initializeAgents(this)
    }

    public void reset() {
        currentFitness = 0
        fitnessDelta = 0
        trials = 0
    }


    public void runSimulation(int ntrials) {
        //initAgents()

        (1..ntrials).each { trial ->




        }
    }

    public void runSimulationOnce() {
        double nfitness = 0
        population.vertexSet().each { Agent a ->
            List<Agent> partners = chooseCommunicationPartner(a).findResults { Agent other ->
                if (communicate(a, other, getAgentTemperature(a))) {
                    other
                }
            }


            if (!partners) {
                think(a, getAgentTemperature(a))
            } else {
                updateConnections(a,partners)
            }


            factory.expose(a,this)
            a.update(world)
            nfitness += a.currentScore
        }

        nfitness /= nagents
        fitnessDelta = nfitness - currentFitness
        currentFitness = nfitness
        trials++


    }

    public double calculateGlobalFitness() {
        population.vertexSet().sum { Agent agent->
            Toolkit.attractorSimilarity(agent.model,world)
        } / nagents

    }

    public printStatus() {
        println "Trial $trials [LF:${currentFitness.round(2)}] [GF:${calculateGlobalFitness().round(2)}] [T:${getTemperature().round(2)}]: ${population.vertexSet().collect { it.currentScore.round(2) }.join(",")}"
    }


    public double getTemperature() {
        if (trials < 50) {
            return 0.8
        } else {

            if (fitness - currentFitness <= 0) {
                nroundsSinceChange++
            } else {
                nroundsSinceChange = 0
            }
            return Math.min(10, (1.0 - currentFitness) * nroundsSinceChange) / 10
        }


    }

    public updateConnections(Agent me, List<Agent> partners) {
        factory.updateConnections(me,partners,this)
    }

    public double getAgentTemperature(Agent m) {
        Math.max(1.0 - m.currentScore, 0)
    }


    public List<Agent> chooseCommunicationPartner(Agent me) {
        factory.chooseCommunicationPartner(me,this)
    }

    public boolean communicate(Agent me, Agent other, double temperature = 0.5) {
        factory.communicate(me,other,this)
    }

    public boolean think(Agent me, double temperature = 0.2) {
        factory.think(me,this)
    }

    public void inspectAgents(Collection<Agent> vertices = null) {
        vertices = vertices ?: population.vertexSet()
        vertices.each { Agent a ->
            println "${a.reportStandardEdgeWeights()}"
        }
    }

    public Collection examineAgentSimilarity(List<Agent> vertices = null) {
        vertices = vertices ?: population.vertexSet() as List
        (0..<nagents).sum { int from ->
            ((from + 1)..<nagents).collect { int to ->

                [from: vertices[from].id, to: vertices[to].id, sim: vertices[from].cosineSimilarity(vertices[to])]
            }

        } as Collection
    }


}
