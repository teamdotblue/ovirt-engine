package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.HasStoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HasStoragePoolValidatorTest {
    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private Instance<StoragePoolValidator> storagePoolValidatorInstance;

    private HasStoragePool hsp = new Cluster();
    @InjectMocks
    private HasStoragePoolValidator validator = new HasStoragePoolValidator(hsp);

    @BeforeEach
    public void setUp() {
        doReturn(new StoragePoolValidator()).when(storagePoolValidatorInstance).get();
    }

    @Test
    public void dataCenterExists() {
        Guid id = Guid.newGuid();
        hsp.setStoragePoolId(id);
        when(storagePoolDao.get(id)).thenReturn(mock(StoragePool.class));
        assertThat(validator.storagePoolExists(), isValid());
    }

    @Test
    public void dataCenterNotExistsWhenClusterIsOrphan() {
        assertThat(validator.storagePoolExists(), isValid());
    }

    @Test
    public void dataCenterDoesNotExist() {
        hsp.setStoragePoolId(Guid.newGuid());
        assertThat(validator.storagePoolExists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST));
    }
}
