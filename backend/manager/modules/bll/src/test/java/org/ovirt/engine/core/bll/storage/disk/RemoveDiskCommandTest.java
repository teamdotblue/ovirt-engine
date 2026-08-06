package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;

/** A test case for {@link RemoveDiskCommandTest} */
@MockitoSettings(strictness = Strictness.LENIENT)
public class RemoveDiskCommandTest extends BaseCommandTest {
    @Mock
    private VmDao vmDao;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private VmStaticDao vmStaticDao;

    @Mock
    private Instance<DiskValidator> diskValidatorInstance;

    @Mock
    private DiskValidator diskValidator;

    @Mock
    private Instance<StorageDomainValidator> storageDomainValidatorInstance;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    private Instance<DiskImagesValidator> diskImagesValidatorInstance;

    @Mock
    private DiskImagesValidator diskImagesValidator;

    private Guid diskId = Guid.newGuid();
    private Disk disk;
    private VM vm;

    @Spy
    @InjectMocks
    private RemoveDiskCommand<RemoveDiskParameters> cmd =
            new RemoveDiskCommand<>(new RemoveDiskParameters(diskId), null);

    @BeforeEach
    public void setUp() {
        disk = new DiskImage();
        setupDisk();

        Guid vmId = Guid.newGuid();
        vm = new VM();
        vm.setId(vmId);

        VmDeviceId vmDeviceId = new VmDeviceId(diskId, vmId);
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(vmDeviceId);
        vmDevice.setPlugged(true);

        when(vmDao.getVmsListForDisk(diskId, Boolean.TRUE)).thenReturn(Collections.singletonList(vm));
        when(vmDeviceDao.get(vmDeviceId)).thenReturn(vmDevice);

        doReturn(disk).when(cmd).getDisk();
        doReturn(ActionType.RemoveDisk).when(cmd).getActionType();
        when(diskValidatorInstance.get()).thenReturn(diskValidator);
        when(diskValidator.init(any())).thenReturn(diskValidator);
        when(diskImagesValidatorInstance.get()).thenReturn(diskImagesValidator);
        when(diskImagesValidator.init(any())).thenReturn(diskImagesValidator);
        doReturn(storageDomainValidator).when(storageDomainValidatorInstance).get();
        doReturn(storageDomainValidator).when(storageDomainValidator).createInstance(any());
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainExistAndActive();
    }

    protected void setupDisk() {
        disk.setId(diskId);
        disk.setVmEntityType(VmEntityType.VM);
        if (disk instanceof DiskImage) {
            ((DiskImage) disk).setStorageIds(new ArrayList<>(Collections.singletonList(Guid.newGuid())));
        }
    }

    /* Tests for validate() flow */

    @Test
    public void testValidateFlowImageDoesNotExist() {
        doReturn(null).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
    }

    @Test
    public void testValidateVmUp() {
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }


    @Test
    public void testValidateTemplateEntity() {
        disk.setVmEntityType(VmEntityType.TEMPLATE);

        StorageDomain domain = new StorageDomain();
        domain.setId(Guid.newGuid());
        domain.setStatus(StorageDomainStatus.Active);
        cmd.getParameters().setStorageDomainId(domain.getId());

        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(domain.getId());
        storageIds.add(Guid.newGuid());
        ((DiskImage) disk).setStorageIds(storageIds);

        doReturn(domain).when(cmd).getStorageDomain();
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(true).when(cmd).checkDerivedDisksFromDiskNotExist(any());
        doReturn(disk).when(diskImageDao).get(any());

        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateTemplateWithNoDomain() {
        disk.setVmEntityType(VmEntityType.TEMPLATE);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_CANT_DELETE_TEMPLATE_DISK_WITHOUT_SPECIFYING_DOMAIN);
    }

    @Test
    public void testValidateOvfDiskNotIllegal() {
        ((DiskImage) disk).setImageStatus(ImageStatus.OK);
        disk.setContentType(DiskContentType.OVF_STORE);
        when(diskImagesValidator.disksInStatus(ImageStatus.ILLEGAL,
            EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS))
            .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS));
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS);
    }

    @Test
    public void testPreImportedHostedEngineLunDiskRemove() {
        disk = new LunDisk();
        setupDisk();
        disk.setDiskAlias(StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS);
        doReturn(disk).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testImportedHostedEngineLunDiskRemove() {
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vm.setStatus(VMStatus.Up);
        disk = new LunDisk();
        setupDisk();
        disk.setDiskAlias(StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS);
        doReturn(disk).when(cmd).getDisk();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testImportedHostedEngineImageDiskRemove() {
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        vm.setStatus(VMStatus.Up);
        doReturn(disk).when(cmd).getDisk();
        spyDiskValidator(disk);
        ValidateTestUtils.runAndAssertValidateFailure(
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_DISK);
    }

    private DiskValidator spyDiskValidator(Disk disk) {
        DiskValidator realDiskValidator = spy(new DiskValidator(disk));
        when(diskValidatorInstance.get()).thenReturn(realDiskValidator);
        return realDiskValidator;
    }

    @Test
    public void testRemoveIsoDiskAttachedToVmFails() {
        Disk disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setContentType(DiskContentType.ISO);
        doReturn(disk).when(cmd).getDisk();
        doReturn(true).when(cmd).canRemoveDiskBasedOnImageStorageCheck();
        doReturn(Collections.singletonList("NiceVm")).when(vmStaticDao).getAllNamesWithSpecificIsoAttached(disk.getId());
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ERROR_CANNOT_REMOVE_ISO_DISK_ATTACHED_TO_VMS);
    }

    @Test
    public void testRemoveHostedEngineDiskFailsByAlias() {
        // No need to test all available aliases as the validation is done on the entire list using contains()
        disk.setDiskAlias(StorageConstants.HOSTED_ENGINE_DISKS_ALIASES.get(0));
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_IS_A_HOSTED_ENGINE_DISK);
    }
}
