package org.jetbrains.exposed.gradle

import com.hendrix.erdos.algorithms.TopologicalSort
import com.hendrix.erdos.graphs.SimpleDirectedGraph
import com.hendrix.erdos.types.Vertex
import schemacrawler.schema.Table

private fun buildTableGraph(tables: List<Table>): SimpleDirectedGraph {
    val graph = SimpleDirectedGraph()
    val tablesToVertices = mutableMapOf<Table, Vertex<Table>>()
    for (table in tables) {
        val v = Vertex<Table>(table.fullName).apply { data = table }
        graph.addVertex(v)
        tablesToVertices[table] = v
        for (column in table.columns) {
            if (column.referencedColumn != null) {
                val referencedTable = column.referencedColumn.parent
                val u = tablesToVertices.getOrDefault(referencedTable,
                        Vertex<Table>(referencedTable.fullName).apply { data = referencedTable }
                )
                graph.addEdge(u, v)
            }
        }
    }
    return graph
}

fun sortTablesByDependencies(tables: List<Table>): List<Table> {
    val graph = buildTableGraph(tables)
    val topologicalSort = TopologicalSort(graph)
    return topologicalSort.applyAlgorithm().map { it.data as Table }
}