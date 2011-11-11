package kg.apc.jmeter.perfmon;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * @author undera
 */
public class OldAgentConnector implements PerfMonAgentConnector {

    private static final Logger log = LoggingManager.getLoggerForClass();
    AgentConnector connector;

    public OldAgentConnector(String host, int port) {
        connector = new AgentConnector(host, port);
    }

    public void setMetricType(String metric) {
        connector.setMetricType(metric);
    }

    public void setParams(String params) {
        log.debug("Old agent don't support params: " + params);
    }

    public void connect() throws IOException {
        Socket sock = createSocket(connector.getHost(), connector.getPort());
        connector.connect(sock);
    }

    public void disconnect() {
        connector.disconnect();
    }

    protected Socket createSocket(String host, int port) throws UnknownHostException, IOException {
        return new Socket(host, port);
    }

    // TODO: cache it to be efficient
    public String getLabel(boolean translate) {
        String hostName;
        if (translate) {
            hostName = connector.getRemoteServerName();
        } else {
            hostName = connector.getHost();
        }
        return hostName + " - " + AgentConnector.metrics.get(connector.getMetricType());
    }

    public void generateSamples(PerfMonSampleGenerator collector) throws IOException {
        String label = null;
        switch (connector.getMetricType()) {
            case AgentConnector.PERFMON_CPU:
                collector.generateSample(100 * connector.getCpu(), label + ", %");
                break;
            case AgentConnector.PERFMON_MEM:
                collector.generateSample((double) connector.getMem() / PerfMonCollector.MEGABYTE, label + ", MB");
                break;
            case AgentConnector.PERFMON_SWAP:
                collector.generate2Samples(connector.getSwap(), label + " page in", label + " page out");
                break;
            case AgentConnector.PERFMON_DISKS_IO:
                collector.generate2Samples(connector.getDisksIO(), label + " reads", label + " writes");
                break;
            case AgentConnector.PERFMON_NETWORKS_IO:
                collector.generate2Samples(connector.getNetIO(), label + " recv, KB", label + " sent, KB", 1024d);
                break;
            default:
                throw new IOException("Unknown metric index: " + connector.getMetricType());
        }
    }
}
