package SpanningTree;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Bridge implements Runnable
{
    // 网桥的编号。
    public int bridgeId;

    // 根网桥的编号。
    public int rootBridgeId;

    // 网桥到根网桥的距离。
    public int distanceToRootBridge;

    // 网桥的配置消息，是一个键值对<配置消息，与之相连的局域网>。
    public Map<ConfigurationMessage, Lan> messageQueue;

    // 网桥与局域网是否相连，是一个键值对<局域网，是否相连>。
    private Map<Lan, Boolean> connected;

    // 到根网桥的下一跳编号。
    private int nextHopToRoot;

    /**
     * 网桥的构造方法。
     * @param bridgeId 网桥的编号。
     */
    public Bridge(int bridgeId)
    {
        connected = new TreeMap<>();
        this.bridgeId = bridgeId;
        this.rootBridgeId = bridgeId;
        this.messageQueue = new TreeMap<>();
        nextHopToRoot = bridgeId;
    }

    /**
     * 判断网桥是不是根网桥。
     *
     * @return 如果网桥与根网桥的编号相同，则是根网桥，返回true，否则返回false。
     */
    public boolean isRoot()
    {
        if (bridgeId == rootBridgeId)
            return true;
        else
            return false;
    }

    /**
     * 增加与网桥相连的局域网。
     *
     * @param lan 与之相连的局域网。
     */
    public void connectLan(Lan lan)
    {
        // 如果局域网与这个网桥相连，在链表中添加这个局域网。
        connected.put(lan, true);
    }

    /**
     * 判断网桥是否与局域网相连。
     * @param lan 局域网。
     * @return 如果相连，返回true，否则返回false。
     */
    public boolean isConnect(Lan lan)
    {
        for (Map.Entry<Lan, Boolean> kvp : connected.entrySet())
        {
            if (kvp.getKey().compareTo(lan) == 0)
                return kvp.getValue();
        }

        return false;
    }

    /**
     * 更新自己的配置信息。
     */
    public void updateSelfMsg()
    {
        // 取出配置消息集合中的每一条配置消息，进行处理。
        for (Map.Entry messageSet : messageQueue.entrySet())
        {
            // 取出配置消息。
            ConfigurationMessage message = (ConfigurationMessage) messageSet.getKey();
            // 取出网桥将发送配置消息给的局域网。
            Lan lan = (Lan) messageSet.getValue();

            // 如果这个配置消息中认为的根网桥的编号比这个网桥认为的根网桥编号小。
            if (message.rootId < this.rootBridgeId)
            {
                this.rootBridgeId = message.rootId;
                // 这个网桥到根网桥的距离增加1。
                this.distanceToRootBridge = message.distanceToRoot + 1;
                // 将走向根网桥的下一跳设置为配置消息的发送网桥。
                nextHopToRoot = message.sendMegBridgeId;
            }

            // 如果根网桥与配置消息传过来的一致，但是下一跳不一致：取到根网桥跳数小的。
            else if ((message.rootId == this.rootBridgeId) && (nextHopToRoot != message.sendMegBridgeId))
            {
                // 如果配置消息中到根网桥的距离比这个网桥到跟网桥的距离小。
                if (message.distanceToRoot < this.distanceToRootBridge)
                {
                    // 更新这个网桥到根网桥的距离
                    this.distanceToRootBridge = message.distanceToRoot + 1;
                    // 断开这个网桥与局域网的连接。
                    connected.put(lan, false);
                }
                else if (message.distanceToRoot == this.distanceToRootBridge)
                {
                    if (this.bridgeId > message.sendMegBridgeId)
                        connected.put(lan, false);
                }
            }
        }

        // 当配置消息全部发送完毕，清空配置消息。
        messageQueue.clear();

        // 重新建立与局域网的连接。
        LinkedList<Lan> connectedLan = new LinkedList<>();
        // 取出配置信息中相连的信息。
        for (Map.Entry connection : connected.entrySet())
        {
            // 如果网桥与局域网相连。
            if (connection.getValue().equals(true))
                // 将局域网添加到与网桥相连的局域网链表。
                connectedLan.addLast((Lan) connection.getKey());
        }
        // 如果一个网桥只连着一个局域网，那么将另一个接口断开。
        if (connectedLan.size() == 1)
            connected.put(connectedLan.getFirst(), false);
    }

    /**
     * 向与自己相连的局域网发送配置消息。
     */
    private void sendMessage()
    {
        // 获取配置消息。
        ConfigurationMessage message = new ConfigurationMessage(this.bridgeId, this.rootBridgeId, this.distanceToRootBridge);
        // 获取每个局域网。

        Set<Lan> lans = connected.keySet();
        for (Lan lan : lans)
        {
            // 如果网桥与局域网相连，将局域网的配置消息添加上这条配置消息。
            if (isConnect(lan))
                lan.messageQueue.add(message);
        }
    }

    /**
     * 整体更新。
     */
    public void update()
    {
        // 更新自身的配置消息。
        updateSelfMsg();
        // 发送配置消息给相连的局域网。
        sendMessage();
    }

    /**
     * 如果这个网桥是根网桥，重置这个网桥的配置消息。
     */
    public void clearMessage()
    {
        this.rootBridgeId = this.bridgeId;
        this.distanceToRootBridge = 0;
        LinkedList<Lan> connectedLans = new LinkedList<>();
        for (Lan lan : connectedLans)
            connected.put(lan, true);
    }

    /**
     * 显示根网桥及网桥到根网桥的距离。
     *
     * @return
     */
    @Override
    public String toString()
    {
        return "The root id for Bridge " + this.bridgeId + " is " + this.rootBridgeId + ", distance to root = " + this.distanceToRootBridge;
    }

    /**
     * 显示互连信息。
     *
     * @return
     */
    public String connectionInformation()
    {
        StringBuilder connectionInformation = new StringBuilder();
        for (Lan lan : connected.keySet())
        {
            connectionInformation.append("Lan " + lan.getLanId() + " is connected with bridge " + this.bridgeId + "\n");
        }
        return connectionInformation.toString();
    }


    public int getBridgeId()
    {
        return bridgeId;
    }

    public int getRootBridgeId()
    {
        return rootBridgeId;
    }

    public void setRootBridgeId(int rootBridgeId)
    {
        this.rootBridgeId = rootBridgeId;
    }

    @Override
    public void run()
    {

    }
}
