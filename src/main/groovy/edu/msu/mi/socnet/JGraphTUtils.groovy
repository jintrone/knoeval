package edu.msu.mi.socnet

import cern.colt.matrix.tdouble.DoubleMatrix2D
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D
import groovy.util.logging.Log4j
import org.jgrapht.Graph
import org.jgrapht.VertexFactory
import org.jgrapht.WeightedGraph
import org.jgrapht.ext.IntegerEdgeNameProvider
import org.jgrapht.ext.VertexNameProvider
import org.jgrapht.generate.RandomGraphGenerator
import org.jgrapht.generate.RingGraphGenerator
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.SimpleWeightedGraph

/**
 * Created by josh on 4/4/15.
 */

class JGraphTUtils {

    static <T, E> E safeAdd(Graph<T, E> graph, T from, T to) {
        if (graph.containsEdge(from, to)) {
            return graph.getEdge(from, to)
        }
        if (!graph.containsVertex(from)) {
            graph.addVertex(from)
        }
        if (!graph.containsVertex(to)) {
            graph.addVertex(to)
        }

        graph.addEdge(from, to)

    }

    static <T, E> E safeAddWithDefault(WeightedGraph<T, E> graph, T from, T to, double weight) {
        if (graph.containsEdge(from, to)) {
            return graph.getEdge(from, to)
        }
        if (!graph.containsVertex(from)) {
            graph.addVertex(from)
        }
        if (!graph.containsVertex(to)) {
            graph.addVertex(to)
        }

        E edge = graph.addEdge(from, to)
        graph.setEdgeWeight(edge, weight)
        edge

    }

    static <T, E> E safeAdd(WeightedGraph<T, E> graph, T from, T to, double weight) {
        E result = safeAdd(graph, from, to)
        graph.setEdgeWeight(result, weight)
        result
    }


    static <T, E> E safeAddIncrement(WeightedGraph<T, E> graph, T from, T to, double weight) {
        boolean inc = graph.containsEdge(from, to)
        E result = safeAdd(graph, from, to)
        if (inc) {
            double current = graph.getEdgeWeight(result)
            graph.setEdgeWeight(result, current + weight)
        } else {
            graph.setEdgeWeight(result, weight)
        }
        result
    }

    static <T, E> double safeGetWeight(WeightedGraph<T, E> graph, T from, T to) {
        E edge = graph.getEdge(from, to)
        edge ? graph.getEdgeWeight(edge) : 0.0
    }

    static <T, E> List<T> sortByEdgeWeight(WeightedGraph<T, E> graph, Closure fx = {
        vertex -> -graph.edgesOf(vertex).sum { graph.getEdgeWeight(it) }
    }) {
        List<T> v = graph.vertexSet() as List<T>
        v.sort(true, fx)
    }

    static <V, E> DoubleMatrix2D asMatrix(WeightedGraph<V, E> graph, List vertices = graph.vertexSet() as List, Closure weightfunc = { weight -> weight }) {
        DoubleMatrix2D result = new SparseDoubleMatrix2D(vertices.size(), vertices.size())
        vertices.eachWithIndex { f, i1 ->
            result.setQuick(i1, i1, 0.0)
            vertices.subList(i1, vertices.size()).eachWithIndex { t, ix ->
                int i2 = ix + i1
                result.setQuick(i1, i2, weightfunc(safeGetWeight(graph, f, t)))
                result.setQuick(i2, i1, weightfunc(safeGetWeight(graph, t, f)))

            }
        }
        result

    }

    static <V, E> V getAlter(WeightedGraph<V, E> graph, V node, E edge) {
        graph.getEdgeSource(edge) == node ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge)
    }

    static writeGraphML(File f, final SimpleWeightedGraph<AttributeVertex, AttributeEdge> graph) {

        MyGraphMLExporter<AttributeVertex, AttributeEdge> exporter = new MyGraphMLExporter<>(
                new VertexNameProvider<AttributeVertex>() {

                    @Override
                    String getVertexName(AttributeVertex vertex) {
                        vertex.id
                    }
                }, null, new IntegerEdgeNameProvider<AttributeEdge>(), null,
                new EdgeWeightProvider<AttributeEdge>() {
                    @Override
                    float getEdgeWeight(AttributeEdge edge) {
                        graph.getEdgeWeight(edge)
                    }
                }, new AttributeProvider() {
            @Override
            MyGraphMLExporter.AttributeType getType() {
                MyGraphMLExporter.AttributeType.VERTEX
            }

            @Override
            String getName() {
                return "type"
            }

            @Override
            String getDataType() {
                return "string"
            }

            @Override
            String getValue(Object obj) {
                return ((AttributeVertex) obj).getAttributeValue("type")
            }
        }
        )
        exporter.export(new FileWriter(f), graph)
    }

    static SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge> createRandomWeightedGraph(int nodecount, double density) {
        SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge> graph = new SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge>(AttributeEdge.class) {
            public String toString() {
                edgeSet().collect {
                    "${getEdgeSource(it).id}->${getEdgeTarget(it).id} (${getEdgeWeight(it)})"
                }.join(",")
            }
        }
        int nedges = (nodecount * (nodecount - 1)) * density
        new RandomGraphGenerator(nodecount, nedges).generateGraph(graph, new VertexFactory<AttributeVertex>() {

            private int localid = 0;

            @Override
            AttributeVertex createVertex() {
                return new AttributeVertex("N" + (this.localid++))
            }
        }, null)
        return graph
    }

    static SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge> createFixedDegreeRandomWeightedGraph(int nodecount, int degree) {
        SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge> graph = new SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge>(AttributeEdge.class) {
            public String toString() {
                edgeSet().collect {
                    "${getEdgeSource(it).id}->${getEdgeTarget(it).id} (${getEdgeWeight(it)})"
                }.join(",")
            }
        }
        VertexFactory<AttributeVertex> vf = new VertexFactory<AttributeVertex>() {

            private int localid = 0;

            @Override
            AttributeVertex createVertex() {
                return new AttributeVertex("N" + (this.localid++))
            }
        }

        (0..<nodecount).each {
            graph.addVertex(vf.createVertex())
        }

        List targets = graph.vertexSet() as List
        List all = graph.vertexSet() as List

        while (targets) {
            AttributeVertex target = targets.pop()
            def sources = all - target
            Collections.shuffle(sources)
            JGraphTUtils.safeAdd(graph,sources[0],target,1.0)
            JGraphTUtils.safeAdd(graph,sources[1],target,1.0)
        }
        return graph
    }

    static SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge> createRingGraph(int nodecount) {
        SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge> graph = new SimpleDirectedWeightedGraph<AttributeVertex, AttributeEdge>(AttributeEdge.class) {
            public String toString() {
                edgeSet().collect {
                    "${getEdgeSource(it).id}->${getEdgeTarget(it).id} (${getEdgeWeight(it)})"
                }.join(",")
            }
        }

        new RingGraphGenerator<AttributeVertex, AttributeEdge>(nodecount).generateGraph(graph, new VertexFactory<AttributeVertex>() {

            private int localid = 0;

            @Override
            AttributeVertex createVertex() {
                return new AttributeVertex("N" + (this.localid++))
            }
        }, null)
        return graph
    }

    static <V> SimpleGraph<V, AttributeEdge> createRandomUnweightedGraph(int nodecount, double density, VertexFactory<V> factory) {
        SimpleGraph<V, AttributeEdge> graph = new SimpleGraph<V, AttributeEdge>(AttributeEdge.class);
        int nedges = (nodecount * (nodecount - 1) / 2) * density
        new RandomGraphGenerator(nodecount, nedges).generateGraph(graph, factory, null)
        return graph
    }

    static void randomizeWeights(WeightedGraph<AttributeVertex, AttributeEdge> graph, double min = 0.0, double max = 1.0) {
        Random r = new Random()
        graph.edgeSet().each {
            graph.setEdgeWeight(it, (r.nextDouble() * (max - min)) + min)
        }
    }

    static <V, E> WeightedGraph<V, E> copy(WeightedGraph<V, E> from, WeightedGraph<V, E> to) {
        to.removeAllEdges(to.edgeSet())
        from.edgeSet().each { E edge ->
            safeAdd(to, from.getEdgeSource(edge), from.getEdgeTarget(edge), from.getEdgeWeight(edge))
        }
        to
    }

    static <V, E> String toPrettyString(WeightedGraph<V, E> graph) {
        graph.edgeSet().collect {
            "${graph.getEdgeSource(it).id}->${graph.getEdgeTarget(it).id}(${graph.getEdgeWeight(it).round(2)})"
        }.join(",")
    }

    static <V, E> double cosineSimilarity(WeightedGraph<V, E> one, WeightedGraph<V, E> two, Map indicesOne = null, Map indicesTwo = null) {
        if (one.vertexSet().size() != two.vertexSet().size()) {
            return 0.0
        }
        if (!indicesOne) {
            int i = 0
            indicesOne = one.vertexSet().collectEntries { [i++, it] }
        }
        if (!indicesTwo) {
            int i = 0
            indicesTwo = two.vertexSet().collectEntries { [i++, it] }
        }

        double denomOne = 0.0, denomTwo = 0.0

        double num = (0..<one.vertexSet().size()).sum { int from ->
            int f = from + 1
            Double result = (f..<one.vertexSet().size()).sum { int to ->
                E eone = one.getEdge(indicesOne[from], indicesOne[to])
                E etwo = two.getEdge(indicesTwo[from], indicesTwo[to])

                double wone = eone ? one.getEdgeWeight(eone) : 0
                double wtwo = etwo ? two.getEdgeWeight(etwo) : 0

                denomOne += (wone**2)
                denomTwo += (wtwo**2)

                wone * wtwo
            }
            result ?: 0.0

        }

        num / (Math.sqrt(denomOne) * Math.sqrt(denomTwo))


    }

    static <V, E> void approach(WeightedGraph<V, E> from, WeightedGraph<V, E> to, double amount = 0.0) {
        to.edgeSet().each { E edge ->
            V s = to.getEdgeSource(edge)
            V d = to.getEdgeTarget(edge)
            E ne = safeAddWithDefault(from, s, d, 0.0)
            double fw = from.getEdgeWeight(ne)
            double tw = to.getEdgeWeight(edge)
            from.setEdgeWeight(ne, fw + (tw - fw) * amount)
        }

        new HashSet<>(from.edgeSet()).each { E edge ->
            V s = to.getEdgeSource(edge)
            V d = to.getEdgeTarget(edge)
            E te = to.getEdge(s, d)
            if (!te) {
                double fw = from.getEdgeWeight(edge)
                double nw = fw - (fw * amount)
                if (Math.abs(nw) < 0.0001 && amount > 0) {
                    from.removeEdge(edge)
                } else {
                    from.setEdgeWeight(edge, nw)
                }
            }
        }
    }

    static <V, E> void approach(WeightedGraph<V, E> from, WeightedGraph<V, E> to, Map vmap1, Map vmap2, double amount = 0.0) {
        int maxIdx = vmap2.keySet().max()
        (0..maxIdx).each { int fv ->
            (0..maxIdx).each { int tv ->
                if (fv != tv) {
                    E te = to.getEdge(vmap2[fv], vmap2[tv])
                    if (te) {
                        E ne = safeAddWithDefault(from, vmap1[fv], vmap1[tv], 0.0)
                        double fw = from.getEdgeWeight(ne)
                        double tw = to.getEdgeWeight(te)
                        from.setEdgeWeight(ne, fw + (tw - fw) * amount)
                    } else {
                        E fe = from.getEdge(vmap1[fv], vmap1[tv])
                        if (fe) {
                            double fw = from.getEdgeWeight(fe)
                            double nw = fw - (fw * amount)
                            if (nw == 0.0 || (nw == fw && amount > 0)) {

                                from.removeEdge(fe)
                            } else {
                                from.setEdgeWeight(fe, nw)
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * Copy "links" random links from the target network
     *
     * @param from
     * @param to
     * @param links
     */
    static <V, E> void adopt(WeightedGraph<V, E> from, WeightedGraph<V, E> to, int links = 1, Random r = null) {
        List edges = to.edgeSet().collect {
            [type: "t", edge: it]
        } + from.edgeSet().collect {
            [type: "f", edge: it]
        } as List

        Random rand = r ?: new Random()
        while (links--) {
            def target = edges.remove(rand.nextInt(edges.size()))
            if (target.type == "t") {
                V s = to.getEdgeSource(target.edge)
                V d = to.getEdgeTarget(target.edge)
                safeAdd(from, s, d, to.getEdgeWeight(target.edge))
            } else {
                V s = from.getEdgeSource(target.edge)
                V d = from.getEdgeTarget(target.edge)

                E e = to.getEdge(s,d)
                if (!e) {
                    from.removeEdge(target.edge)
                } else {
                    from.setEdgeWeight(e,to.getEdgeWeight(e))
                }

            }

        }
        from

    }


}
