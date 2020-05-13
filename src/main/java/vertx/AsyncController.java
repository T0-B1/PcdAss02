package vertx;

import commons.Controller;
import commons.MyGraph;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AsyncController extends Controller {

    private Vertx vertx;
    private WebClient client;
    int initDepth;

    public AsyncController(MyGraph graph) {
        super(graph);
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);
    }

    @Override
    public void start(String startingURL, int depth) {
        stopVertx();
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);
        vertx.executeBlocking(promise->{
            graph.clear();
            graph.addNode(startingURL);
            scrape(startingURL, depth);
        }, res->{});
        initDepth = depth;
    }

    @Override
    public void stop() {
        super.stop();
        stopVertx();
    }

    private void stopVertx() {
        if(client != null) client.close();
        if(vertx != null) vertx.close();
    }

    private void scrape(String pageUrl, int depth) {
        URL url = null;
        try {
            url = new URL(pageUrl);
        } catch (MalformedURLException e) {
            return;
        }
        String host = url.getHost();
        String path = url.getPath();

        client
            .get(host,path)
            .send(ar -> {
                if (ar.succeeded()) {
                    log(depth, "Managing http response of "+pageUrl.substring(pageUrl.lastIndexOf('/'))+" "+depth+" "+Thread.currentThread());
                    HttpResponse<Buffer> response = ar.result();
                    vertx.executeBlocking(promise->{
                        log(depth, "Scraping content of "+pageUrl.substring(pageUrl.lastIndexOf('/'))+" "+depth+" "+Thread.currentThread());
                        Document doc = Jsoup.parse(ar.result().bodyAsString());
                        graph.setdNodeLabel(pageUrl, doc.getElementById("firstHeading").text());
                        if(depth > 0) {
                            getLinksInWikiPage(doc).forEach(link->{
                                scrape(link, depth-1);
                                graph.addDirectedEdge(pageUrl, link);
                            });
                        }
                        promise.complete();
                    }, res->{});
                } else {
                    log(depth, "Something went wrong " + ar.cause().getMessage());
                }
            });
    }

    private void log(int depth, String text) {
        for(int i =0;i< initDepth-depth;i++)
            System.out.print("  ");
        System.out.println(text);
    }

    private List<String> getLinksInWikiPage(Document doc) {
        List<String> links = new ArrayList<>();
        Optional<Element> p = doc.getElementById("mw-content-text")
                .getElementsByClass("mw-parser-output").first()
                .getElementsByTag("p").stream()
                .filter(e -> !e.text().isEmpty())
                .findFirst();
        if(p.isPresent())
            links = p.get().getElementsByAttribute("href").stream()
                    .map(e -> e.attr("href"))
                    .filter(e -> e.startsWith("/wiki/"))
                    .map("https://en.wikipedia.org"::concat)
                    .collect(Collectors.toList());
        return links;
    }
}
