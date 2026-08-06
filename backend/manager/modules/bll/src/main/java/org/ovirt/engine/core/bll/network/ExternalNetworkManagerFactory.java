package org.ovirt.engine.core.bll.network;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;

@Singleton
public class ExternalNetworkManagerFactory {

    @Inject
    private Instance<ExternalNetworkManager> externalNetworkManagerInstance;

    /**
     * Create a manager for the specific vNIC.
     *
     * @param nic
     *            The vNIC to create a manager for.
     */
    public ExternalNetworkManager create(VmNic nic) {
        return externalNetworkManagerInstance.get().init(nic);
    }

    /**
     * Create a manager for the specific vNIC with the given network.
     *
     * @param nic
     *            The vNIC to create a manager for.
     * @param network
     *            The network to manage.
     */
    public ExternalNetworkManager create(VmNic nic, Network network) {
        return externalNetworkManagerInstance.get().init(nic, network);
    }
}
