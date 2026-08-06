package org.ovirt.engine.core.bll.storage.pool;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class RefreshStoragePoolAndDisconnectAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {

    @Inject
    private Instance<RefreshStoragePoolAndDisconnectAsyncOperation> refreshStoragePoolAndDisconnectAsyncOperation;

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return refreshStoragePoolAndDisconnectAsyncOperation.get().createInstance(getVdss(), getStorageDomain(), getStoragePool());
    }
}
