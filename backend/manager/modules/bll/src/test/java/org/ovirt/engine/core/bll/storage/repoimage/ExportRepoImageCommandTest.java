package org.ovirt.engine.core.bll.storage.repoimage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.VmDao;

/** A test case for {@link ExportRepoImageCommand} */
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExportRepoImageCommandTest extends ImportExportRepoImageCommandTest {

    @Mock
    private VmDao vmDao;

    @Mock
    private DiskDao diskDao;

    @Mock
    private Instance<DiskValidator> diskValidatorInstance;

    @Mock
    private DiskValidator diskValidator;

    @Mock
    private DiskLunMapDao diskLunMapDao;

    @Mock
    private Instance<DiskImagesValidator> diskImagesValidatorInstance;

    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Mock
    private Instance<StorageDomainValidator> storageDomainValidatorInstance;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @InjectMocks
    protected ExportRepoImageCommand<ExportRepoImageParameters> cmd =
            new ExportRepoImageCommand<>(new ExportRepoImageParameters(diskImageGroupId, repoStorageDomainId), null);

    protected VM vm;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        vm = new VM();
        vm.setStatus(VMStatus.Down);

        when(vmDao.getVmsListForDisk(diskImageId, Boolean.FALSE)).thenReturn(Collections.singletonList(vm));
        when(diskValidatorInstance.get()).thenReturn(diskValidator);
        when(diskValidator.init(any())).thenReturn(diskValidator);
        when(diskImagesValidatorInstance.get()).thenReturn(diskImagesValidator);
        when(diskImagesValidator.init(any())).thenReturn(diskImagesValidator);
        when(diskImagesValidator.diskImagesNotIllegal()).thenReturn(ValidationResult.VALID);
        when(diskImagesValidator.diskImagesNotLocked()).thenReturn(ValidationResult.VALID);
        when(storageDomainValidatorInstance.get()).thenReturn(storageDomainValidator);
        when(storageDomainValidator.createInstance(any())).thenAnswer(invocation ->
                new StorageDomainValidator(invocation.getArgument(0)));

        when(diskDao.get(diskImageGroupId)).thenReturn(diskImage);
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateLunDisk() {
        LunDisk disk = new LunDisk();
        when(diskDao.get(diskImageGroupId)).thenReturn(disk);
        spyDiskValidator(disk);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    private DiskValidator spyDiskValidator(Disk disk) {
        DiskValidator realDiskValidator = spy(new DiskValidator(disk));
        doReturn(diskLunMapDao).when(realDiskValidator).getDiskLunMapDao();
        when(diskValidatorInstance.get()).thenReturn(realDiskValidator);
        return realDiskValidator;
    }

    @Test
    public void testValidateImageDoesNotExist() {
        when(diskDao.get(diskImageGroupId)).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testValidateDomainInMaintenance() {
        diskStorageDomain.setStatus(StorageDomainStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testValidateImageHasParent() {
        diskImage.setParentId(Guid.newGuid());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
    }

    @Test
    public void testValidateVmRunning() {
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_RUNNING);
    }

    @Test
    public void testValidateImageLocked() {
        diskImage.setImageStatus(ImageStatus.LOCKED);
        when(diskImagesValidator.diskImagesNotLocked())
            .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED));
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }
}
