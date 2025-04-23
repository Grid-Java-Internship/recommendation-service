package com.internship.recommendation_service.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LogUtil {
    /**
     * Logs an INFO message with the given format string and arguments.
     *
     * @param message the message to be logged
     * @param args    the arguments to be used in the formatting
     */
    public void info(String message, Object... args) {
        log.info(message, args);
    }

    /**
     * Logs a WARN message with the given format string and arguments.
     *
     * @param message the message to be logged
     * @param args    the arguments to be used in the formatting
     */
    public void warn(String message, Object... args) {
        log.warn(message, args);
    }

    /**
     * Logs an ERROR message with the given format string and arguments.
     *
     * @param message the message to be logged
     * @param args    the arguments to be used in the formatting
     */
    public void error(String message, Object... args) {
        log.error(message, args);
    }
}
