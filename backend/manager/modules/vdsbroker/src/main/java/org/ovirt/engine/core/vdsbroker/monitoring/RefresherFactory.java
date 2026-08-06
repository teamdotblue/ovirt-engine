package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.KubevirtHostConnectionRefresher;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.KubevirtVmStatsRefresher;

@Singleton
public class RefresherFactory {

    @Inject
    private Instance<KubevirtHostConnectionRefresher> kubevirtHostConnectionRefresherInstance;
    @Inject
    private Instance<KubevirtVmStatsRefresher> kubevirtVmStatsRefresherInstance;
    @Inject
    private Instance<EventVmStatsRefresher> eventVmStatsRefresherInstance;

    public VmStatsRefresher createVmStatsRefresher(VdsManager vdsManager, ResourceManager resourceManager) {
        return getVmStatsRefresher(vdsManager, resourceManager);
    }

    public HostConnectionRefresherInterface createHostConnectionRefresher(VdsManager vdsManager,
            ResourceManager resourceManager) {
        switch (vdsManager.getVdsType()) {
            case KubevirtNode:
                return kubevirtHostConnectionRefresherInstance.get().createInstance(vdsManager);
            default:
                return new HostConnectionRefresher(vdsManager, resourceManager);
        }
    }

    private VmStatsRefresher getVmStatsRefresher(VdsManager vdsManager, ResourceManager resourceManager) {
        switch (vdsManager.getVdsType()) {
            case KubevirtNode:
                return kubevirtVmStatsRefresherInstance.get().createInstance(vdsManager);
            default:
                return eventVmStatsRefresherInstance.get().createInstance(vdsManager, resourceManager);
        }
    }
}
