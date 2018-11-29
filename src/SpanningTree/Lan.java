package SpanningTree;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Lan implements Runnable, Comparable<Lan>
{
    // 局域网的编号。
    public int lanId;

    // 与局域网相连的网桥。
    public LinkedList<Bridge> connectedBridge;

    // 局域网的配置消息。
    public ConcurrentLinkedDeque<ConfigurationMessage> messageQueue;

    /**
     * 局域网的构造方法。
     * @param lanId 局域网的编号。
     */
    public Lan(int lanId)
    {
        this.lanId = lanId;
        connectedBridge = new LinkedList<>();
        this.messageQueue = new ConcurrentLinkedDeque<>();
    }

    /**
     * 增加与局域网相连的网桥。
     * @param bridge 与这个局域网相连的网桥。
     */
    public void connectBridge(Bridge bridge)
    {
        // 如果网桥与局域网相连，在链表中添加这个网桥。
        connectedBridge.addLast(bridge);
    }

    /**
     * 局域网将自己的配置消息传递给下一个与之相连的网桥。
     */
    public void transmit()
    {
        // 如果配置消息不为空，循环取出配置信息，发送给下一个网桥。
        while (!messageQueue.isEmpty())
        {
            // 将配置消息依次取出。
            ConfigurationMessage message = messageQueue.removeFirst();
            // 获取上一个发送给它消息的网桥的编号。
            int senderId = message.sendMegBridgeId;
            // 依次获取与局域网相连接的网桥。
            for (Bridge bridge : connectedBridge)
            {
                // 如果这个网桥不是上一个发送配置消息的网桥。
                if (bridge.bridgeId != senderId)
                {
                    // 在这个网桥上添加配置消息。
                    bridge.messageQueue.put(message, this);
                }
            }
        }
        // 当配置消息发送完毕之后清空配置消息列表，避免陷入死循环。
        messageQueue.clear();
    }

    /**
     * 将这个局域网的相关信息显示出来。
     * @return 返回局域网信息。
     */
    public String toString()
    {
        if (connectedBridge.size() == 0)
            return "Single link";

        LinkedList<Integer> connectedBridges = new LinkedList<>();
        for (Bridge bridge : connectedBridge)
        {
            if (bridge.isConnect(this))
                connectedBridges.addLast(bridge.bridgeId);
        }
        int designateBridgeId = connectedBridges.getFirst();
        for (int i = 1; i < connectedBridges.size(); i++)
        {
            if (designateBridgeId > connectedBridges.get(i))
                designateBridgeId = connectedBridges.get(i);
        }

        return "The designate bridge for lan " + this.lanId + " is bridge " + designateBridgeId;
    }

    public int getLanId()
    {
        return lanId;
    }

    public void setLanId(int lanId)
    {
        this.lanId = lanId;
    }

    public ConcurrentLinkedDeque<ConfigurationMessage> getMessageQueue()
    {
        return messageQueue;
    }

    public void setMessageQueue(ConcurrentLinkedDeque<ConfigurationMessage> messageQueue)
    {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run()
    {

    }

    @Override
    public int compareTo(Lan o)
    {
        if ((this == o) || (this.lanId == o.lanId))
            return 0;

        return 1;
    }
}
