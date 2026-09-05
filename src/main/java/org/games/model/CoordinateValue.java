package org.games.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CoordinateValue(Double constant, Double sqrt2, Double sqrt3) {}
