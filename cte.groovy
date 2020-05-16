@Grapes([
    @Grab("com.h2database:h2:1.4.198")
])
import cte.*
import javax.jdo.annotations.*
import javax.jdo.Constants;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory
import org.datanucleus.metadata.PersistenceUnitMetaData
import org.datanucleus.metadata.TransactionType

def pumd = new PersistenceUnitMetaData("dynamic-unit", "RESOURCE_LOCAL", null)
def configuration = [
    "javax.jdo.option.ConnectionURL": "jdbc:h2:mem:cte",
    "javax.jdo.option.ConnectionUserName": "sa",
    "javax.jdo.option.ConnectionPassword": "",
    "datanucleus.schems.autoCreateAll": "true",
    "datanucleus.schema.autoCreateTables": "true"
]
factory = new JDOPersistenceManagerFactory(pumd, configuration)
factory.transactionIsolationLevel = Constants.TX_READ_COMMITTED
factory.transactionType = TransactionType.RESOURCE_LOCAL.toString()

def txn(Closure block) {
    def pm = factory.persistenceManager
    tx = pm.currentTransaction()
    try {
        tx.begin()
        def result = block.call(pm)
        tx.commit()
        return result
    } finally {
        if (tx.isActive()) {
            tx.rollback()
        }
        pm.close()
    }
}

// txn { pm ->
//     def inv = new Inventory("My Inventory")
//     def product = new Product("Sony Discman", "A standard discman from Sony", 49.99)
//     inv.products.add(product)
//     pm.makePersistent(inv)
// }

// txn { pm ->
//     def q = pm.newQuery("SELECT FROM ${Product.class.name} WHERE price < 150.00 ORDER BY price ASC")
//     def products = q.executeList()
//     products.each { println it.inspect() }
// }

def heads = []

txn { pm ->
    1.times { n ->
        def parent = null
        10.times { m ->
            def node = new Node()
            if (parent != null) {
                node.parent = parent.id
            } else {
                heads << node.id
            }
            def value = String.format("%d:%04d", n, m)
            def data = new Data(node, value)
            pm.makePersistent(node)
            pm.makePersistent(data)
            parent = node
        }
    }
}

def descendants(String id) {
    def results = []
    txn { pm ->
        def parent = id
        while (true) {
            def q = pm.newQuery("""
                SELECT UNIQUE id INTO String FROM ${Node.class.name}
                WHERE parent == :parent
            """.trim())
            def result = q.execute(parent)
            if (result == null) {
                break;
            } else {
                results << result
                parent = result
            }
        }
    }
    return results
}

heads.each {
    println it
    descendants(it).each { println "> $it" }
    println()
}

txn { pm ->
    def q = pm.newQuery("SELECT FROM ${Data.class.name}")
    def results = q.executeList()
    results.each { println it }
}

def printDescendantsData(String id) {
    def descendants = descendants(id)
    txn { pm ->
        def q = pm.newQuery("""
            SELECT FROM ${Data.class.name}
            WHERE descendants.contains(this.nodeId)
            PARAMETERS java.util.List descendants
        """.trim())
        q.execute(descendants).each { println "> $it" }
    }
}

heads.each {
    println it
    printDescendantsData(it)
    println()
}
