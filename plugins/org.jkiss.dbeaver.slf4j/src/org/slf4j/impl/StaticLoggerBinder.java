/*
  DBeaver loopback
 */
package org.slf4j.impl;

import org.jkiss.dbeaver.Log;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * 
 * Dummy logger
 */
public class StaticLoggerBinder implements LoggerFactoryBinder, ILoggerFactory {

    private static final Log log = Log.getLog(StaticLoggerBinder.class);

    public ILoggerFactory getLoggerFactory() {
        return this;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return StaticLoggerBinder.class.getName();
    }

    @Override
    public Logger getLogger(String name) {
        return new Logger() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isTraceEnabled() {
                return false;
            }

            @Override
            public void trace(String s) {

            }

            @Override
            public void trace(String s, Object o) {

            }

            @Override
            public void trace(String s, Object o, Object o1) {

            }

            @Override
            public void trace(String s, Object... objects) {

            }

            @Override
            public void trace(String s, Throwable throwable) {

            }

            @Override
            public boolean isTraceEnabled(Marker marker) {
                return false;
            }

            @Override
            public void trace(Marker marker, String s) {

            }

            @Override
            public void trace(Marker marker, String s, Object o) {

            }

            @Override
            public void trace(Marker marker, String s, Object o, Object o1) {

            }

            @Override
            public void trace(Marker marker, String s, Object... objects) {

            }

            @Override
            public void trace(Marker marker, String s, Throwable throwable) {

            }

            @Override
            public boolean isDebugEnabled() {
                return true;
            }

            @Override
            public void debug(String s) {
                log.debug(s);
            }

            @Override
            public void debug(String s, Object o) {
                log.debug(s);
            }

            @Override
            public void debug(String s, Object o, Object o1) {
                log.debug(s);
            }

            @Override
            public void debug(String s, Object... objects) {
                log.debug(s);
            }

            @Override
            public void debug(String s, Throwable throwable) {
                log.debug(s, throwable);
            }

            @Override
            public boolean isDebugEnabled(Marker marker) {
                return false;
            }

            @Override
            public void debug(Marker marker, String s) {
            }

            @Override
            public void debug(Marker marker, String s, Object o) {
            }

            @Override
            public void debug(Marker marker, String s, Object o, Object o1) {
            }

            @Override
            public void debug(Marker marker, String s, Object... objects) {
            }

            @Override
            public void debug(Marker marker, String s, Throwable throwable) {
            }

            @Override
            public boolean isInfoEnabled() {
                return true;
            }

            @Override
            public void info(String s) {
                log.info(s);
            }

            @Override
            public void info(String s, Object o) {
                log.info(s);
            }

            @Override
            public void info(String s, Object o, Object o1) {
                log.info(s);
            }

            @Override
            public void info(String s, Object... objects) {
                log.info(s);
            }

            @Override
            public void info(String s, Throwable throwable) {
                log.info(s, throwable);
            }

            @Override
            public boolean isInfoEnabled(Marker marker) {
                return false;
            }

            @Override
            public void info(Marker marker, String s) {
            }

            @Override
            public void info(Marker marker, String s, Object o) {
            }

            @Override
            public void info(Marker marker, String s, Object o, Object o1) {
            }

            @Override
            public void info(Marker marker, String s, Object... objects) {
            }

            @Override
            public void info(Marker marker, String s, Throwable throwable) {
            }

            @Override
            public boolean isWarnEnabled() {
                return true;
            }

            @Override
            public void warn(String s) {
                log.warn(s);
            }

            @Override
            public void warn(String s, Object o) {
                log.warn(s);
            }

            @Override
            public void warn(String s, Object... objects) {
                log.warn(s);
            }

            @Override
            public void warn(String s, Object o, Object o1) {
                log.warn(s);
            }

            @Override
            public void warn(String s, Throwable throwable) {
                log.warn(s, throwable);
            }

            @Override
            public boolean isWarnEnabled(Marker marker) {
                return false;
            }

            @Override
            public void warn(Marker marker, String s) {
            }

            @Override
            public void warn(Marker marker, String s, Object o) {
            }

            @Override
            public void warn(Marker marker, String s, Object o, Object o1) {
            }

            @Override
            public void warn(Marker marker, String s, Object... objects) {
            }

            @Override
            public void warn(Marker marker, String s, Throwable throwable) {
            }

            @Override
            public boolean isErrorEnabled() {
                return true;
            }

            @Override
            public void error(String s) {
                log.error(s);
            }

            @Override
            public void error(String s, Object o) {
                log.error(s);
            }

            @Override
            public void error(String s, Object o, Object o1) {
                log.error(s);
            }

            @Override
            public void error(String s, Object... objects) {
                log.error(s);
            }

            @Override
            public void error(String s, Throwable throwable) {
                log.error(s, throwable);
            }

            @Override
            public boolean isErrorEnabled(Marker marker) {
                return false;
            }

            @Override
            public void error(Marker marker, String s) {
            }

            @Override
            public void error(Marker marker, String s, Object o) {
            }

            @Override
            public void error(Marker marker, String s, Object o, Object o1) {
            }

            @Override
            public void error(Marker marker, String s, Object... objects) {
            }

            @Override
            public void error(Marker marker, String s, Throwable throwable) {
            }
        };
    }
}
