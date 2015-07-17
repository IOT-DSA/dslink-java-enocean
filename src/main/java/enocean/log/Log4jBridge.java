package enocean.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dsa.iot.dslink.util.log.LogBridge;
import org.dsa.iot.dslink.util.log.LogLevel;
import org.dsa.iot.dslink.util.log.LogManager;

/**
 * @author Samuel Grenier
 */
public class Log4jBridge implements LogBridge {

    private LogLevel level;

    //@Override
    public void configure() {
    }

    //@Override
    public void setLevel(LogLevel level) {
        Logger logger = org.apache.log4j.LogManager.getRootLogger();
        switch (level) {
            case OFF:
                logger.setLevel(Level.OFF);
                break;
            case ERROR:
                logger.setLevel(Level.ERROR);
                break;
            case WARN:
                logger.setLevel(Level.WARN);
                break;
            case INFO:
                logger.setLevel(Level.INFO);
                break;
            case TRACE:
                level = LogLevel.DEBUG;
                logger.setLevel(Level.DEBUG);
                break;
            case DEBUG:
                logger.setLevel(Level.DEBUG);
                break;
            default:
                throw new RuntimeException("Unknown log level: " + level);
        }
        this.level = level;
    }

    public LogLevel getLevel() {
        return level;
    }

    public static void init() {
        LogManager.setBridge(new Log4jBridge());
    }
}
