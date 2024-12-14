package com.dmsc.cryptofinanceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class WalletNotFoundException extends ResponseStatusException {
    public WalletNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
