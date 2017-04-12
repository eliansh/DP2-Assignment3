package it.polito.dp2.WF.sol3;

import it.polito.dp2.WF.lab3.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.WorkflowMonitor;
import it.polito.dp2.WF.WorkflowMonitorException;
import it.polito.dp2.WF.WorkflowReader;
import it.polito.dp2.WF.lab3.gen.*;

public class MyWorkFlowMonitor implements WorkflowMonitor, Refreshable {
	
	private HashMap<String, MyWorkFlowReader> workFlows = new HashMap<String, MyWorkFlowReader>();
	private HashSet<MyProcessReader> allProcess = null;
	private WorkflowInfo proxy;
	//private static final String SCHEMA_FILE = "xsd" + File.separatorChar + "WFInfo.xsd";
	
	public MyWorkFlowMonitor() throws WorkflowMonitorException, MalformedURLException {
		
		URL url = null;
		QName qname = null;
		try{
			url = new URL(System.getProperty("it.polito.dp2.WF.sol3.URL"));
			qname = new QName("http://pad.polito.it/WorkflowInfo","WorkflowInfoService");
		}catch (MalformedURLException e) {
			e.printStackTrace();
			System.err.println("Wrong url!");
		}catch (IllegalArgumentException iae){
			iae.printStackTrace();
		}
			
			WorkflowInfoService service = new WorkflowInfoService(url, qname);
		
		    proxy = service.getWorkflowInfoPort();
		    
		 Parser  parser = new Parser(proxy, workFlows, allProcess);
		 parser.parse();
	}

	@Override
	public Set<ProcessReader> getProcesses() {
		return new HashSet<ProcessReader>(allProcess);
	}

	@Override
	public WorkflowReader getWorkflow(String name) {
		if(isNameValid(name)){
			return workFlows.get(name);
		}
		else
			return null;
	}

	@Override
	public Set<WorkflowReader> getWorkflows() {
		return new HashSet<WorkflowReader>(workFlows.values());
	}
	
	public static boolean isNameValid(String name) {
		String Regx = "[A-Za-z][A-Za-z0-9]*";
		return (name==null || name.matches(Regx));
	}
	private static String formatDate(Calendar calendar) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
		dateFormat.setTimeZone(calendar.getTimeZone());
		return dateFormat.format(calendar.getTime());
	}

	@Override
	public void refresh() {
		Parser  parser = new Parser(proxy, workFlows, allProcess);
		 try {
			parser.parse();
		} catch (WorkflowMonitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

