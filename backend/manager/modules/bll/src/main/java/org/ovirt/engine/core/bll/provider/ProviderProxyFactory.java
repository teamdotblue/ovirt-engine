package org.ovirt.engine.core.bll.provider;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.host.provider.foreman.ForemanHostProviderProxy;
import org.ovirt.engine.core.bll.provider.cluster.KubevirtProviderProxy;
import org.ovirt.engine.core.bll.provider.network.UnmanagedNetworkProviderProxy;
import org.ovirt.engine.core.bll.provider.network.openstack.ExternalNetworkProviderProxy;
import org.ovirt.engine.core.bll.provider.network.openstack.OpenstackNetworkProviderProxy;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.bll.provider.vms.KVMVmProviderProxy;
import org.ovirt.engine.core.bll.provider.vms.VmwareVmProviderProxy;
import org.ovirt.engine.core.bll.provider.vms.XENVmProviderProxy;
import org.ovirt.engine.core.common.businessentities.KVMVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.XENVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;

/**
 * The provider proxy factory can create a provider proxy according to the provider definition.
 */
@Singleton
public class ProviderProxyFactory {

    @Inject
    private Instance<UnmanagedNetworkProviderProxy> unmanagedNetworkProviderProxy;
    @Inject
    private Instance<ExternalNetworkProviderProxy> externalNetworkProviderProxy;
    @Inject
    private Instance<VmwareVmProviderProxy> vmwareVmProviderProxy;
    @Inject
    private Instance<KVMVmProviderProxy> kvmVmProviderProxy;
    @Inject
    private Instance<XENVmProviderProxy> xenVmProviderProxy;
    @Inject
    private Instance<KubevirtProviderProxy> kubevirtProviderProxy;
    @Inject
    private Instance<OpenstackNetworkProviderProxy> openstackNetworkProviderProxy;
    @Inject
    private Instance<OpenStackVolumeProviderProxy> openStackVolumeProviderProxy;
    @Inject
    private Instance<OpenStackImageProviderProxy> openStackImageProviderProxy;
    @Inject
    private Instance<ForemanHostProviderProxy> foremanHostProviderProxy;

    /**
     * Create the proxy used to communicate with the given provider.
     *
     * @param provider
     *            The provider to create the proxy for.
     * @return The proxy for communicating with the provider
     */
    @SuppressWarnings("unchecked")
    public <P extends ProviderProxy<?>> P create(Provider<?> provider) {
        switch (provider.getType()) {
            case EXTERNAL_NETWORK:
                if (provider.getIsUnmanaged()) {
                    return (P) unmanagedNetworkProviderProxy.get().init((Provider<OpenstackNetworkProviderProperties>) provider);
                }
                return (P) externalNetworkProviderProxy.get().init((Provider<OpenstackNetworkProviderProperties>) provider);

            case FOREMAN:
                return (P) foremanHostProviderProxy.get().init(provider);

            case OPENSTACK_NETWORK:
                return (P) openstackNetworkProviderProxy.get().init((Provider<OpenstackNetworkProviderProperties>) provider);

            case OPENSTACK_IMAGE:
                return (P) openStackImageProviderProxy.get().init((Provider<OpenStackImageProviderProperties>) provider);

            case OPENSTACK_VOLUME:
                return (P) openStackVolumeProviderProxy.get().init((Provider<OpenStackVolumeProviderProperties>) provider);

            case VMWARE:
                return (P) vmwareVmProviderProxy.get().init((Provider<VmwareVmProviderProperties>) provider);

            case KVM:
                return (P) kvmVmProviderProxy.get().init((Provider<KVMVmProviderProperties>) provider);

            case XEN:
                return (P) xenVmProviderProxy.get().init((Provider<XENVmProviderProperties>) provider);

            case KUBEVIRT:
                return (P) kubevirtProviderProxy.get().init((Provider<KubevirtProviderProperties>) provider);

            default:
                return null;
        }
    }
}
