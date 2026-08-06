package org.ovirt.engine.core.bll.validator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.DiskImageDao;

public class MultipleVmsValidator {

    private DiskImageDao diskImageDao;

    private Iterable<VM> vms;

    public MultipleVmsValidator(VM vm, DiskImageDao diskImageDao) {
        this.diskImageDao = diskImageDao;
        this.vms = Collections.singletonList(vm);
    }

    public MultipleVmsValidator(Iterable<VM> vms, DiskImageDao diskImageDao) {
        this.diskImageDao = diskImageDao;
        this.vms = vms;
    }

    /**
     * @return ValidationResult indicating whether there are plugged disk snapshots
     */
    public ValidationResult vmNotHavingPluggedDiskSnapshots(EngineMessage message) {
        List<String> vmPluggedDiskSnapshotsInfo = null;
        for (VM vm : vms) {
            List<DiskImage> pluggedDiskSnapshots =
                    diskImageDao.getAttachedDiskSnapshotsToVm(vm.getId(), Boolean.TRUE);
            if (!pluggedDiskSnapshots.isEmpty()) {
                if (vmPluggedDiskSnapshotsInfo == null) {
                    vmPluggedDiskSnapshotsInfo = new LinkedList<>();
                }
                List<String> pluggedDiskSnapshotAliases = new LinkedList<>();
                for (BaseDisk disk : pluggedDiskSnapshots) {
                    pluggedDiskSnapshotAliases.add(disk.getDiskAlias());
                }
                vmPluggedDiskSnapshotsInfo.add(
                        String.format("%s / %s",
                                vm.getName(),
                                StringUtils.join(pluggedDiskSnapshotAliases, ",")));
            }
        }

        if (vmPluggedDiskSnapshotsInfo != null) {
            return new ValidationResult(message,
                    String.format("$disksInfo %s",
                            String.format(StringUtils.join(vmPluggedDiskSnapshotsInfo, "%n"))));
        }

        return ValidationResult.VALID;
    }
}
