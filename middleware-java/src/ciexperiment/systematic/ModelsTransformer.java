package ciexperiment.systematic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.egl.EglFileGeneratingTemplate;
import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EglTemplateFactoryModuleAdapter;
import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ModelsTransformer {

	public ArrayList<String> retriveAllModels(String baseModel) throws Exception {

		ArrayList<String> mmURIS = registerMMs();

		EmfModel sourceModel = new EmfModel();
		sourceModel.setName("Source");
		File sourceModelFile = new File(baseModel);
		sourceModel.setModelFile(sourceModelFile.getAbsolutePath());
		sourceModel.setMetamodelUris(mmURIS);

		sourceModel.setReadOnLoad(true);
		sourceModel.load();

		// !!! HERE WE WRITE THE LOGIC FOR MUTATION OF MODELS !!!

		// Simple queries to get objects by type and their attributes
		Set<EObject> mutableObjects = new HashSet<EObject>();

		for (EObject obj : sourceModel.getAllOfKind("Component")) {
			EStructuralFeature mutable = obj.eClass().getEStructuralFeature("mutable");
			if ((Boolean) obj.eGet(mutable)) {
				mutableObjects.add(obj);
			}
		}

		Set<Set<EObject>> allCombinationsSet = Sets.powerSet(mutableObjects);
		Set<Set<EObject>> allAcceptableSets = new HashSet<Set<EObject>>();
		List<MutationGroup> allMutationGroupsWithMemberIds = new ArrayList<MutationGroup>();
		Collection<EObject> mutationGroups = sourceModel.getAllOfType("MutationGroup");

		for (EObject mutGroup : mutationGroups) {
			MutationGroup mg = new MutationGroup();
			EStructuralFeature maxLimit = mutGroup.eClass().getEStructuralFeature("maxRequiredFromGroup");
			EStructuralFeature minLimit = mutGroup.eClass().getEStructuralFeature("minRequiredFromGroup");
			mg.setMaxLimit((Integer) mutGroup.eGet(maxLimit));
			mg.setMinLimit((Integer) mutGroup.eGet(minLimit));
			mg.setGroupID(sourceModel.getElementId(mutGroup));

			List<EObject> groupMembers = new ArrayList<EObject>();
			EStructuralFeature membersRef = mutGroup.eClass().getEStructuralFeature("members");
			groupMembers = (List<EObject>) mutGroup.eGet(membersRef);
			Set<String> membersIds = new HashSet<String>();
			for (EObject member : groupMembers) {
				String memberId = sourceModel.getElementId(member);
				mg.getMemberIds().add(memberId);
			}
			allMutationGroupsWithMemberIds.add(mg);
		}

		List<EObject> missions = (List<EObject>) sourceModel.getAllOfType("Mission");
		String missionId = sourceModel.getElementId(missions.get(0));
//		System.out.println("Mission ID: " + missionId);
		for (Set<EObject> aSet : allCombinationsSet) {
			if (!aSet.isEmpty()) {
				boolean rejected = false;
				Set<String> mutableIdsInTheSet = new HashSet<String>();
				for (EObject aMutableObjectInTheSet : aSet) {
					// String id = getElementId(aMutableObjectInTheSet);
					String id = sourceModel.getElementId(aMutableObjectInTheSet);
					mutableIdsInTheSet.add(id);
				}
//				System.out.println("----");

//				System.out.println("mutableIdsInTheSet: " + mutableIdsInTheSet);

				// Reject if parent not in the set (i.e., is not included in the mutated model)
//				System.out.println("Mutable Size: " + aSet.size());
				for (EObject aMutableObjectInTheSet : aSet) {
					EStructuralFeature objectName = aMutableObjectInTheSet.eClass().getEStructuralFeature("name");
					System.out.println("Name: " + aMutableObjectInTheSet.eGet(objectName));
					EObject parent = (EObject) sourceModel.getContainerOf(aMutableObjectInTheSet);
					String parentId = sourceModel.getElementId(parent);
//					System.out.println("Parent ID: " + parentId);
					// FIXME: If parent is not mutable, then sets containing children are rejected.
					// We need to reject if mutable and non-existing in the current set instead.
					if (parentId.equals(missionId)) {
//						System.out.println("Top Element");
					} else if (!mutableIdsInTheSet.contains(parentId)) {
//						System.out.println("Parent rejected (Parent: " + parentId + ", children: " + mutableIdsInTheSet + ")");
						rejected = true;
						break;
					}
				}
//				System.out.println("End of Parent Check. Rejected? " + rejected);

				// If not already rejected, check if group rules are violated.

				if (!rejected) {
					HashMap<String, Integer> groupIdToCountersMap = new HashMap<String, Integer>();
					for (MutationGroup mg : allMutationGroupsWithMemberIds) {
						groupIdToCountersMap.put(mg.getGroupID(), 0);
					}
					for (EObject aMutableObjectInTheSet : aSet) {
						for (MutationGroup mg : allMutationGroupsWithMemberIds) {
							if (mg.getMemberIds().contains(sourceModel.getElementId(aMutableObjectInTheSet))) {
								groupIdToCountersMap.put(mg.getGroupID(),
										groupIdToCountersMap.get(mg.getGroupID()) + 1);
							}
						}

					}
					for (MutationGroup mg : allMutationGroupsWithMemberIds) {
						String mgId = mg.getGroupID();
						int mgMax = mg.getMaxLimit();
						int mgMin = mg.getMinLimit();
						int mgCounter = groupIdToCountersMap.get(mgId);
						if (mgCounter > mgMax || mgCounter < mgMin) {
							// Reject if and only if the mutation group violates the limits AND the parent of this group is in the mutated model.
							// This needs to be done in cases where the lower limit is > 0, but these elements do not appear at all in the mutated model
							// Which is OK because maybe the parent does not appear at all. Thus we don't want to reject this set completely because it
							// does not contain e.g. sensors which is ok not to be contained as the parent is not appearing in this mutation.
							EObject oneChild = (EObject) sourceModel.getElementById(mg.getMemberIds().get(0));
							EObject parent = oneChild.eContainer();
							EStructuralFeature childName = oneChild.eClass().getEStructuralFeature("name");
							EStructuralFeature parentName = parent.eClass().getEStructuralFeature("name");
							String parentId = sourceModel.getElementId(parent);
							System.out.println("One Child: " + oneChild.eGet(childName));
//							System.out.println("The parent: " + parent.eGet(parentName));

							if (aSet.contains(parent) || parentId.equals(missionId)) {
								System.out.println("Rejected");
								rejected = true;
								break;
							}

						}
					}
					System.out.println("+++++");
				}
				if (!rejected) {
					allAcceptableSets.add(aSet);
				}

//				System.out.println("One Set: " + mutableIdsInTheSet);
			}

		}
		System.out.println("allAcceptableSets: " + allAcceptableSets.size());
		ArrayList<String> allTheCreatedModels = new ArrayList<String>();
		for (Set<EObject> aSet : allAcceptableSets) {
			// Copy Model
			UUID uuid = UUID.randomUUID();
			Path copied = Paths.get(sourceModelFile.getAbsolutePath() + "-" + uuid.toString() + ".model");
			Path originalPath = Paths.get(sourceModelFile.getAbsolutePath());
			try {
				Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Load copied Model
			EmfModel targetModel = new EmfModel();
			targetModel.setName("Target");
			File targetModelFile = new File(copied.toAbsolutePath().toString());
			allTheCreatedModels.add(targetModelFile.getAbsolutePath());
			targetModel.setModelFile(targetModelFile.getAbsolutePath());
			targetModel.setMetamodelUris(mmURIS);
			targetModel.setStoredOnDisposal(true);
			targetModel.load();
			Set<EObject> diff = Sets.difference(mutableObjects, aSet); // use the copy constructor
			for (EObject elementToRemove : diff) {
				String id = sourceModel.getElementId(elementToRemove);
				System.out.println(id);
				EObject elementFromTarget = (EObject) targetModel.getElementById(id);
				if (elementFromTarget != null) {
					EcoreUtil.delete(elementFromTarget, true);
//					targetModel.deleteElement(elementFromTarget);

				}
			}
			targetModel.dispose();
		}
		
		return allTheCreatedModels;
	}

	protected EmfModel createEmfModel(String name, String model, String metamodel, boolean readOnLoad,
			boolean storeOnDisposal) throws EolModelLoadingException, URISyntaxException {
		EmfModel emfModel = new EmfModel();
		StringProperties properties = new StringProperties();
		properties.put(EmfModel.PROPERTY_NAME, name);
		properties.put(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI, getFileURI(metamodel).toString());
		properties.put(EmfModel.PROPERTY_MODEL_URI, getFileURI(model).toString());
		properties.put(EmfModel.PROPERTY_READONLOAD, readOnLoad + "");
		properties.put(EmfModel.PROPERTY_STOREONDISPOSAL, storeOnDisposal + "");
		emfModel.load(properties, (IRelativePathResolver) null);
		return emfModel;
	}

	protected static String getElementId(EObject eObj) {
		return EcoreUtil.getIdentification(eObj).split("#_")[1].substring(0,
				EcoreUtil.getIdentification(eObj).split("#_")[1].length() - 1);
	}

	protected static ArrayList<String> registerMMs() throws IOException {

		ArrayList<String> mmURIs = new ArrayList<String>();
		Resource.Factory xmiFactory = new XMIResourceFactoryImpl();

		Resource missionMM = xmiFactory.createResource(URI.createFileURI("ecore-metamodels-combined/atlas.ecore"));
		missionMM.load(null);
		EPackage pkgMission = (EPackage) missionMM.getContents().get(0);
		EPackage.Registry.INSTANCE.put(pkgMission.getNsURI(), pkgMission);
		mmURIs.add(pkgMission.getNsURI());

//		Resource componentsMM = xmiFactory.createResource(URI.createFileURI("ecore-metamodels-thanos/components.ecore"));
//		componentsMM.load(null);
//		EPackage pkgComponents = (EPackage) componentsMM.getContents().get(0);
//		EPackage.Registry.INSTANCE.put(pkgComponents.getNsURI(), pkgComponents);
//		mmURIs.add(pkgComponents.getNsURI());
//
//		Resource regionMM = xmiFactory.createResource(URI.createFileURI("ecore-metamodels-thanos/region.ecore"));
//		regionMM.load(null);
//		EPackage pkgRegion = (EPackage) regionMM.getContents().get(0);
//		EPackage.Registry.INSTANCE.put(pkgRegion.getNsURI(), pkgRegion);
//		mmURIs.add(pkgRegion.getNsURI());
//
//		Resource faultsMM = xmiFactory.createResource(URI.createFileURI("ecore-metamodels-thanos/faults.ecore"));
//		faultsMM.load(null);
//		EPackage pkgFaults = (EPackage) faultsMM.getContents().get(0);
//		EPackage.Registry.INSTANCE.put(pkgFaults.getNsURI(), pkgFaults);
//		mmURIs.add(pkgFaults.getNsURI());
//
//		Resource messageMM = xmiFactory.createResource(URI.createFileURI("ecore-metamodels-thanos/message.ecore"));
//		messageMM.load(null);
//		EPackage pkgMessage = (EPackage) messageMM.getContents().get(0);
//		EPackage.Registry.INSTANCE.put(pkgMessage.getNsURI(), pkgMessage);
//		mmURIs.add(pkgMessage.getNsURI());

		return mmURIs;
	}

	protected static java.net.URI getFileURI(String fileName) throws URISyntaxException {
		java.net.URI binUri = ModelsTransformer.class.getResource(fileName).toURI();
		return binUri.toString().contains("bin") ? new java.net.URI(binUri.toString().replaceAll("bin", "src"))
				: binUri;
	}
}
