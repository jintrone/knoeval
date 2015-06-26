package edu.msu.mi.cmap

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

    public static double score(List test, List truth, List awareness = null) {
        double result = 0
        if (awareness == null) {
            awareness = 0..<(test ?: truth)[0].size()
        }
        (1..<Math.min(truth.size(), test.size())).each { idx ->
            def truval = truth[idx] // should be an array of binary
            def testval = test[idx]

            result += awareness.sum { idx2 ->
                (1 - Math.abs(truval[idx2] - testval[idx2]))
            } / awareness.size()

        }
        return result / (Math.max(test.size(), truth.size()) - 1)
    }

    def void approach(BinaryThresholdCmap other, double amount) {
        JGraphTUtils.approach(model.graph, other.graph, amount)
        model.reset()
    }

    def double probe(BinaryThresholdCmap reality, Integer question) {

        List behavior = model.getBehavior(question)
        score(behavior, reality.getBehavior(question),awareness)
    }

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
