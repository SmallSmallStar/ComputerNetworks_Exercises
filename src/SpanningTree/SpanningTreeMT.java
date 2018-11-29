package SpanningTree;

public class SpanningTreeMT
{
    private class Signal
    {
        public boolean isConverged;
        public Signal()
        {
            isConverged = false;
        }
    }

    private class BridgeUpdater
    {
        public Bridge bridge;
        private Signal signal;
    }
}