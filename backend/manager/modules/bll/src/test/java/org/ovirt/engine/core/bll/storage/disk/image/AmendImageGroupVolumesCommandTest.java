package org.ovirt.engine.core.bll.storage.disk.image;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.AmendImageGroupVolumesCommandParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AmendImageGroupVolumesCommandTest extends BaseCommandTest {

    @Mock
    private DiskDao diskDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private DiskValidator diskValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    private Instance<StorageDomainValidator> storageDomainValidatorInstance;

    @Mock
    private Instance<DiskImagesValidator> diskImagesValidatorInstance;

    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Mock
    private Instance<StoragePoolValidator> storagePoolValidatorInstance;

    private Guid diskId = Guid.newGuid();
    private Guid storagePoolId = Guid.newGuid();
    private Guid storageDomainId = Guid.newGuid();
    private StoragePool storagePool;
    private StorageDomain storageDomain;
    private DiskImage diskImage;

    @Spy
    @InjectMocks
    private AmendImageGroupVolumesCommand<AmendImageGroupVolumesCommandParameters> command =
            new AmendImageGroupVolumesCommand<>(createParameters(), CommandContext.createContext(""));

    @BeforeEach
    public void setup() {
        doReturn(new StoragePoolValidator()).when(storagePoolValidatorInstance).get();
        diskImage = new DiskImage();
        diskImage.setStoragePoolId(storagePoolId);
        diskImage.setVmEntityType(VmEntityType.VM);
        diskImage.setId(diskId);
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(storageDomainId);
        diskImage.setStorageIds(storageIds);
        doReturn(diskImage).when(diskDao).get(diskId);
        doReturn(diskValidator).when(command).createDiskValidator();
        doReturn(storageDomainValidator).when(storageDomainValidatorInstance).get();
        when(storageDomainValidator.createInstance(any())).thenAnswer(invocation ->
            new StorageDomainValidator(invocation.getArgument(0)));
        when(diskImagesValidatorInstance.get()).thenReturn(diskImagesValidator);
        when(diskImagesValidator.init(any())).thenReturn(diskImagesValidator);
        when(diskImagesValidator.diskImagesNotIllegal()).thenAnswer(invocation ->
            diskImage.getImageStatus() == ImageStatus.ILLEGAL
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL)
                : ValidationResult.VALID);
        when(diskImagesValidator.diskImagesNotLocked()).thenAnswer(invocation ->
            diskImage.getImageStatus() == ImageStatus.LOCKED
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED)
                : ValidationResult.VALID);
        mockRunningStoragePool();
        mockRunningStorageDomain();
    }

    @Test
    public void testValidationPasses() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidationFailsDiskNotExists() {
        when(diskValidator.isDiskExists())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST));
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testValidationFailsDiskConnectedToRunningVm() {
        when(diskValidator.isDiskPluggedToAnyNonDownVm(false))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void testValidationFailsStoragePoolNotExists() {
        doReturn(null).when(storagePoolDao).get(storagePoolId);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidationFailsStoragePoolNotUp() {
        storagePool.setStatus(StoragePoolStatus.NotOperational);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void testValidationFailsStorageDomainNotExists() {
        doReturn(null).when(storageDomainDao).getForStoragePool(storageDomainId, storagePoolId);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testValidationFailsStorageDomainNotUp() {
        storageDomain.setStatus(StorageDomainStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testValidationFailsForTemplateDisk() {
        diskImage.setVmEntityType(VmEntityType.TEMPLATE);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_CANT_AMEND_TEMPLATE_DISK);
    }

    @Test
    public void testValidationFailsForIllegalDisk() {
        diskImage.setImageStatus(ImageStatus.ILLEGAL);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    @Test
    public void testValidationFailsForLockedDisk() {
        diskImage.setImageStatus(ImageStatus.LOCKED);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    private void mockRunningStoragePool() {
        storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        storagePool.setId(storagePoolId);
        storagePool.setCompatibilityVersion(Version.v4_2);
        doReturn(storagePool).when(storagePoolDao).get(storagePool.getId());
    }

    private void mockRunningStorageDomain() {
        storageDomain = new StorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Active);
        doReturn(storageDomain).when(storageDomainDao).getForStoragePool(storageDomainId, storagePoolId);
    }

    private AmendImageGroupVolumesCommandParameters createParameters() {
        return new AmendImageGroupVolumesCommandParameters(diskId, QcowCompat.QCOW2_V2);
    }
}
