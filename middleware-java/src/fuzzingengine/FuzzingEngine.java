package fuzzingengine;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import java.lang.reflect.*;

import atlasdsl.Component;
import atlasdsl.Computer;
import atlasdsl.Message;
import atlasdsl.Mission;
import atlasdsl.Robot;
import carsspecific.ros.carsqueue.ROSTopicUpdate;
import fuzzingengine.FuzzingSimMapping.VariableDirection;
import fuzzingengine.FuzzingSimMapping.VariableSpecification;
import fuzzingengine.operations.EventFuzzingOperation;
import fuzzingengine.operations.FuzzingOperation;
import fuzzingengine.operations.ValueFuzzingOperation;
import middleware.core.*;
import middleware.logging.ATLASLog;

public class FuzzingEngine<E> {
	Mission m;
	FuzzingConfig confs = new FuzzingConfig();
	FuzzingSimMapping simmapping = new FuzzingSimMapping();
	

	PriorityQueue<FutureEvent<E>> delayedEvents = new PriorityQueue<FutureEvent<E>>();

	public FuzzingEngine(Mission m) {
		this.m = m;
	}

	private List<String> getVehicles(String vehicleList) throws MissingRobot {
		if (vehicleList.toUpperCase().equals("ALL")) {
			return m.getAllRobotAndComputerNames();
		} else if (vehicleList.equals("")) {
			return m.getAllRobotAndComputerNames();

		} else {
			List<String> vehicleNames = Arrays.asList(vehicleList.split("\\|"));
			// Check for the missing robots in the model
			return vehicleNames;
		}
	}

	public void addFuzzingKeyOperation(String fuzzingKey, String vehicleNameList, int groupNum, double startTime,
			double endTime, FuzzingOperation op) throws MissingRobot {
		VariableSpecification vr = simmapping.getRecordForKey(fuzzingKey);
		List<String> vehicles = getVehicles(vehicleNameList);
		FuzzingKeySelectionRecord fr = new FuzzingKeySelectionRecord(fuzzingKey, vr.getReflectionName_opt(),
				vr.getComponent(), vr.getRegexp(), groupNum, op, vehicles, startTime, endTime);
		confs.addKeyRecord(fr);
	}

	private void addFuzzingComponentOperation(String componentName, FuzzingOperation op, String vehicleNameList)
			throws MissingRobot {
		List<String> vehicles = getVehicles(vehicleNameList);
		FuzzingComponentSelectionRecord cr = new FuzzingComponentSelectionRecord(componentName, op, vehicles);
		confs.addComponentRecord(cr);
	}

	public void addFuzzingMessageOperation(String messageName, String messageFieldName, int groupNum, double startTime,
			double endTime, FuzzingOperation op) throws InvalidMessage {
		Message msg = m.getMessage(messageName);
		if (msg == null) {
			throw new InvalidMessage(messageName, "Message not in model");
		} else {
			String primedKeyName = messageFieldName + "'";
			VariableSpecification vr = simmapping.getRecordForKey(primedKeyName);
			if (vr == null) {
				throw new InvalidMessage(messageName,
						"Simmapping key not present for message field name " + messageFieldName);
			} else {
				// TODO: handle case where the given message is not defined in the mission
				FuzzingMessageSelectionRecord mr = new FuzzingMessageSelectionRecord(messageFieldName, msg, op);

				List<String> participants = new ArrayList<String>();

				for (Component cFrom : msg.getFrom()) {
					if (cFrom instanceof Robot) {
						participants.add(((Robot) cFrom).getName());
					} else {
						participants.add(((Computer) cFrom).getName());
					}
				}


				FuzzingKeySelectionRecord kr = new FuzzingKeySelectionRecord(primedKeyName,
						Optional.of(messageFieldName), vr.getComponent(), vr.getRegexp(), groupNum, op, participants,
						startTime, endTime);

				// For all dests, have to set them as a participant for the fuzzing key record
				List<Component> cDests = msg.getTo();

				for (Component c : cDests) {
					String destName;
					if (c instanceof Computer) {
						Computer comp = (Computer) c;
						destName = comp.getName();
						kr.addParticipant(destName);
						System.out.println("Adding participant " + destName);
					}
				}

				for (Component c : cDests) {
					String destName;
					if (c instanceof Robot) {
						Robot r = (Robot) c;
						destName = r.getName();
						kr.addParticipant(destName);
					}
				}

				confs.addKeyRecord(kr);
				confs.addMessageRecord(mr);
			}
		}
	}

	// need to test: look up the key in simmodel, is its specific component set as
	// active
	private Optional<FuzzingOperation> getOperationByInboundComponentAndVehicle(String key, String vehicle) {
		VariableSpecification vr = simmapping.getRecordForKey(key);
		if (vr != null) {
			Optional<String> comp = vr.getComponent();
			if (comp.isPresent()) {
				return confs.getOperationByOutboundComponentAndVehicle(comp.get(), vehicle);
			}
		}

		return Optional.empty();
	}

	public Optional<String> getKeyOrTopic(E event) {
		if (event instanceof KeyValueUpdate) {
			KeyValueUpdate kv = (KeyValueUpdate) event;
			return Optional.of(kv.getKey());
		}

		if (event instanceof ROSTopicUpdate) {
			ROSTopicUpdate rtu = (ROSTopicUpdate) event;
			return Optional.of(rtu.getTopicName());
		}
		return Optional.empty();
	}

	public List<FuzzingOperation> shouldFuzzCARSEvent(E event, double time) {
		List<FuzzingOperation> res = new ArrayList<FuzzingOperation>();
		if (event instanceof KeyValueUpdate) {
			KeyValueUpdate cv = (KeyValueUpdate) event;
			String vehicle = cv.getVehicleName();
			String key = cv.getKey();
			ATLASLog.logFuzzing("shouldFuzzCARSEvent called on vehicle " + vehicle + " - key " + key);
			Optional<FuzzingOperation> op_o = confs.getOperationByKeyAndVehicle(key, vehicle, time);
			if (op_o.isPresent()) {
				res.add(op_o.get());
			}
		}

		if (event instanceof ROSTopicUpdate) {
			ROSTopicUpdate rtu = (ROSTopicUpdate) event;
			String vehicle = rtu.getVehicleName();
			String key = rtu.getTopicName();
			Optional<FuzzingOperation> op_o = confs.getOperationByKeyAndVehicle(key, vehicle, time);
			if (op_o.isPresent()) {
				res.add(op_o.get());
			}
		}

		return res;
	}

	public Optional<String> shouldReflectBackToCARS(E event) {
		if (event instanceof KeyValueUpdate) {
			KeyValueUpdate cv = (KeyValueUpdate) event;
			String k = cv.getKey();
			return simmapping.getReflectionKey(k);
		}

		if (event instanceof ROSTopicUpdate) {
			ROSTopicUpdate rtu = (ROSTopicUpdate) event;
			String k = rtu.getTopicName();
			return simmapping.getReflectionKey(k);
		}

		return Optional.empty();
	}

	public static String replaceGroup(Pattern p, String source, int groupToReplace, ValueFuzzingOperation op)
			throws FuzzingEngineMatchFailure {
		Matcher m = p.matcher(source);
		if (!m.find()) {
			throw new FuzzingEngineMatchFailure(p, source);
		} else {
			String v = m.group(groupToReplace);
			String newV = op.fuzzTransformString(v);
			return new StringBuilder(source).replace(m.start(groupToReplace), m.end(groupToReplace), newV).toString();
		}
	}
	
	private JsonObject insertValue(JsonObject source, String key, String value) {
	    JsonObjectBuilder builder = Json.createObjectBuilder();
	    builder.add(key, value);
	    source.entrySet().
	            forEach(e -> builder.add(e.getKey(), e.getValue()));
	    return builder.build();
	}
	
	public static JsonObject replaceJSON(JsonObject js, String jsonSpec, ValueFuzzingOperation op) throws FuzzingEngineMatchFailure {
		// Split the jsonSpec on periods
		JsonObjectBuilder builder = Json.createObjectBuilder();
	    for (Map.Entry<String,JsonValue> entry : js.entrySet()){
	        if (entry.getKey().equals("")){
	    		String fuzzed = op.fuzzTransformString(js.toString());
	    		JsonReader jsonReader = Json.createReader(new StringReader(fuzzed));
	    		JsonObject jModified = jsonReader.readObject();
	            builder.add(entry.getKey(), entry.getValue());
	        } else {
	            builder.add(entry.getKey(), entry.getValue());
	        }
	    }    
		
		
//		String [] jsonSpecFields = jsonSpec.split(".");
//		JsonObject jo = js;
//		JsonObject joParent = jo;
//		for (int i = 0; i < jsonSpecFields.length; i++) {
//			String specField = jsonSpecFields[0];
//			joParent = jo;
//			jo = jo.getJsonObject(specField);
//		}
//		
//		String fuzzed = op.fuzzTransformString(jo.toString());
//		JsonReader jsonReader = Json.createReader(new StringReader(fuzzed));
//		JsonObject jModified = jsonReader.readObject();
		return js;
	}

	public Optional<E> fuzzTransformEvent(Optional<E> event_o, FuzzingOperation op) {
		if (op.isEventBased()) {
			EventFuzzingOperation eop = (EventFuzzingOperation) op;
			return eop.fuzzTransformPotentialEvent(event_o);
		} else {
			if (event_o.isPresent()) {
				E event = event_o.get();
				if (event instanceof KeyValueUpdate) {
					KeyValueUpdate cv = (KeyValueUpdate) event;
					KeyValueUpdate newUpdate = new KeyValueUpdate(cv);
					ValueFuzzingOperation vop = (ValueFuzzingOperation) op;
					String key = cv.getKey();
					String v = cv.getValue();
					String newValue = v;

					Optional<Map.Entry<Pattern, Object>> regexp_sel = confs.getPatternAndGroupStructure(key);
					if (!regexp_sel.isPresent()) {
						newValue = vop.fuzzTransformString(v);
					} else {
						Pattern p = regexp_sel.get().getKey();
						// TODO: check this is an integer
						Integer groupNum = (Integer)regexp_sel.get().getValue();
						try {
							newValue = replaceGroup(p, v, groupNum, vop);
						} catch (FuzzingEngineMatchFailure e) {
							ATLASLog.logFuzzing("FuzzingEngineMatchFailure - " + event + e);
						}
					}

					newUpdate.setValue(newValue);
					return Optional.of((E)newUpdate);
				} else if (event instanceof ROSTopicUpdate) {
					ROSTopicUpdate rtu = (ROSTopicUpdate)event;
					ValueFuzzingOperation vop = (ValueFuzzingOperation)op;
					String key = rtu.getTopicName();
					JsonObject js = rtu.getJSON();
					JsonObject newValue = js;
					
					Optional<Map.Entry<Pattern, Object>> regexp_sel = confs.getPatternAndGroupStructure(key);
					if (!regexp_sel.isPresent()) {
						// This fuzzes the JSON object as a string representation, re-parses it
						String fuzzed = vop.fuzzTransformString(js.toString());
						JsonReader jsonReader = Json.createReader(new StringReader("{}"));
						JsonObject obj = jsonReader.readObject();
						jsonReader.close();
						
					} else {
						Pattern p = regexp_sel.get().getKey();
						String jsonSpec = (String)regexp_sel.get().getValue();
						try {
							newValue = replaceJSON(js, jsonSpec, vop);
						} catch (FuzzingEngineMatchFailure e) {
							ATLASLog.logFuzzing("FuzzingEngineMatchFailure - " + event + e);
						}
					}
					return event_o;
					
				} else {
					return event_o;
				}
			} else
				return event_o;
		}
	}

	public FuzzingSimMapping getSimMapping() {
		return simmapping;
	}

	public FuzzingConfig getConfig() {
		return confs;
	}

	public HashMap<String, String> getVariables() {
		// Need to return: any variables from components enabled for binary fuzzing
		// With either the component enabled for fuzzing
		// Or the individual variable enabled
		HashMap<String, String> vars = new HashMap<String, String>();
		// for (confs.getAllKeysByComponent(component)) {
		// confs.getAllKeysByComponent
		// }

		return vars;

	}

	public void setSimMapping(FuzzingSimMapping simMapping) {
		this.simmapping = simMapping;
	}

	public List<String> getMessageKeys(String robotName, VariableDirection dir) {
		return confs.getMessageKeys(robotName, dir);
	}

	public Optional<FuzzingOperation> loadOperation(String className, String params) {
		try {
			Class<?> c = Class.forName("fuzzingengine.operations." + className);
			System.out.println("className for operation " + className);
			Method method = c.getDeclaredMethod("createFromParamString", String.class);
			Object res = method.invoke(null, params);
			if (res instanceof FuzzingOperation) {
				return Optional.of((FuzzingOperation) res);
			} else {
				return Optional.empty();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public void setupFromFuzzingFile(String fileName, Mission m) {
		System.out.println("setupFromFuzzingFile - " + fileName);
		try {
			Files.readAllLines(Paths.get(fileName)).forEach(line -> {
				if (line.charAt(0) != '#') {
					String fields[] = line.split(",");
					System.out.println("0 - " + fields[0]);
					int i = 0;

					for (final String token : fields) {
						if (!token.isEmpty()) {
							System.out.println("Token:" + i + " " + token);
						}
						i++;
					}

					// Scan for key record in file
					if (fields[0].toUpperCase().equals("KEY")) {
						System.out.println("KEY based fuzzing");
						String varName = fields[1];
						double startTime = Double.parseDouble(fields[2]);
						double endTime = Double.parseDouble(fields[3]);
						String vehicleNames = fields[4];
						Integer groupNum = Integer.valueOf(fields[5]);
						String opClass = fields[6];
						String params = fields[7];

						Optional<FuzzingOperation> op_o = loadOperation(opClass, params);
						if (op_o.isPresent()) {
							FuzzingOperation op = op_o.get();
							try {
								addFuzzingKeyOperation(varName, vehicleNames, groupNum, startTime, endTime, op);
							} catch (MissingRobot e) {
								System.out.println("Missing robot: name = " + e.getName());
								e.printStackTrace();
							}
							System.out.println("Installing fuzzing operation for " + varName + " - " + op);
						}
					}

					// Scan for component record in file
					if (fields[0].toUpperCase().equals("COMPONENT")) {
						// TODO: parse the vehicle names from here
						String componentName = fields[1];
						String vehicleNames = fields[2];
						String dirString = fields[3];
						// TODO: not using the direction string yet
						String opClass = fields[4];
						String params = fields[5];
						Optional<FuzzingOperation> op_o = loadOperation(opClass, params);
						if (op_o.isPresent()) {
							FuzzingOperation op = op_o.get();
							try {
								addFuzzingComponentOperation(componentName, op, vehicleNames);
							} catch (MissingRobot e) {
								e.printStackTrace();
							}
							System.out.println(
									"Installing fuzzing operation for component " + componentName + " - " + op);
						}
					}

					// Scan for message
					if (fields[0].toUpperCase().equals("MESSAGE")) {
						String messageName = fields[1];
						double startTime = Double.parseDouble(fields[2]);
						double endTime = Double.parseDouble(fields[3]);
						String messageFieldName = fields[4];
						Integer groupNum = Integer.valueOf(fields[5]);
						String opClass = fields[6];
						String params = fields[7];
						Optional<FuzzingOperation> op_o = loadOperation(opClass, params);
						if (op_o.isPresent()) {
							FuzzingOperation op = op_o.get();
							// Where to get the regexp for the message fields? - in the model
							try {
								addFuzzingMessageOperation(messageName, messageFieldName, groupNum, startTime, endTime,
										op);
							} catch (InvalidMessage e) {
								System.out
										.println("Invalid message name: " + e.getMessageName() + " - " + e.getReason());
								e.printStackTrace();
								// TODO: raise exception to indicate the conversion failed
							}
							System.out.println("Installing fuzzing operation for " + messageName + ":"
									+ messageFieldName + " - " + op);
						}
					}

				}
			});

		} catch (NoSuchFileException ex) {
			System.out.println("Fuzzing file " + fileName + " not found - performing a null fuzzing experiment");
		}

		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// This gets the explicitly selected keys upon this component
	public Map<String, String> getAllChanges(String component) {
		Map<String, String> inOut = new HashMap<String, String>();

		List<FuzzingKeySelectionRecord> recs = getConfig().getAllKeysByComponent(component);
		for (FuzzingKeySelectionRecord kr : recs) {
			if (kr.getReflectionKey().isPresent()) {
				inOut.put(kr.getKey(), kr.getReflectionKey().get());
			}
		}
		return inOut;
	}

	// This gets the keys upon this component defined in the simmapping
	public Map<String, String> getAllChangesDefinedOn(String component) {
		Map<String, String> inOut = new HashMap<String, String>();

		Set<VariableSpecification> recs = getSimMapping().getRecordsUponComponent(component);
		for (VariableSpecification vr : recs) {
			inOut.put(vr.getVariable(), vr.getReflectionName());
		}
		return inOut;
	}

	public void addToQueue(E e, double releaseTime, Optional<String> reflectBackName) {
		// TODO: define comparators for the release time
		delayedEvents.add(new FutureEvent(e, releaseTime, reflectBackName));
	}

	public List<FutureEvent<E>> pollEventsReady(double byTime) {
		List<FutureEvent<E>> res = new ArrayList<FutureEvent<E>>();
		FutureEvent<E> fe = delayedEvents.peek();

		if (fe != null) {
			if (fe.getPendingTime() < byTime) {
				// System.out.println("Event ready: pendingTime = " + fe.getPendingTime() + ",
				// byTime=" + byTime);
				FutureEvent<E> feRemoved = delayedEvents.remove();
				res.add(feRemoved);
			}
		}
		return res;
	}
}
