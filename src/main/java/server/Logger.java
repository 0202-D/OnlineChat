package server;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Dm.Petrov
 * DATE: 28.09.2022
 */
public class Logger {
    private static Logger INSTANCE = null;
    private static FileWriter fileWriter;
    private Logger() { }

    public static Logger getInstance() {
        if (INSTANCE == null) {
            synchronized (Logger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Logger();
                }
            }
        }
        return INSTANCE;
    }

    public void log( String msg) {
        try {
            fileWriter = new FileWriter("logger.txt",true);
            fileWriter.write(msg+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
