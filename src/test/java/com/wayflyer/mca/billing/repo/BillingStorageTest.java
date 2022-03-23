package com.wayflyer.mca.billing.repo;

import com.wayflyer.mca.billing.domain.Advance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BillingStorageTest {

    @InjectMocks
    BillingStorage storage;

    @BeforeEach
    void setUp() {
        storage.getStorage().clear();
    }

    @Test
    void saveAdvance() {
        Advance ad1=  new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);
        Advance ad2=  new Advance(3, 2, "2022-01-10", "300000.00", "14000.00", 1, "2022-01-10", 17);
        storage.saveAdvance(Arrays.asList(ad1,ad2));
        assertEquals(2,storage.getStorage().size());
    }

    @Test
    void updateAdvance() {
        Advance ad1=  new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);
        storage.saveAdvance(List.of(ad1));
        ad1.setFee("0");
        storage.updateAdvance(ad1);
       String fee = storage.getStorage().get(1).getFee();
        assertEquals("0",fee);
    }

    @Test
    void getStorage(){
       Map<Integer,Advance> store =  storage.getStorage();
       assertTrue(store.isEmpty());
    }
}