package edu.msu.mi.cmap.simulation

import edu.msu.mi.cmap.BinaryThresholdCmap
import edu.msu.mi.cmap.analysis.Toolkit
import edu.msu.mi.socnet.JGraphTUtils

/**
 * Created by josh on 6/9/15.
 */
class Agent {

    static int GLOBAL_ID = 0

    BinaryThresholdCmap model
    double currentScore

    //these are indices into reality that I am aware of
    List awareness

    //these are states that I am interested in (in base reality)
    List<Integer> interests


    final int id = GLOBAL_ID++
    Random random

    public Agent() {

    }

    public Agent(BinaryThresholdCmap model, List data) {
        this.model = model
        this.awareness = data
        this.random = new Random()
    }



    def void approach(BinaryThresholdCmap other, double amount) {
        JGraphTUtils.approach(model.graph, other.graph, amount)
        model.reset()
    }

    def double probe(BinaryThresholdCmap reality, Integer question) {

        List behavior = model.getBehavior(question)
        Toolkit.score(behavior, reality.getBehavior(question),awareness)
    }

    //@TODO Make this into a method that externalizes the states an agent is exposed to
    def update(BinaryThresholdCmap reality) {

        currentScore = probe(reality, interests[random.nextInt(interests.size())])
    }

    def cosineSimilarity(Agent a) {
        JGraphTUtils.cosineSimilarity(this.model.graph,a.model.graph,this.model.indexToVertex,a.model.indexToVertex)
    }

    def String reportStandardEdgeWeights() {
        model.reportStandardEdgeWeights()
    }

}
