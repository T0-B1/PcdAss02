package commons;

import javafx.scene.layout.Pane;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyGraph {

    private final Graph graph;
    private final Viewer viewer;
    private final FxViewPanel viewPanel;

    private long startTime = System.currentTimeMillis();

    public MyGraph() {
        // labels update is broken in synchronizedGraph
        //graph = Graphs.synchronizedGraph(new SingleGraph("commons.MyGraph"));
        System.setProperty("org.graphstream.ui", "javafx");
        //System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph = new SingleGraph("commons.MyGraph");
        setGraphAttributes();
        viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        //viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        viewPanel =  (FxViewPanel) viewer.addDefaultView( false ) ;
    }

    private void setGraphAttributes() {
        String nodeStyle = "node {" +
                "size: 15px;" +
                "fill-color: white;" +
                "stroke-mode: plain;" +
                "text-size: 15px;" +
                //"text-mode:normal;" +
                //"text-background-mode: plain;" +
                //"fill-mode: dyn-plain;" +
                //"text-visibility-mode: normal;" +
                //"text-alignment: center;"+
                "}";
        graph.setAutoCreate(true);
        graph.setStrict(false);
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet", nodeStyle);
    }

    public synchronized void addNode(String node) {
        //System.out.println("ADDING NODE: "+node);
        graph.addNode(node);
    }

    public synchronized void addDirectedEdge(String from, String to) {
        graph.addEdge(from+to, from, to, true);
    }

    public synchronized void addDirectedEdges(List<Map.Entry<String, String>> edges) {
        // Temporarily disable force-based rendering algorithm
        if(viewer != null)
            viewer.disableAutoLayout();
        edges.forEach(e->{
            addDirectedEdge(e.getKey(), e.getValue());
        });
        if(viewer != null)
            viewer.enableAutoLayout();
    }

    public synchronized void setdNodeLabel(String node, String label) {
        Node n = graph.getNode(node);
        if( n != null) n.setAttribute("ui.label", label);
    }

    public void clear() {
        graph.clear();
        setGraphAttributes();
    }

    public Pane getPanel() {
        return viewPanel;
    }

    public void stopRendering() {
        viewer.close();
    }

    public void printStats() {
        System.out.println("E "+graph.getEdgeCount());
        System.out.println("N "+graph.getNodeCount());
        System.out.println((float)(System.currentTimeMillis()-startTime)/1000);
        System.out.println();
    }
}
