package rx;

import commons.Controller;
import commons.MyGraph;
import commons.Node;
import commons.Page;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RxController extends Controller {

    public RxController(MyGraph graph) {
        super(graph);
    }

    @Override
    public void start(String startingURL, int depth) {
        Page firstPage = new Page(startingURL, "", depth);
        graph.clear();
        Flowable<Node> nodes = scrape(firstPage);
        nodes.observeOn(Schedulers.io())
                .subscribe(n -> {
            log("Adding edge "+n.getFatherId()+ " "+n.getLabel());
            if(n.getFatherId() != "")
                graph.addDirectedEdge(n.getFatherId(), n.getId());
            graph.setdNodeLabel(n.getId(), n.getLabel());
        });
    }

    Flowable<Node> scrape(Page page) {
        log("Scraping "+page.getUrl()+ " "+page.getDepth());
        Flowable<Node> thisNode = Flowable.just(new Node(page.getUrl(), page.getFather(), getDocumentTitleFromUrl(page.getUrl())));
        if(page.isLeaf())
            return thisNode;
        return Flowable.merge(
                thisNode,
                Flowable.fromCallable(() -> getDocument(page.getUrl()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .map(this::getLinksInDocument)
                        .flatMapIterable(linkedUrl->linkedUrl)
                        .map(url-> new Page(url, page.getUrl(), page.nextDepth()))
                        .flatMap(this::scrape)
        );
    }

    private Document getDocument(String url) {
        log("getting page "+url);
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            return null;
        }
    }

    private String getDocumentTitle(Document doc) {
        return doc.getElementById("firstHeading").text();
    }

    private String getDocumentTitleFromUrl(String url) {
        return url.substring(url.lastIndexOf('/')+1);
    }

    private List<String> getLinksInDocument(Document doc) {
        log("getting links "+doc.title());
        List<String> links = new ArrayList<>();
        doc.getElementById("mw-content-text")
                .getElementsByClass("mw-parser-output").first()
                .getElementsByTag("p").stream()
                .filter(e -> !e.text().isEmpty())
                .findFirst().ifPresent(f -> {
            f.getElementsByAttribute("href").stream()
                    // TODO for debug ease
                    //.limit(2)
                    .map(e -> e.attr("href"))
                    .filter(e -> e.startsWith("/wiki/"))
                    .map("https://en.wikipedia.org"::concat)
                    .forEach(links::add);
        });
        return links;
    }

    static private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + " ] " + msg);
    }

}
