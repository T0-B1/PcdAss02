package commons;

public class Node{

    private final String id;
    private final String fatherId;
    private final String label;

    public Node(String id, String fatherId, String label) {
        this.id = id;
        this.fatherId = fatherId;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getFatherId() {
        return fatherId;
    }

    public String getLabel() {
        return label;
    }
}
