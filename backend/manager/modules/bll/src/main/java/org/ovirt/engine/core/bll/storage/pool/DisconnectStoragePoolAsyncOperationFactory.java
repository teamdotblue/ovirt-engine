package org.ovirt.engine.core.bll.storage.pool;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class DisconnectStoragePoolAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {

    @Inject
    private Instance<DisconnectStoragePoolAsyncOperation> disconnectStoragePoolAsyncOperation;

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return disconnectStoragePoolAsyncOperation.get().createInstance(getVdss(), getStoragePool());
    }
}
