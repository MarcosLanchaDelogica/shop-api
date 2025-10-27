package com.example.shop.service;

import com.example.shop.domain.model.Address;
import com.example.shop.domain.model.Customer;
import com.example.shop.dto.common.PagedResponse;
import com.example.shop.dto.customer.in.AddressCreateRequest;
import com.example.shop.dto.customer.in.CustomerCreateRequest;
import com.example.shop.dto.customer.in.CustomerUpdateRequest;
import com.example.shop.dto.customer.out.AddressResponse;
import com.example.shop.dto.customer.out.CustomerResponse;
import com.example.shop.exceptions.BadRequestException;
import com.example.shop.exceptions.ConflictException;
import com.example.shop.exceptions.NotFoundException;
import com.example.shop.mappers.common.PageMapper;
import com.example.shop.mappers.customer.AddressMapper;
import com.example.shop.mappers.customer.CustomerMapper;
import com.example.shop.repository.AddressRepository;
import com.example.shop.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerMapper customerMapper;
    private final AddressMapper addressMapper;
    private final PageMapper pageMapper;

    // Crear un nuevo cliente a partir del DTO de entrada.
    public CustomerResponse createCustomer(CustomerCreateRequest dto) {
        log.info("Intentando crear cliente con email: {}", dto.email());

        // Compruebo si ya existe un cliente con el mismo email.
        if (customerRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Ya existe un cliente con ese email");
        }

        // MapStruct transforma el DTO en una entidad Customer lista para persistir.
        Customer customer = customerMapper.toEntity(dto);

        // Guardo el cliente en la base de datos.
        Customer saved = customerRepository.save(customer);

        log.info("Cliente creado con ID {} y email {}", saved.getId(), saved.getEmail());

        // Devuelvo el objeto de salida mapeado con los datos finales del cliente.
        return customerMapper.toResponse(saved);
    }

    // Obtener todos los clientes con paginación y filtro opcional por email.
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getAllCustomers(Pageable pageable, Optional<String> email) {
        try {
            // Filtro por email si lo especifican
            Page<Customer> page = email
                    .map(e -> customerRepository.findByEmailContainingIgnoreCase(e, pageable))
                    .orElseGet(() -> customerRepository.findAll(pageable));

            // Mapeo y convierto a formato paginado propio
            Page<CustomerResponse> mapped = page.map(customerMapper::toResponse);
            return pageMapper.toPagedResponse(mapped);
        }
        catch (PropertyReferenceException ex) {
            // Si el campo de sort no existe (por ejemplo "sort=['asc']")
            throw new BadRequestException("El campo de ordenación proporcionado no es válido. Usa sort=fullName,asc");
        }
    }

    // Obtener un cliente por su ID.
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        // Intento encontrar el cliente; si no existe, lanzo excepción 404.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        // Lo convierto a DTO para devolver solo la información necesaria.
        return customerMapper.toResponse(customer);
    }

    // Actualizar un cliente existente con los datos del DTO.
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest dto) {
        // Busco el cliente a actualizar.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        // Aplico los cambios del DTO a la entidad con MapStruct.
        customerMapper.update(dto, customer);

        // Guardo la entidad actualizada.
        Customer updated = customerRepository.save(customer);

        // Devuelvo el DTO de respuesta.
        return customerMapper.toResponse(updated);
    }

    //Eliminar un cliente por ID.
    public void deleteCustomer(Long id) {
        // Busco el cliente y lanzo excepción si no existe.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        // Elimino el cliente de la base de datos.
        customerRepository.delete(customer);
    }

    // Añadir una nueva dirección a un cliente existente.
    public List<AddressResponse> addAddress(Long customerId, AddressCreateRequest dto) {
        // Verifico que el cliente exista.
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        // Transformo el DTO de la dirección en entidad.
        Address address = addressMapper.toEntity(dto);

        // Asocio la dirección al cliente.
        address.setCustomer(customer);

        // Guardo la nueva dirección.
        addressRepository.save(address);

        // Añado la dirección a la lista del cliente (en memoria).
        customer.getAddresses().add(address);

        // Devuelvo la lista de direcciones actualizada.
        return customer.getAddresses().stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    // Establecer una dirección como predeterminada para un cliente.
    public AddressResponse setDefaultAddress(Long customerId, Long addressId) {
        // Busco el cliente.
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        // Busco la dirección.
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));

        // Compruebo que la dirección pertenezca al cliente correcto.
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new ConflictException("La dirección no pertenece al cliente");
        }

        // Desmarco todas las direcciones anteriores como predeterminadas.
        customer.getAddresses().forEach(a -> a.setDefault(false));

        // Marco la nueva como predeterminada y guardo.
        address.setDefault(true);
        addressRepository.save(address);

        // Devuelvo la dirección actualizada.
        return addressMapper.toResponse(address);
    }
}
