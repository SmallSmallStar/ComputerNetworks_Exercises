package SpanningTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ConcurrentModificationException;
import java.util.Scanner;

public class SpanningTreeMT
{
    // 判断每次迭代是否收敛（网桥和局域网的配置消息全部更新完成）
    private Bool isConverged;

    // 网桥更新类，实现Runnable接口。
    private class BridgeUpdater implements Runnable
    {
        public Bridge bridge;

        public BridgeUpdater(Bridge bridge)
        {
            this.bridge = bridge;
        }

        @Override
        public void run()
        {
            while (!isConverged.getValue())
            {
                try
                {
                    bridge.update();
                    Thread.sleep(5);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ConcurrentModificationException e)
                {

                }
                catch (NullPointerException e)
                {

                }
            }
        }
    }

    // 局域网更新类，实现Runnable接口。
    private class LanUpdater implements Runnable
    {
        public Lan lan;

        public LanUpdater(Lan lan)
        {
            this.lan = lan;
        }

        @Override
        public void run()
        {
            while (!isConverged.getValue())
            {
                try
                {
                    lan.transmit();
                    Thread.sleep(5);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ConcurrentModificationException e)
                {

                }
                catch (NullPointerException e)
                {

                }
            }
        }
    }

    // 网桥的个数。
    public int bridgeCount;

    // 局域网的个数。
    public int lanCount;

    // 网桥数组。
    private Bridge[] bridges;

    // 局域网数组。
    private Lan[] lans;

    // 每个网桥认为的根网桥编号。
    private int[] rootId;

    /**
     * 生成树多线程实现的构造方法。
     * @param bridgeCount 网桥的数量。
     * @param lanCount 局域网的数量。
     */
    public SpanningTreeMT(int bridgeCount, int lanCount)
    {
        initialize(bridgeCount,lanCount);
    }

    /**
     * 初始化带环的扩展局域网。
     * @param bridgeCount 网桥的数量。
     * @param lanCount 局域网的数量。
     */
    private void initialize(int bridgeCount, int lanCount)
    {
        isConverged = new Bool(false);

        // 给网桥和局域网的数量赋值。
        this.bridgeCount = bridgeCount;
        this.lanCount = lanCount;

        // 初始化网桥的信息。
        bridges = new Bridge[bridgeCount];
        for (int i = 0; i < bridgeCount; i++)
            bridges[i] = new Bridge(i);

        // 初始化局域网的信息。
        lans = new Lan[lanCount];
        for (int i = 0; i < lanCount; i++)
            lans[i] = new Lan(i);

        // 初始化各个网桥认为的根节点。
        rootId = new int[bridgeCount];
        for (int i = 0; i < bridgeCount; i++)
            rootId[i] = i;
    }

    /**
     * 生成树多线程实现的构造方法。
     * @param filepath 文件路径。
     * @throws FileNotFoundException 文件找不到的时候抛出找不到此文件异常。
     */
    public SpanningTreeMT(String filepath) throws FileNotFoundException
    {
        // 用Scanner来读取文件。
        File file = new File(filepath);
        Scanner scanner = new Scanner(file);

        // 文件的第一行是网桥的个数。
        bridgeCount = Integer.parseInt(scanner.nextLine());
        // 文件的第二行是局域网的个数。
        lanCount = Integer.parseInt(scanner.nextLine());

        // 初始化带环的扩展局域网。
        initialize(bridgeCount, lanCount);

        // 读取文件中网桥与局域网的互连信息。
        while (scanner.hasNext())
        {
            // 扩展局域网的文件存储格式：【网桥编号】【connect】【局域网编号】。
            String[] line = scanner.nextLine().split(" connect ");
            int bridgeId = Integer.parseInt(line[0]);
            int lanId = Integer.parseInt(line[1]);
            addConnection(bridgeId, lanId);
        }

        // 关闭读文件的scanner。
        scanner.close();
    }

    /**
     * 将网桥与局域网连接起来，用于成树单线程实现的构造方法。
     * @param bridgeId 网桥的编号。
     * @param lanId 局域网的编号。
     */
    private void addConnection(int bridgeId, int lanId)
    {
        bridges[bridgeId].connectLan(lans[lanId]);
        lans[lanId].connectBridge(bridges[bridgeId]);
    }

    public void run()
    {
        // 清空生成树。
        clearSpanningTree();

        for (int i = 0; i < bridges.length; i++)
        {
            BridgeUpdater bridgeUpdater = new BridgeUpdater(bridges[i]);
            Thread thread = new Thread(bridgeUpdater);
            thread.start();
        }

        // 每个网桥负责转发配置消息。
        for (int i = 0; i < lans.length; i++)
        {
            LanUpdater lanUpdater = new LanUpdater(lans[i]);
            Thread thread = new Thread(lanUpdater);
            thread.start();
        }

        for (;;)
        {
            // 如果更新全部完成，程序结束。
            if (isConverged())
            {
                isConverged.setValue(true);
                break;
            }
            else
            {
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // 将局域网信息显示出来。
        for (Lan lan : lans)
            System.out.println(lan);

        // 将网桥信息显示出来。
        for (Bridge bridge : bridges)
            System.out.println(bridge);

        System.out.println("\n");

    }

    /**
     * 清空生成树，用于单线程解生成树。
     */
    public void clearSpanningTree()
    {
        for (Bridge bridge : bridges)
            bridge.clearMessage();

        for (int i = 0; i < bridges.length; i++)
            rootId[i] = i;
    }

    /**
     * 判断是否迭代完成。
     * @return 如果每个网桥的配置消息中的跟网桥是一样的，则配置消息全部正确，返回true，否则返回false。
     */
    private boolean isConverged()
    {
        // 更新配置消息。
        updateInformation();

        // 取出第一个网桥认为的跟网桥。
        int correctRootId = rootId[0];

        // 查看其它网桥认为的跟网桥是否一样。
        for (int i = 1; i < this.bridgeCount; i++)
        {
            if (rootId[i] != correctRootId)
                return false;
        }
        return true;
    }

    /**
     * 更新每个网桥的配置消息，用于判断程序是否运行结束。
     */
    private void updateInformation()
    {
        for (int i = 0; i < this.bridgeCount; i++)
            rootId[i] = bridges[i].rootBridgeId;
    }

    /**
     * 将生成树中的网桥与局域网的连接信息显示出来。
     * @return 返回连接信息。
     */
    public String toString()
    {
        StringBuilder connectionInformation = new StringBuilder();
        for (Bridge bridge : bridges)
            connectionInformation.append(bridge.connectionInformation());
        return connectionInformation.toString();
    }
}