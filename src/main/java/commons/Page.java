package commons;

public class Page {

    private final String url;
    private final String father;
    private final int depth;

    public Page(String url, String father, int depth) {
        this.url = url;
        this.father = father;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public String getFather() {
        return father;
    }

    public int getDepth() {
        return depth;
    }

    public int nextDepth() {
        return depth-1;
    }

    public boolean isLeaf() {
        return depth <= 0;
    }
}
