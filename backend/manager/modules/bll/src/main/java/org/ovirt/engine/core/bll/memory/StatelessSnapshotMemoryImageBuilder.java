package org.ovirt.engine.core.bll.memory;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * This builder is responsible to create the memory volumes for stateless snapshot -
 * it just take the memory volume of the previously active snapshot
 */
public class StatelessSnapshotMemoryImageBuilder implements MemoryImageBuilder {

    @Inject
    private SnapshotDao snapshotDao;

    private Guid vmId;

    private Snapshot activeSnapshot;

    public StatelessSnapshotMemoryImageBuilder(VM vm) {
        this.vmId = vm.getId();
    }

    public StatelessSnapshotMemoryImageBuilder() {
    }

    public StatelessSnapshotMemoryImageBuilder init(VM vm) {
        this.vmId = vm.getId();
        return this;
    }

    @Override
    public void build() {
        //no op
    }

    @Override
    public Guid getMemoryDiskId() {
        activeSnapshot = snapshotDao.get(vmId, SnapshotType.ACTIVE);
        return activeSnapshot.getMemoryDiskId();
    }

    @Override
    public Guid getMetadataDiskId() {
        activeSnapshot = snapshotDao.get(vmId, SnapshotType.ACTIVE);
        return activeSnapshot.getMetadataDiskId();
    }

    @Override
    public boolean isCreateTasks() {
        return false;
    }
}
