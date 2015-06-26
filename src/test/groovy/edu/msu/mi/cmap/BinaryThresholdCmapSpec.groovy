package edu.msu.mi.cmap

import edu.msu.mi.socnet.AttributeEdge
import edu.msu.mi.socnet.JGraphTUtils
import org.jgrapht.alg.cycle.JohnsonSimpleCycles
import org.jgrapht.graph.SimpleDirectedGraph
import org.w3c.dom.Attr
import spock.lang.Specification


/**
 * Created by josh on 6/12/15.
 */
class BinaryThresholdCmapSpec extends Specification {

    def "check profile validity"() {
        setup:
        BinaryThresholdCmap model = new BinaryThresholdCmap(5,0.7)

        model.createProfile()

        expect:
        model.profile.each {k,v->
            assert k==BinaryThresholdCmap.fromBinary(v[0])
        }
   }

    def "check attractor structure"() {
        setup:
        BinaryThresholdCmap model = new BinaryThresholdCmap(5,0.7)

        //ring networks have a particular attractor profile; useful for testing
        model.setInitialNetwork(JGraphTUtils.createRingGraph(5))
        def attractorProfile = model.analyzeAttractorProfile()

        expect:
        attractorProfile.size() == 8
        int cycles = 0, fp = 0, total = 0

        attractorProfile.each {
            if (it.type == "CYCLE") cycles++
            else fp++
            total+=(it.basin + (it?.size?:0))
        }
        fp==2
        cycles==6

    }


}