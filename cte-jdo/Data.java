package cte;
import javax.jdo.annotations.*;

@PersistenceCapable(detachable = "true")
class Data {
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    long id;
    String nodeId;
    String value;

    Data(Node node, String value) {
        this.nodeId = node.id.toString();
        this.value = value.toString();
    }

    @Override
    public String toString() {
        return "Data<" + nodeId + " = " + value + ">";
    }
}
