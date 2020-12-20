package faultgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atlasdsl.InvalidComponentType;
import atlasdsl.Mission;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;

public class FaultFileIO {
	private Mission mission;
	private CountHashmap<Fault> countFaults;
	
	private Pattern optionalScanner = Pattern.compile("Optional\\[([^,]+)\\]");
	
	public FaultFileIO(Mission mission) {
		this.mission = mission;
	}
	
	public Optional<String> decodeOptional(String encodedOptional) {
		Matcher m = optionalScanner.matcher(encodedOptional);
		if (m.find()) {
			return Optional.of(m.group(1));
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<FaultInstance> decodeFaultFromString(String faultDefinition) throws InvalidFaultFormat, InvalidComponentType, FaultNotFoundInModel, FaultInstanceInvalid, FaultRepeatCountInvalid {
		String[] fields = faultDefinition.split(",");
		if (fields.length < 3) {
			// Ignore the situation with an empty line or a single name tag
			if (fields.length > 1) {
				throw new InvalidFaultFormat();
			} else {
				return Optional.empty();
			}
		} else {
			
			int faultInstanceNum = Integer.parseInt(fields[0]);
			String faultNameInModel = fields[1];
			Double startTime = Double.parseDouble(fields[2]);
			Double length = Double.parseDouble(fields[3]);
			Double endTime = startTime + length;
			boolean isActive = Boolean.parseBoolean(fields[4]);
			Optional<String> extraData = Optional.empty();
			if (fields.length > 5) {
				extraData = Optional.of(fields[5]);
			}
			
			Optional<Fault> f_o = mission.lookupFaultByName(faultNameInModel);
			if (f_o.isPresent()) {
				Fault f = f_o.get();
				countFaults.incrementCount(f);
				if ((countFaults.getCount(f)) > f.getMaxCount()) {
					throw new FaultRepeatCountInvalid();
				}
				
				FaultInstance fi = new FaultInstance(startTime, endTime, f, extraData);
				fi.setActiveFlag(isActive);
				
				if (!fi.isValid()) {
					throw new FaultInstanceInvalid();
				} else return Optional.of(fi);
			} else {
				throw new FaultNotFoundInModel(faultNameInModel);
			}
		}
	}
	
	// TODO: should be renamed to loadFaultInstancesFromFile
	public List<FaultInstance> loadFaultsFromFile(String filename) throws FileNotFoundException, InvalidFaultFormat {
		List<FaultInstance> outputFaults = new ArrayList<FaultInstance>();
		countFaults = new CountHashmap<Fault>();

		File f = new File(filename);
		Scanner reader = new Scanner(f);
		while (reader.hasNextLine()) {
			String faultAsString = reader.nextLine();
			Optional<FaultInstance> fi_o;
			
			try {
				fi_o = decodeFaultFromString(faultAsString);
				if (fi_o.isPresent()) {
					FaultInstance fi = fi_o.get();
					outputFaults.add(fi);
				}
			} catch (InvalidComponentType e) {
				e.printStackTrace();
			} catch (FaultNotFoundInModel e) {
				e.printStackTrace();
			} catch (FaultInstanceInvalid e) {
				e.printStackTrace();
			} catch (FaultRepeatCountInvalid e) {
				e.printStackTrace();
			}
		}
		reader.close();
		return outputFaults;
	}
	
	public List<FaultInstance> loadFaultsFromJMetalFile(String filename, int selectedLine) throws FileNotFoundException, InvalidFaultFormat {
		List<FaultInstance> outputFaults = new ArrayList<FaultInstance>();
		countFaults = new CountHashmap<Fault>();
		
		File f = new File(filename);
		Scanner reader = new Scanner(f);
		int line = 0;
		while (reader.hasNextLine()) {
			String faultset = reader.nextLine();
			
			if (line == selectedLine) {
				String faults[] = faultset.split(":");
				
				for (String fiSpec : faults) {
					Optional<FaultInstance> fi_o;
					try {
							fi_o = decodeFaultFromStringJMetal(fiSpec);
							if (fi_o.isPresent()) {
								FaultInstance fi = fi_o.get();
								outputFaults.add(fi);
							}
					} catch (InvalidComponentType e) {
						e.printStackTrace();
					} catch (FaultNotFoundInModel e) {
						e.printStackTrace();
					} catch (FaultInstanceInvalid e) {
						e.printStackTrace();
					} catch (FaultRepeatCountInvalid e) {
						e.printStackTrace();
					}
				}
			}
			line++;
		}
		reader.close();
		return outputFaults;
	}

	private Optional<FaultInstance> decodeFaultFromStringJMetal(String faultDefinition) throws InvalidFaultFormat, InvalidComponentType, FaultNotFoundInModel, FaultInstanceInvalid, FaultRepeatCountInvalid {
		String[] fields = faultDefinition.split(",");
		if (fields.length < 3) {
			// Ignore the situation with an empty line or a single name tag
			if (fields.length > 1) {
				throw new InvalidFaultFormat();
			} else {
				return Optional.empty();
			}
		} else {
			
			int faultInstanceNum = Integer.parseInt(fields[0]);
			String faultNameInModel = fields[1];
			Double startTime = Double.parseDouble(fields[2]);
			Double endTime = Double.parseDouble(fields[3]);
			boolean isActive = true;
			Optional<String> extraData = Optional.empty();
			if (fields.length > 4) {
				String extraDataEncoded = fields[4];
				extraData = decodeOptional(extraDataEncoded);
			}
			
			Optional<Fault> f_o = mission.lookupFaultByName(faultNameInModel);
			if (f_o.isPresent()) {
				Fault f = f_o.get();
				countFaults.incrementCount(f);
				if ((countFaults.getCount(f)) > f.getMaxCount()) {
					throw new FaultRepeatCountInvalid();
				}
				
				FaultInstance fi = new FaultInstance(startTime, endTime, f, extraData);
				fi.setActiveFlag(isActive);
				
				if (!fi.isValid()) {
					throw new FaultInstanceInvalid();
				} else return Optional.of(fi);
			} else {
				throw new FaultNotFoundInModel(faultNameInModel);
			}
		}
		
	}
}


