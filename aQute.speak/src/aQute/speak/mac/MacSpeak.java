package aQute.speak.mac;

import javax.script.*;

import aQute.bnd.annotation.component.*;
import aQute.service.speak.*;

@Component
public class MacSpeak implements Speak {
	ScriptEngine	engine	= new ScriptEngineManager().getEngineByName("AppleScript");

	@Override
	public void say(String message) throws Exception {
		engine.eval("say \"" + message + "\"");
	}

}
