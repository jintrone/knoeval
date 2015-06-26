package edu.msu.mi.cmap

import edu.msu.mi.socnet.AttributeEdge
import edu.msu.mi.socnet.JGraphTUtils
import org.jgrapht.Graphs
import org.jgrapht.VertexFactory
import org.jgrapht.graph.SimpleGraph

/**
 * Created by josh on 6/3/15.
 */
class AgentSimulation {

    SimpleGraph<Agent,AttributeEdge> graph
    BinaryThresholdCmap simulation
    int nagents
    double density
    int numIndicatorNodes
    Random rand
    double currentFitness = 0
    int nroundsSinceChange = 0




    public AgentSimulation(BinaryThresholdCmap simulation,nagents,double density,Integer numIndicatorNodes = null) {
        this.simulation = simulation
        this.nagents = nagents
        this.density = density
        this.numIndicatorNodes= (numIndicatorNodes==null)?simulation.nodecount:numIndicatorNodes
        rand = new Random()
        initAgents()
    }


    public void initAgents() {
        graph = JGraphTUtils.createRandomUnweightedGraph(nagents,density,{ return new Agent()} as VertexFactory<Agent>)
        List awarenessList = []
        List interestList = []
        def generateRandomList = {int size, int max, List holder ->
            def result = []
            (1..1).each {
                if (!holder) {
                    holder.addAll((0..<max)-result)
                    Collections.shuffle(holder)
                }
                result<<holder.pop()
            }
            result
        }
        List agentList = graph.vertexSet() as List
        Collections.shuffle(agentList)

        agentList.each { Agent a->

            a.with {
                random = rand
                awareness = generateRandomList(numIndicatorNodes,simulation.nodecount,awarenessList.sort())
                //awareness = 0..<numIndicatorNodes as List
                model = new BinaryThresholdCmap(simulation.nodecount,simulation.density)
                interests = generateRandomList(1,simulation.getMaxVal(),interestList)
                update(simulation)
            }
        }
    }


    public void runSimulation(int ntrials) {
        //initAgents()
        double nfitness = 0
        (1..ntrials).each { trial->
            double temperature = getTemperature(trial,nfitness)

            graph.vertexSet().each { Agent a->
                if (!communicate(a,getAgentTemperature(a))) {
                    think(a,getAgentTemperature(a))
                }
                a.update(simulation)
                nfitness+=a.currentScore
            }
            nfitness/=nagents
            println "Trial $trial [S:${nfitness.round(2)}] $trial [T:${temperature.round(2)}]: ${graph.vertexSet().collect { it.currentScore.round(2)}.join(",")}"

        }
    }



    public double getTemperature(int round, double fitness) {
        if (round < 50) {
            return 0.8
        } else {

            if (fitness - currentFitness <= 0) {
                nroundsSinceChange++
            } else {
                nroundsSinceChange = 0
            }
            currentFitness = fitness
            return Math.min(10, (1.0 - currentFitness) * nroundsSinceChange) / 10
        }


    }

    public double getAgentTemperature(Agent m) {
        Math.max(1.0 - m.currentScore,0)
    }

    public boolean communicate(Agent me,double temperature=0.5) {

        List neighbors = Graphs.neighborListOf(graph,me)
        Agent other = neighbors[rand.nextInt(neighbors.size())]


        double nscore = other.probe(simulation,me.interests[0])

        if (nscore > me.currentScore) {
            me.approach(other.model,temperature)
            return true
        } else {
            return false
        }
    }

    public boolean think(Agent me, double temperature=0.2) {


        BinaryThresholdCmap test = new BinaryThresholdCmap(simulation.nodecount,simulation.density)
        List behavior = test.getBehavior(me.interests[0])
        double nscore  = Agent.score(behavior,simulation.getBehavior(me.interests[0]),me.awareness)
        if (nscore >  me.currentScore) {
            me.approach(test,temperature)
            true
        } else {
            false
        }

    }

    public void inspectAgents(Collection<Agent> vertices = null) {
        vertices=vertices?:graph.vertexSet()
        vertices.each {Agent a ->
            println "${a.reportStandardEdgeWeights()}"
        }
    }

    public Collection examineAgentSimilarity(List<Agent> vertices = null) {
        vertices = vertices?:graph.vertexSet() as List
        (0..<nagents).sum { int from ->
            ((from+1)..<nagents).collect {int to ->

                 [from: vertices[from].id, to:vertices[to].id, sim: vertices[from].cosineSimilarity(vertices[to])]
            }

        } as Collection
    }



}
