package dast.bdi;

import java.io.IOException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.jdi.connect.Connector.StringArgument;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;

public class DastVisualizer {
	private VirtualMachine vm;
	private boolean attached = false;
	private String[] excludes = {"java.*", "javax.*", "sun.*", 
	 "com.sun.*"};
	
	private Vector<OutputListener> diagnosticsListeners = new Vector<OutputListener>();
	private Session session;
	private List threadInfoList = new LinkedList();
	Vector<SessionListener> sessionListeners = new Vector<SessionListener>();
	private ExecutionManager runntime = new ExecutionManager();
	
	public static void main(String args[]) {
		DastVisualizer application = new DastVisualizer();
		application.main();
	}
	
	private void main(){
		VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
		List<AttachingConnector> acs = vmm.attachingConnectors();
		
		AttachingConnector connector = null;
		Iterator<AttachingConnector> it = acs.iterator();
        while (it.hasNext()) {
            AttachingConnector tmp = (AttachingConnector)  it.next();
            if ("com.sun.jdi.SocketAttach".equals(tmp.name())) {
               connector = tmp;
               break;
            }
        }
        
        Map<String, Argument> arg = connector.defaultArguments();
        IntegerArgument portNumber =
            ( IntegerArgument ) arg.get("port");
        portNumber.setValue(8000);
        StringArgument hostname =
            ( StringArgument ) arg.get("hostname");
        hostname.setValue("localhost");
        
        while(attached == false){
     		try {
     			vm = connector.attach(arg);
     			} catch (IOException | IllegalConnectorArgumentsException e) {
     				e.printStackTrace();
     			}
     		if(vm != null){
     			attached = true;
     		}
     	}
        
        Session newSession = runntime.internalAttach(connector, arg);
        if (newSession != null) {
			try {
				runntime.startSession(newSession);
			} catch (VMLaunchFailureException e) {
				e.printStackTrace();
			}
		}
	}
	
}