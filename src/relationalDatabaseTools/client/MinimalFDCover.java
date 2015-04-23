package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinimalFDCover {
	public static void determineMinimalCover(final Relation relation) {
		List<FunctionalDependency> fMin = new ArrayList<>();
		// Split FDs that have more than one attribute on right-side.
		for (FunctionalDependency f : relation.getInputFDs()) {
			if (f.isProperFD) {
				if (f.getRightHandAttributes().size() == 1) {
					fMin.add(f);
				} else {
					for (Attribute a : f.getRightHandAttributes()) {
						List<Attribute> rightSplitted = new ArrayList<>();
						rightSplitted.add(a);
						FunctionalDependency splitted = new FunctionalDependency(f.getLeftHandAttributes(), rightSplitted, relation);
						fMin.add(splitted);
					}
				}
			}
		}
		// Minimize left-hand side
		List<FunctionalDependency> minimizedLHS = new ArrayList<>();
		for (FunctionalDependency f : fMin) {
			// Only need to consider FDs whose LHS has 2 or more attributes
			if (f.getLeftHandAttributes().size() > 1) {
				List<Attribute> minimizedLeftAttributes = new ArrayList<>();
				for (Attribute a : f.getLeftHandAttributes()) {
					// Check if attribute is necessary by taking it out and computing closure.
					Attribute rightAttribute = f.getRightHandAttributes().get(0);
					List<Attribute> newLeftSide = new ArrayList<>();
					for (Attribute b : f.getLeftHandAttributes()) {
						if (!b.getName().equals(a.getName())) {
							newLeftSide.add(b);
						}
					}
					Closure closure = null;
					// Find closure with new left-hand side
					for (Closure c : relation.getClosures()) {
						if (c.getClosureOf().size() == newLeftSide.size()) {
							boolean containsAll = true;
							for (Attribute attr : newLeftSide) {
								if (!CalculateClosure.containsAttribute(c.getClosureOf(), attr)) {
									containsAll = false;
									break;
								}
							}
							if (containsAll) {
								closure = c;
								break;
							}
						}
					}
					
					// Now check if the right-hand attribute is still in the closure.
					if (!CalculateClosure.containsAttribute(closure.getClosure(), rightAttribute)) {
						// Removed attribute is necessary. Add to list.
						minimizedLeftAttributes.add(a);
					}
				}
				if (minimizedLeftAttributes.size() < f.getLeftHandAttributes().size()) {
					if (!minimizedLeftAttributes.isEmpty()) {
						FunctionalDependency reducedFD = new FunctionalDependency(minimizedLeftAttributes, f.getRightHandAttributes(), relation);
						minimizedLHS.add(reducedFD);
					}
				} else {
					minimizedLHS.add(f);
				}
			} else {
				minimizedLHS.add(f);
			}
		}
		fMin.clear();
		for (FunctionalDependency funcDe : minimizedLHS) {
			if (funcDe.isProperFD) {
				fMin.add(funcDe);
			}
		}
		// Now minimize the set of functional dependencies
		int[] blocked = new int[fMin.size()];
		for (int y = 0; y < blocked.length; y++) {
			blocked[y] = 0;
		}
		List<FunctionalDependency> minimizedSetFDs = new ArrayList<>();
		for (int i = 0; i < fMin.size(); i++) {
			if (blocked[i] != 0) {
				continue;
			}
			// Create temporary subset of FDs with the given FD removed
			List<FunctionalDependency> checkRemoved = new ArrayList<>();
			for (int j = 0; j < fMin.size(); j++) {
				if (j != i && blocked[j] == 0) {
					checkRemoved.add(fMin.get(j));
				}
			}
			Closure checkClosure = CalculateClosure.calculateClosureOf(fMin.get(i).getLeftHandAttributes(), checkRemoved);
			if (!CalculateClosure.containsAttribute(checkClosure.getClosure(), fMin.get(i).getRightHandAttributes().get(0))) {
				// The FD is necessary since the new closure does not contain the right-hand side attribute of the removed FD.
				minimizedSetFDs.add(fMin.get(i));
			} else {
				// The FD is not necessary and we can strike it out of the list.
				blocked[i] = 1;
			}
		}
		fMin.clear();
		fMin.addAll(minimizedSetFDs);
		// Now consolidate FDs that have common left-hand side
		List<FunctionalDependency> consolidatedFDs = new ArrayList<>();
		int[] checkedIndices = new int[fMin.size()];
		for (int z = 0; z < checkedIndices.length; z++) {
			checkedIndices[z] = 0;
		}
		for (int i = 0; i < fMin.size(); i++) {
			if (checkedIndices[i] != 0) {
				continue;
			}
			FunctionalDependency f = fMin.get(i);
			checkedIndices[i] = 1;
			List<Attribute> leftHandSide = new ArrayList<>();
			List<Attribute> rightHandSide = new ArrayList<>();
			for (Attribute leftAttr : f.getLeftHandAttributes()) {
				leftHandSide.add(leftAttr);
			}
			for (Attribute rightAttr : f.getRightHandAttributes()) {
				rightHandSide.add(rightAttr);
			}
			for (int j= 0; j < fMin.size(); j++) {
				if (j != i && checkedIndices[j] == 0) {
					FunctionalDependency g = fMin.get(j);
					if (g.getLeftHandAttributes().size() == leftHandSide.size()) {
						boolean containsAll = true;
						for (Attribute leftAttr : leftHandSide) {
							if (!CalculateClosure.containsAttribute(g.getLeftHandAttributes(), leftAttr)) {
								containsAll = false;
								break;
							}
						}
						if (containsAll) {
							for (Attribute dupRightAttr : g.getRightHandAttributes()) {
								rightHandSide.add(dupRightAttr);
							}
							checkedIndices[j] = 1;
						}
					}
				}
			}
			FunctionalDependency consolidated = new FunctionalDependency(leftHandSide, rightHandSide, relation);
			consolidatedFDs.add(consolidated);
		}
		Collections.sort(consolidatedFDs);
		for (FunctionalDependency consolidatedFD : consolidatedFDs) {
			relation.addMinimalCoverFD(consolidatedFD);
		}
	}
}
