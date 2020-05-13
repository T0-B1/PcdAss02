package commons;

public abstract class Controller {

    protected final MyGraph graph;

    public Controller(MyGraph graph) {
        this.graph = graph;
    }

    public abstract void start(String startingURL, int depth);

    public void stop() { graph.stopRendering(); }

}
