package SpanningTree;

import java.io.FileNotFoundException;

public class Main
{
    public static void main(String[] args) throws FileNotFoundException
    {
        SpanningTreeST spanningTreeST = new SpanningTreeST("src/SpanningTree/loopLan.txt");
        spanningTreeST.run();
    }
}
