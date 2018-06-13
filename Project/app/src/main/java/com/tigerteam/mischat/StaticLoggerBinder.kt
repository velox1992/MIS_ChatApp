package com.tigerteam.mischat

import io.underdark.util.nslogger.NSLoggerFactory
import org.slf4j.ILoggerFactory

class StaticLoggerBinder private constructor() {

    val loggerFactory: ILoggerFactory

    val loggerFactoryClassStr = NSLoggerFactory::class.java.name

    init {
        loggerFactory = NSLoggerFactory("underdark")
    }

    companion object {
        /**
         * The unique instance of this class.
         */
        /**
         * Return the singleton of this class.
         *
         * @return the StaticLoggerBinder singleton
         */
        val singleton = StaticLoggerBinder()

        /**
         * Declare the version of the SLF4J API this implementation is compiled against.
         * The value of this field is usually modified with each release.
         */
        // to avoid constant folding by the compiler, this field must *not* be final
        var REQUESTED_API_VERSION = "1.7.12"  // !final
    }
} // StaticLoggerBinder
