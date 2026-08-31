package ru.morkamo.kontrolbankdata.controller;

final class FormValidationException extends RuntimeException {

    FormValidationException(String message) {
        super(message);
    }
}
