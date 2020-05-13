package executors;

import commons.Controller;
import commons.MyGraph;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class TaskController extends Controller {

    private ForkJoinPool forkJoinPool;

    public TaskController(MyGraph graph) {
        super(graph);
    }

    @Override
    public void start(String startingURL, int depth) {
        // First graph rendering is quite expensive, executor prevents gui from become temporarily unresponsive
        Executors.newSingleThreadExecutor().execute(() ->
        {
            if(forkJoinPool != null)
                forkJoinPool.shutdownNow();
            forkJoinPool = new ForkJoinPool();
            graph.clear();
            graph.addNode(startingURL);
            forkJoinPool.invoke(new WikiPageSearchTask(startingURL, depth, graph));
        });
    }

    @Override
    public void stop() {
        super.stop();
        if(forkJoinPool != null) {
            forkJoinPool.shutdownNow();
            try {
                forkJoinPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
