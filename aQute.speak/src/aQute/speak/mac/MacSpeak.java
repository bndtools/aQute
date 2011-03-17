package aQute.speak.mac;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import aQute.bnd.annotation.component.Component;
import aQute.service.speak.Speak;

@Component
public class MacSpeak implements Speak {
	ScriptEngine engine = new ScriptEngineManager().getEngineByName("AppleScript");
	
	@Override
	public void say(String message) throws Exception {
		engine.eval("say \"" + message + "\"");
	}

}
