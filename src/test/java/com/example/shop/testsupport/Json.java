package com.example.shop.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

//Convertir objetos Java a JSON
public final class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private Json() {}
    public static String toJson(Object o){
        try { return MAPPER.writeValueAsString(o); } catch (Exception e) { throw new RuntimeException(e); }
    }
}