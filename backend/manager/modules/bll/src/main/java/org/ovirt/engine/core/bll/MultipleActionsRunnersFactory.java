package org.ovirt.engine.core.bll;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.AttachStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.bll.storage.domain.DeactivateStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;

@Singleton
public class MultipleActionsRunnersFactory {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private ClusterDao clusterDao;
    @Inject
    private Instance<DeactivateStorageDomainsMultipleActionRunner> deactivateStorageDomainsMultipleActionRunnerProvider;
    @Inject
    private Instance<RunVMActionRunner> runVMActionRunnerProvider;
    @Inject
    private Instance<MigrateVMActionRunner> migrateVMActionRunnerProvider;
    @Inject
    private Instance<AttachStorageDomainsMultipleActionRunner> attachStorageDomainsMultipleActionRunnerProvider;
    @Inject
    private Instance<RemoveVmFromPoolRunner> removeVmFromPoolRunnerProvider;
    @Inject
    private Instance<GlusterMultipleActionsRunner> glusterMultipleActionsRunnerProvider;
    @Inject
    private Instance<DefaultPrevalidatingMultipleActionsRunner> prevalidatingMultipleActionsRunnerProvider;
    @Inject
    private Instance<ParallelMultipleActionsRunner> parallelMultipleActionsRunnerProvider;
    @Inject
    private Instance<SequentialMultipleActionsRunner> sequentialMultipleActionsRunnerProvider;

    public MultipleActionsRunner createMultipleActionsRunner(ActionType actionType,
                                                             List<ActionParametersBase> parameters,
                                                             boolean isInternal, CommandContext commandContext) {
        MultipleActionsRunner runner;
        switch (actionType) {
            case DeactivateStorageDomainWithOvfUpdate:
                runner = deactivateStorageDomainsMultipleActionRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            case AttachStorageDomainToPool:
                runner = attachStorageDomainsMultipleActionRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            case RunVm:
                runner = runVMActionRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            case MigrateVm:
                runner = migrateVMActionRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            case RemoveVmFromPool:
                runner = removeVmFromPoolRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            case StartGlusterVolume:
            case StopGlusterVolume:
            case DeleteGlusterVolume:
            case SetGlusterVolumeOption:
            case ResetGlusterVolumeOptions:
            case AddVds: // AddVds is called with multiple actions *only* in case of gluster clusters
            case RemoveGlusterServer:
            case EnableGlusterHook:
            case DisableGlusterHook:
            case DeleteGlusterVolumeSnapshot:
                runner = glusterMultipleActionsRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            case RemoveVds:
                if (containsGlusterServer(parameters)) {
                    runner = glusterMultipleActionsRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                } else {
                    runner = prevalidatingMultipleActionsRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                }

                break;
            case PersistentHostSetupNetworks:
            case SyncAllHostNetworks:
                runner = parallelMultipleActionsRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            case AttachNetworkToCluster:
            case DetachNetworkToCluster:
            case UpdateNetworkOnCluster:
                throw new UnsupportedOperationException("Multiple network attachments/detachments/updates should be run through ManageNetworkClustersCommand!");

            case AddNetworkAttachment:
            case UpdateNetworkAttachment:
            case RemoveNetworkAttachment:
                throw new UnsupportedOperationException("AddNetworkAttachment, UpdateNetworkAttachment, and RemoveNetworkAttachment cannot be run using MultipleActionsRunner");
            case RemoveDiskProfile:
            case RemoveCpuProfile:
            case RemoveNetwork:
                runner = sequentialMultipleActionsRunnerProvider.get().createInstance(actionType, parameters, commandContext, isInternal);
                break;
            default:
                runner = prevalidatingMultipleActionsRunnerProvider.get().init(actionType, parameters, commandContext, isInternal);
                break;
        }
        return runner;
    }

    private boolean containsGlusterServer(List<ActionParametersBase> parameters) {
        Set<Guid> processed = new HashSet<>();
        for (ActionParametersBase param : parameters) {
            VDS vds = vdsDao.get(((RemoveVdsParameters) param).getVdsId());
            if (vds != null && !processed.contains(vds.getClusterId())) {
                Cluster cluster = clusterDao.get(vds.getClusterId());
                if (cluster.supportsGlusterService()) {
                    return true;
                }
                processed.add(vds.getClusterId());
            }
        }

        return false;
    }
}
