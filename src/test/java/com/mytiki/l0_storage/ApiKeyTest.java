/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_storage;

import com.mytiki.l0_storage.features.latest.api_id.ApiIdAORsp;
import com.mytiki.l0_storage.features.latest.api_id.ApiIdDO;
import com.mytiki.l0_storage.features.latest.api_id.ApiIdRepository;
import com.mytiki.l0_storage.features.latest.api_id.ApiIdService;
import com.mytiki.l0_storage.main.l0StorageApp;
import com.mytiki.spring_rest_api.exception.ApiException;
import com.mytiki.spring_rest_api.reply.ApiReplyAO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {l0StorageApp.class}
)
@ActiveProfiles(profiles = {"test", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiKeyTest {

    @Autowired
    private ApiIdService service;

    @Autowired
    private ApiIdRepository repository;

    @Test
    public void Test_Register_Success() {
        ApiIdAORsp rsp = service.register("test");
        Optional<ApiIdDO> found = repository.findById(UUID.fromString(rsp.getApiId()));

        assertTrue(found.isPresent());
        assertEquals(rsp.getApiId(), found.get().getApiId().toString());
        assertEquals(rsp.getValid(), found.get().getValid());
        assertNotNull(found.get().getCreated());
        assertNotNull(found.get().getModified());
        assertNotNull(rsp.getCreated());
        assertNotNull(rsp.getModified());
    }

    @Test
    public void Test_Revoke_Success() {
        ApiIdAORsp register = service.register("test");
        ApiIdAORsp rsp = service.revoke(register.getApiId());
        Optional<ApiIdDO> found = repository.findById(UUID.fromString(rsp.getApiId()));

        assertFalse(rsp.getValid());
        assertTrue(found.isPresent());
        assertFalse(found.get().getValid());
    }

    @Test
    public void Test_Revoke_NotFound() {
        ApiException ex = assertThrows(ApiException.class, () -> {
            service.revoke(UUID.randomUUID().toString());
        });
        assertEquals(ex.getCode(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void Test_Get_Success() {
        ApiIdAORsp register = service.register("test");
        ApiIdAORsp get = service.find(register.getApiId());

        assertEquals(register.getApiId(), get.getApiId());
        assertNotNull(get.getValid());
        assertNotNull(get.getCreated());
        assertNotNull(get.getModified());
    }

    @Test
    public void Test_Get_NotFound() {
        ApiException ex = assertThrows(ApiException.class, () -> {
            service.find(UUID.randomUUID().toString());
        });
        assertEquals(ex.getCode(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void Test_GetAll_00_Success() {
        String customerId = UUID.randomUUID().toString();
        service.register(customerId);
        ApiReplyAO<List<ApiIdAORsp>> all = service.all(customerId, 0, 100);

        assertEquals(all.getData().size(), 1);
        assertEquals(all.getPage().getPage(), 0);
        assertEquals(all.getPage().getTotalPages(), 1);
        assertEquals(all.getPage().getSize(), 1);
        assertEquals(all.getPage().getTotalElements(), 1);
    }

    @Test
    public void Test_GetAll_12_Success() {
        String customerId = UUID.randomUUID().toString();
        service.register(customerId);
        service.register(customerId);
        service.register(customerId);
        ApiReplyAO<List<ApiIdAORsp>> all = service.all(customerId, 1, 2);

        assertEquals(all.getData().size(), 1);
        assertEquals(all.getPage().getPage(), 1);
        assertEquals(all.getPage().getTotalPages(), 2);
        assertEquals(all.getPage().getSize(), 1);
        assertEquals(all.getPage().getTotalElements(), 3);

    }

    @Test
    public void Test_GetAll_Empty() {
        ApiReplyAO<List<ApiIdAORsp>> all = service.all(UUID.randomUUID().toString(), 0, 100);

        assertEquals(all.getData().size(), 0);
        assertEquals(all.getPage().getPage(), 0);
        assertEquals(all.getPage().getTotalPages(), 0);
        assertEquals(all.getPage().getSize(), 0);
        assertEquals(all.getPage().getTotalElements(), 0);
    }
}