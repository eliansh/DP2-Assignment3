package it.polito.dp2.WF.sol3;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import javax.xml.ws.Holder;


import it.polito.dp2.WF.*;
import it.polito.dp2.WF.lab3.gen.Action;
import it.polito.dp2.WF.lab3.gen.GetWorkflows;
import it.polito.dp2.WF.lab3.gen.UnknownNames;
import it.polito.dp2.WF.lab3.gen.UnknownNames_Exception;
import it.polito.dp2.WF.lab3.gen.Workflow;
import it.polito.dp2.WF.lab3.gen.WorkflowInfo;
public class Parser {
	//private String customizedURL;
	private WorkflowInfo proxy;
	private HashMap<String, MyWorkFlowReader> workFlows = new HashMap<String, MyWorkFlowReader>();
	private HashSet<MyProcessReader> allProcess = null;
	private Holder<List<String>> name = new Holder<List<String>>();
	private Holder<XMLGregorianCalendar> lastModTime = new Holder<XMLGregorianCalendar>();
	private Holder<List<Workflow>> workflow = new Holder<List<Workflow>>();
	
	public Parser(WorkflowInfo proxy, HashMap<String, MyWorkFlowReader> workFlows, HashSet<MyProcessReader> allProcess){
		//this.customizedURL = customizedURL;
		this.proxy = proxy;
		this.workFlows = workFlows;
		this.allProcess = null;
	}
	
	public void parse() throws WorkflowMonitorException{

		proxy.getWorkflowNames(lastModTime, name);
		GetWorkflows gwf = new GetWorkflows();
		gwf.getWfname().clear();
		for(String s:name.value){
			gwf.getWfname().add(s);		
		}

		try {
		
			proxy.getWorkflows(name.value, lastModTime, workflow);
			
			parseWorkFlowsNode(workflow.value);
		} catch (UnknownNames_Exception e) {
			throw new WorkflowMonitorException("The inserted name is unknown");
		}

	}

	private void parseWorkFlowsNode(List<Workflow> workFlowsNode) throws WorkflowMonitorException {
		for(Workflow workFlow : workFlowsNode){
			parseWorkFlowNode(workFlow);			
		}
	}

	private void parseWorkFlowNode(Workflow workFlowNode) throws WorkflowMonitorException{
		String flowName = workFlowNode.getName();
		
		
		HashMap<String, MyActionReader> actions = new HashMap<String, MyActionReader>();
		HashSet<MyProcessReader> processes = null;
		MyWorkFlowReader wfs = new MyWorkFlowReader(actions, flowName, processes);
		List<Action> ac = workFlowNode.getAction();
		//ActionsType actionsNode = workFlowNode.getActions();
		for(Action actionNode : ac){
			String actionName = actionNode.getName();
			if(!MyActionReader.isNameValid(actionName))
				throw new WorkflowMonitorException("action name is not correct");

			String actionRole =actionNode.getRole();
			if(!MyActionReader.isRoleValid(actionRole))
				throw new WorkflowMonitorException("action role is not correct");
			boolean automaticallyInstantiated = actionNode.isAutomaticallyInstantiated();
			MyActionReader action = new MyActionReader(wfs, actionName, actionRole, automaticallyInstantiated);
			if(actionNode.getNextAction()!=null){
				//SimpleActType simpleActNode = actionNode.getSimpleAct();
					HashSet<MyActionReader> nextActions = new HashSet<MyActionReader>();
					for(String s: actionNode.getNextAction()){
						for(MyActionReader a:actions.values()){
							if(a.getName().equals(s))
								nextActions.add(a);
						}						
					}
					action = new MySimpleActionReader(wfs, actionName, actionRole, automaticallyInstantiated, nextActions);
						
						}
					actions.put(actionName, action);
		}	
		//allProcess.addAll(processes);
		MyWorkFlowReader wf = new MyWorkFlowReader(actions, flowName, processes);
		workFlows.put(flowName, wf);
	}
		
			
			private static Calendar parseDate(String string) throws ParseException {
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
				Calendar cal  = Calendar.getInstance();
				dateFormat.setTimeZone(TimeZone.getTimeZone("CEST"));
				cal.setTime(dateFormat.parse(string));
				return cal;
			}
			
			private static String formatDate(Calendar calendar) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
				dateFormat.setTimeZone(calendar.getTimeZone());
				return dateFormat.format(calendar.getTime());
			}
			
}


