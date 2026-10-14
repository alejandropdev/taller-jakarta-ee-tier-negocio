package co.edu.javeriana.rm.web;

import co.edu.javeriana.rm.domain.model.Priority;

public record RegisterResourceRequestBody(String disasterDescription, String resourceType,
                                          Integer quantity, Priority priority) {
}
