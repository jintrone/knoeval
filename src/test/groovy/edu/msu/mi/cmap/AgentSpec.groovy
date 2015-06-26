package edu.msu.mi.cmap

import junit.framework.Assert
import spock.lang.Specification


/**
 * Created by josh on 6/12/15.
 */
class AgentSpec extends Specification {

    def "compare behaviors"() {
        setup:
        BinaryThresholdCmap model = new BinaryThresholdCmap(5,0.7)
        Agent agent = new Agent(model,model.vertexToIndex.values() as List)

        expect:
        agent.model.getBehavior(a) == model.getBehavior(a)

        where:
        a << (1..31 as List)
    }

    def "test agent score"() {
        setup:
        BinaryThresholdCmap model = new BinaryThresholdCmap(5,0.7)
        Agent agent = new Agent(model,model.vertexToIndex.values() as List)
        agent.interests = [a]
        agent.update(model)

        expect:
        agent.currentScore == 1.0

        where:
        a << (1..31 as List)


    }

    def Agent createAgent(BinaryThresholdCmap model,List interests) {
        Agent a = new Agent(new BinaryThresholdCmap(model.nodecount,model.density),model.indexToVertex.keySet() as List)
        a.interests = interests
        a.update(model)
        a

    }


    def "make sure that an agent that approaches another becomes more similar (cosine similarity)"() {
        setup:
        BinaryThresholdCmap model = new BinaryThresholdCmap(5,0.7)
        Agent agent = createAgent(model,[a])
        Agent agent2 = createAgent(model,[a])


        when:
        double initial = agent.cosineSimilarity(agent2)
        agent.approach(agent2.model,0.5)

        then:
        agent.cosineSimilarity(agent2) > initial

        where:
        a << (1..31 as List)
    }

    def "make sure that an agent that approaches another with amount = 0 doesn't change"() {
        setup:
        BinaryThresholdCmap model = new BinaryThresholdCmap(5,0.7)
        Agent agent = createAgent(model,[a])
        Agent agent2 = createAgent(model,[a])
        double currentScore = agent.currentScore


        when:
        double initial = agent.cosineSimilarity(agent2)
        agent.approach(agent2.model,0.0)
        agent.update(model)

        then:
        agent.cosineSimilarity(agent2) == initial
        agent.currentScore == currentScore

        where:
        a << (1..31 as List)
    }


    def "make sure that an agent that approaches another with amount = 1 becomes the same as the other agent"() {
        setup:
        BinaryThresholdCmap model = new BinaryThresholdCmap(5,0.7)
        Agent agent = createAgent(model,[a])
        Agent agent2 = createAgent(model,[a])



        when:
        agent.approach(agent2.model,1.0)
        agent.update(model)

        then:
        Assert.assertEquals(agent.cosineSimilarity(agent2),1.0,0.001)
        agent.currentScore == agent2.currentScore

        where:
        a << (1..31 as List)
    }

}