package SpanningTree;

public class ThreadTest
{
    private static class SimpleThread implements Runnable
    {
        private int i;
        public SimpleThread(int i)
        {
            this.i = i;
        }

        @Override
        public void run()
        {
            System.out.println(i);
        }
    }

    public static void main(String[] args)
    {
        for (int i = 0; i < 20; i++)
            new Thread(new SimpleThread(i)).run();
    }
}
