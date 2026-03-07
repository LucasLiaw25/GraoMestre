package com.liaw.dev.GraoMestre.exception;

import java.time.LocalDateTime;

public record ErrorMessage(int status, String message, LocalDateTime dateTime) {
}
