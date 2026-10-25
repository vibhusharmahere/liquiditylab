package com.liquiditylab.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

        @NotBlank
        @Size(max = 34)
        String accountNumber,

        @NotBlank
        @Size(max = 120)
        String accountName,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency

) {
}