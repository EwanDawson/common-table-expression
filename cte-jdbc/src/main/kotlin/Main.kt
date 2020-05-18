@file:JvmName("Main")

import java.util.UUID
import com.vladsch.kotlin.jdbc.*

fun main() {

    // Create a connection to a new in-memory database
    val session = session("jdbc:h2:./h2-database", "sa", "")

    // Create the NODE and DATA tables for our experiment
    session.execute(sqlQuery(
                       """
                       CREATE TABLE NODE (
                           ID VARCHAR(36) NOT NULL PRIMARY KEY,
                           PARENTID VARCHAR(36)
                       )"""))
    session.execute(sqlQuery(
                       """
                       CREATE TABLE DATA (
                           ID SERIAL NOT NULL PRIMARY KEY,
                           NODEID VARCHAR(36) NOT NULL,
                           DATA VARCHAR(256)
                       )"""))
    session.execute(sqlQuery(
                        """
                        CREATE INDEX NODE_PARENT_IDX
                        ON NODE (PARENTID)
                        """))
    session.execute(sqlQuery(
                        """
                        CREATE INDEX DATA_NODE_IDX
                        ON DATA (NODEID)
                        """))

    // Populate our NODE and DATA tables
    val heads = mutableListOf<String>()
    for (n in 1..20) {
        var parent : String? = null
        for (m in 1..1005) {
            val id = UUID.randomUUID().toString()
            val value = String.format("%d:%04d", n, m)
            session.update(sqlQuery(
                               "INSERT INTO NODE (ID, PARENTID) VALUES (?, ?)", id, parent
            ))
            session.update(sqlQuery(
                               "INSERT INTO DATA (NODEID, DATA) VALUES (?, ?)", id, value
            ))
            if (m == 1) heads.add(id)
            parent = id
        }
    }

    // Print out our database
    // session.list(
    //     sqlQuery("SELECT * FROM NODE"),
    //     { rs -> mapOf("id" to rs.stringOrNull("ID"), "parent" to rs.stringOrNull("PARENTID")) }
    // ).forEach { println(it) }
    // session.list(
    //     sqlQuery("SELECT * FROM DATA"),
    //     { rs -> mapOf("id" to rs.int("ID"), "node" to rs.stringOrNull("NODEID"), "data" to rs.stringOrNull("DATA")) }
    // ).forEach { println(it) }

    // Now show all the data for descendants of a particular node
    fun descendants(id: String) : List<String> {
        var appendChild = { id: String, lineage: MutableList<String> -> mutableListOf<String>() }
        appendChild =
            { parent: String, lineage: MutableList<String> ->
                  val query = sqlQuery("SELECT ID FROM NODE WHERE PARENTID = ?", parent)
              val child = session.first(query, { rs -> rs.stringOrNull("ID") } )
              if (child == null) {
                  lineage
              } else {
                  lineage.add(child)
                  appendChild(child, lineage)
              }
            }
        return appendChild(id, mutableListOf(id))
    }
    fun descendantsDataNaive(id: String) : List<String?> {
        val descendants = descendants(id)
        val param = descendants.map({ "?" }).joinToString()
        val sql = "SELECT DATA FROM DATA WHERE NODEID IN ($param) ORDER BY DATA"
        val query = sqlQuery(sql).paramsList(descendants(id))
        return session.list(query) {
            rs -> rs.stringOrNull("DATA")
        }
    }

    // See https://stackoverflow.com/a/19691965/205821
    fun descendantsDataCTE(id: String): List<String?> {
        val sql = """
        WITH RECURSIVE LINEAGE(id, parent) AS (
            SELECT ID, PARENTID FROM NODE WHERE ID = ?
          UNION ALL
            SELECT C.ID, C.PARENTID FROM LINEAGE
            INNER JOIN NODE AS C ON (LINEAGE.ID = C.PARENTID)
        )
        SELECT DATA FROM DATA INNER JOIN LINEAGE ON (DATA.NODEID = LINEAGE.ID)
        """
        val query = sqlQuery(sql, id)
        return session.list(query) {
            rs -> rs.stringOrNull("DATA")
        }
    }

    val start = System.currentTimeMillis()
    //heads.forEach { println(descendants(it)) }
    heads.forEach { descendantsDataNaive(it) }
    // heads.forEach { descendantsDataCTE(it) }
    println("Elapsed: ${System.currentTimeMillis() - start}ms")
}
