package edu.msu.mi.socnet;

/**
 * Created by josh on 4/4/15.
 */
public interface EdgeWeightProvider<E> {

    float getEdgeWeight(E edge);
}
