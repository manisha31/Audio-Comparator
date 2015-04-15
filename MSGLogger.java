import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.io.IOException;
import java.util.logging.SimpleFormatter;
//import java.util.date;

public class MSGLogger{
    private static MSGLogger instance = null;
    private static Logger LOGGER ;
    
    
    private MSGLogger(){
    	
    }
    
    
    public static Logger getInstance(){
        if(instance == null){
            instance = new MSGLogger();
            instance.init();
        }
        return instance.LOGGER;
    }

    
    private void init(){
        FileHandler fh;
        try{
            LOGGER =  Logger.getLogger("4AudioSerach");
            java.util.Date date = new java.util.Date();
            String fileName = "./logdir/AudioSearch_"+date.getTime()+".log";
            fh = new FileHandler(fileName);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            LOGGER.addHandler(fh);
            LOGGER.setUseParentHandlers(false);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
