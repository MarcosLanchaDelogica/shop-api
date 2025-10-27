package com.example.shop.unit;

import com.example.shop.domain.model.*;
import com.example.shop.dto.common.PagedResponse;
import com.example.shop.dto.customer.in.*;
import com.example.shop.dto.customer.out.*;
import com.example.shop.exceptions.*;
import com.example.shop.mappers.common.PageMapper;
import com.example.shop.mappers.customer.*;
import com.example.shop.repository.*;
import com.example.shop.service.CustomerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock CustomerRepository customerRepo;
    @Mock AddressRepository addressRepo;
    @Mock CustomerMapper customerMapper;
    @Mock AddressMapper addressMapper;
    @Mock PageMapper pageMapper;

    @InjectMocks CustomerService service;

    private Customer customer;
    private CustomerResponse resp;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setId(1L);
        customer.setEmail("ana@example.com");
        customer.setFullName("Ana");
        customer.setAddresses(new ArrayList<>());

        resp = new CustomerResponse(1L, "Ana", "ana@example.com", "600000000", null, null);
    }

    //Creación
    @Test
    void createCustomer_shouldSucceedOrThrowConflict() {
        var dto = new CustomerCreateRequest("Ana", "ana@example.com", "600000000");
        when(customerRepo.existsByEmail(dto.email())).thenReturn(false);
        when(customerMapper.toEntity(dto)).thenReturn(customer);
        when(customerRepo.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(resp);

        assertEquals(resp.email(), service.createCustomer(dto).email());

        when(customerRepo.existsByEmail(dto.email())).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.createCustomer(dto));
    }

    //Listado
    @Test
    void getAllCustomers_shouldReturnOrHandleBadRequest() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(customer));
        var pagedResp = new PagedResponse<>(List.of(resp), 0, 10, 1, 1, true);
        when(customerRepo.findAll(pageable)).thenReturn(page);
        when(pageMapper.toPagedResponse(any())).thenReturn((PagedResponse) pagedResp);
        when(customerMapper.toResponse(customer)).thenReturn(resp);

        assertEquals(1, service.getAllCustomers(pageable, Optional.empty()).content().size());

        when(customerRepo.findByEmailContainingIgnoreCase("ana", pageable)).thenReturn(page);
        assertEquals(1, service.getAllCustomers(pageable, Optional.of("ana")).content().size());

        doThrow(PropertyReferenceException.class).when(customerRepo).findAll(pageable);
        assertThrows(BadRequestException.class, () -> service.getAllCustomers(pageable, Optional.empty()));

    }

    //Consulta y actualización
    @Test
    void getOrUpdateCustomer_shouldReturnOrThrowNotFound() {
        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(resp);
        assertEquals(resp.id(), service.getCustomer(1L).id());

        when(customerRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getCustomer(99L));

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepo.save(customer)).thenReturn(customer);
        var dto = new CustomerUpdateRequest("Ana 2", "nueva@x.com", "611111111");
        service.updateCustomer(1L, dto);
        verify(customerMapper).update(dto, customer);

        when(customerRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.updateCustomer(99L, dto));
    }

    //Eliminación
    @Test
    void deleteCustomer_shouldDeleteOrThrow() {
        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        service.deleteCustomer(1L);
        verify(customerRepo).delete(customer);

        when(customerRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.deleteCustomer(99L));
    }

    //Direcciones
    @Test
    void addAddress_shouldPersistAndReturnList() {
        Address addr = new Address();
        addr.setId(5L);
        addr.setCustomer(customer);
        var addrResp = new AddressResponse(5L, 1L, "Calle", "123", "Madrid", "28001", "España", true);

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(addressMapper.toEntity(any())).thenReturn(addr);
        when(addressRepo.save(addr)).thenReturn(addr);
        when(addressMapper.toResponse(any())).thenReturn(addrResp);

        var result = service.addAddress(1L, mock(AddressCreateRequest.class));

        assertEquals(1, result.size());
        verify(addressRepo).save(addr);
    }

    @Test
    void setDefaultAddress_shouldMarkAndSaveOrThrow() {
        Address addr = new Address();
        addr.setId(5L);
        addr.setCustomer(customer);
        var addrResp = new AddressResponse(5L, 1L, "Calle", "123", "Madrid", "28001", "España", true);

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepo.findById(5L)).thenReturn(Optional.of(addr));
        when(addressMapper.toResponse(addr)).thenReturn(addrResp);

        service.setDefaultAddress(1L, 5L);
        verify(addressRepo).save(addr);

        when(addressRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.setDefaultAddress(1L, 99L));

        var other = new Customer();
        other.setId(2L);
        var wrongAddr = new Address();
        wrongAddr.setId(6L);
        wrongAddr.setCustomer(other);
        when(addressRepo.findById(6L)).thenReturn(Optional.of(wrongAddr));
        assertThrows(ConflictException.class, () -> service.setDefaultAddress(1L, 6L));
    }

}
