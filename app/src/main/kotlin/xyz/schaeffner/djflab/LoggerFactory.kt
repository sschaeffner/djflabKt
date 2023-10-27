package xyz.schaeffner.djflab

import org.slf4j.Logger

fun loggerFactory(clazz: Class<*>): Logger = org.slf4j.LoggerFactory.getLogger(clazz)
