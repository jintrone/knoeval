package edu.msu.mi.socnet


import groovy.xml.StreamingMarkupBuilder
import org.jgrapht.graph.SimpleWeightedGraph

/**
 * Created by josh on 5/30/15.
 */
class GroovyGraphMLExporter<V, E> {


    SimpleWeightedGraph<V, E> graph
    List nodeProviders = []
    List edgeProviders = []

    public GroovyGraphMLExporter(SimpleWeightedGraph<V, E> graph, AttributeProvider... others) {
        this.graph = graph
        others.each {
            (it.type == MyGraphMLExporter.AttributeType.VERTEX ? nodeProviders : edgeProviders) << it
        }
    }

    public void export(Writer out) {
//        <graphml xmlns="http://graphml.graphdrawing.org/xmlns"
//        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//        xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns
//        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">

        def comment = "<![CDATA[<!-- address is new to this release -->]]>"
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        builder.encoding = "UTF-8"
        def graphml = {
            mkp.xmlDeclaration()
            mkp.declareNamespace('': "http://graphml.graphdrawing.org/xmlns",
                    "xsi": "http://www.w3.org/2001/XMLSchema-instance")
            graphml('xsi:schemaLocation': "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd") {
                key(id: "name", for: "node", 'attr.name': "name", 'attr.type': "string")
                nodeProviders.each { AttributeProvider p->
                    key(id:"${p.name}",for:"node",'attr.name':"${p.name}",'attr.type':"${p.dataType}")

                }
                key(id: "weight", for: "edge", 'attr.name': "weight", 'attr.type': "double")
                edgeProviders.each { AttributeProvider p->
                    key(id:"${p.name}",for:"edge",'attr.name':"${p.name}",'attr.type':"${p.dataType}")

                }


                graph(id: "G", edgedefault: "undirected") {
                    graph.vertexSet().each { vertex ->
                        node(id: vertex.id) {
                            data(key: "name", vertex.id)
                            nodeProviders.each { AttributeProvider p->
                                data(key:"${p.name}","${p.getValue(vertex)}")

                            }
                        }
                    }

                    graph.edgeSet().each { edgeIt ->
                        edge(source: "${graph.getEdgeSource(edgeIt).id}", target: "${graph.getEdgeTarget(edgeIt).id}") {
                            data(key: "weight", "${ graph.getEdgeWeight(edgeIt)}")
                        }

                    }


                }

            }

        }


        out << builder.bind(graphml)

    }
}
