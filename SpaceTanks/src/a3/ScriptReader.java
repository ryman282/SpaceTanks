package a3;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptReader {
	
	private ScriptEngineManager factory;
	private ScriptEngine jsEngine;
	private FileReader filereader;
	
	public ScriptReader() {
		factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");
	}
	
	public ScriptEngine getEngine() {
		return jsEngine;
	}
	
	public void openScript(String scriptName) {
		try
		{filereader = new FileReader(scriptName);
		 jsEngine.eval(filereader);
		 filereader.close();	
		}
		catch(FileNotFoundException e1)
		{System.out.println(scriptName + " not found " + e1);}
		catch(IOException e2)
		{System.out.println("IO problem with " + scriptName + e2);}
		catch(ScriptException e3)
		{System.out.println("ScriptException in " + scriptName + e3);}
		catch(NullPointerException e4)
		{System.out.println("Null ptr exception in " + scriptName + e4);}
	}	
}
