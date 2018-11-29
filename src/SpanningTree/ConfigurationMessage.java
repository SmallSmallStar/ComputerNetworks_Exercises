package SpanningTree;

public class ConfigurationMessage implements Comparable<ConfigurationMessage>
{
    // 发送消息的网桥的编号。
    public int sendMegBridgeId;

    // 发送网桥认定的根网桥编号。
    public int rootId;

    // 发送网桥到根网桥的距离。
    public int distanceToRoot;

    /**
     * 配置消息的构造方法。
     * @param sendMegBridgeId 发送消息的网桥的编号。
     * @param rootId 发送网桥认定的根网桥编号。
     * @param distanceToRoot 发送网桥到根网桥的距离。
     */
    public ConfigurationMessage(int sendMegBridgeId, int rootId, int distanceToRoot)
    {
        this.sendMegBridgeId = sendMegBridgeId;
        this.rootId = rootId;
        this.distanceToRoot = distanceToRoot;
    }

    public int getSendMegBridgeId()
    {
        return sendMegBridgeId;
    }

    public int getRootId()
    {
        return rootId;
    }

    public int getDistanceToRoot()
    {
        return distanceToRoot;
    }

    @Override
    public int compareTo(ConfigurationMessage o)
    {
        return 1;
    }
}
