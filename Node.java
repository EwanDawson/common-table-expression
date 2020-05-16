package cte;
import java.util.UUID;
import javax.jdo.annotations.*;

@PersistenceCapable(detachable = "true", table = "Node")
class Node {
    @PrimaryKey
    @Column(length = 36)
    String id = UUID.randomUUID().toString();

    @Column(length = 36, allowsNull = "true")
    String parent;

    public String toString() {
        return "Node<" + id + "<-" + parent + ">";
    }
}
