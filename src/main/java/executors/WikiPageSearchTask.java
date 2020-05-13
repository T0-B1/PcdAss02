package executors;

import commons.MyGraph;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class WikiPageSearchTask extends RecursiveAction {

    private final String page;
    private final int depth;
    private final MyGraph graph;

    WikiPageSearchTask(String page, int depth, MyGraph graph) {
        this.page = page;
        this.depth = depth;
        this.graph = graph;
    }

    @Override
    public void compute() {
        List<String> related = new ArrayList<>();
        Document doc;
        String title;

        try {
            doc = Jsoup.connect(page).get();
        } catch (IOException e) {
            return;
        }

        title = doc.getElementById("firstHeading").text();

        // https://github.com/graphstream/gs-core/issues/293
        graph.setdNodeLabel(page, title);

        if(depth > 0) {
            doc.getElementById("mw-content-text")
                    .getElementsByClass("mw-parser-output").first()
                    .getElementsByTag("p").stream()
                    .filter(e -> !e.text().isEmpty())
                    .findFirst().ifPresent(f -> {
                f.getElementsByAttribute("href").stream()
                        .map(e -> e.attr("href"))
                        .filter(e -> e.startsWith("/wiki/"))
                        .map("https://en.wikipedia.org"::concat)
                        .forEach(link -> {
                            new WikiPageSearchTask(link, depth - 1, graph).fork();
                            graph.addDirectedEdge(page, link);
                            //related.add(link);
                        });
            });
/*
            // This way is not any faster
            graph.addDirectedEdges(
                related.stream()
                        .map(link-> new AbstractMap.SimpleEntry<String, String>(page, link))
                        .collect(Collectors.toList()));
            for(String link : related)
               new WikiPageSearchTask(link, depth - 1, graph).fork();
*/
        }
    }
}
