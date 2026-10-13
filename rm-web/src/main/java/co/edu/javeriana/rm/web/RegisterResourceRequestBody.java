package co.edu.javeriana.rm.web;

import co.edu.javeriana.rm.domain.model.Priority;

/**
 * Sin validaciones ni campo de estado: los datos se transportan como llegan para que sea el tier
 * de negocio el que los rechace.
 */
public record RegisterResourceRequestBody(String disasterDescription, String resourceType,
                                          Integer quantity, Priority priority) {
}
