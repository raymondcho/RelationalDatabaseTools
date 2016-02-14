package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Relational Database Tools user interface.
 * @author Raymond Cho
 *
 */
public class RelationalDatabaseTools implements EntryPoint {
	
	private final VerticalPanel mainPanel = new VerticalPanel();
	private final HorizontalPanel panel_1 = new HorizontalPanel();
	private final HorizontalPanel panel_2 = new HorizontalPanel();
	private final HorizontalPanel panel_3 = new HorizontalPanel();
	private final HorizontalPanel panel_4 = new HorizontalPanel();
	private final VerticalPanel outputPanel = new VerticalPanel();
	
	private final Label label_1 = new Label("Enter the relation schema in form R(A,B,C,AB,ABC)");
	private final Label label_1b = new Label("Use commas to separate attributes. Spaces are optional. Inputs are case-insensitive.");
	private final TextBox textBox_1 = new TextBox();
	
	private final Label label_2 = new Label("Enter all given functional dependencies in form A -> B; AB -> C; B,C -> A");
	private final Label label_2b = new Label("Use commas to separate attributes that belong to the same side of the same functional dependency.");
	private final Label label_2c = new Label("Use semi-colons to separate different functional dependencies.");
	private final TextBox textBox_2 = new TextBox();
	private final VerticalPanel secondaryFDPanel = new VerticalPanel();
	
	private final Label label_3 = new Label("Enter all given multivalued dependencies in form A -> B; AB -> C; B,C ->A (same as functional dependencies)");
	private final Label label_3b = new Label("Leave blank if there are none.");
	private final TextBox textBox_3 = new TextBox();

	
	private final Button calculateButton = new Button("Calculate");
	private final Button resetButton = new Button("Reset");
	
	private Label outputLabel = new Label();
	private final Label errorLabel = new Label();
	
	private final List<Label> outputs = new ArrayList<>();
	
	private Relation relation;
	
	@Override
	public void onModuleLoad() {
		resetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resetDisplay();
			}			
		});
		
		panel_1.add(label_1);
		panel_1.add(textBox_1);
		textBox_1.addStyleName("textboxes");
		panel_1.add(label_1b);
		panel_1.addStyleName("panels");
		
		secondaryFDPanel.add(label_2b);
		secondaryFDPanel.add(label_2c);
		
		panel_2.add(label_2);
		panel_2.add(textBox_2);
		textBox_2.addStyleName("textboxes");
		panel_2.add(secondaryFDPanel);
		panel_2.addStyleName("panels");
		
		panel_3.add(label_3);
		panel_3.add(textBox_3);
		panel_3.add(label_3b);
		textBox_3.addStyleName("textboxes");
		panel_3.addStyleName("panels");
		
		panel_4.add(calculateButton);
		calculateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				calculate();
			}
		});
		panel_4.addStyleName("panels");
		
		errorLabel.addStyleName("errorLabel");
		outputPanel.add(errorLabel);
		outputPanel.add(outputLabel);
		outputPanel.addStyleName("panels");
		
		mainPanel.add(resetButton);
		mainPanel.add(panel_1);
		mainPanel.add(panel_2);
		mainPanel.add(panel_3);
		mainPanel.add(panel_4);
		mainPanel.add(outputPanel);
		mainPanel.addStyleName("panels");
		
		outputs.add(outputLabel);
		
		RootPanel.get("relationalTools").add(mainPanel);
		relation = null;
	}

	private void calculate() {
		String completeRelation = textBox_1.getText().toUpperCase();
		textBox_1.setText(completeRelation);
		String completeFDs = textBox_2.getText().toUpperCase();
		textBox_2.setText(completeFDs);
		String completeMVDs = textBox_3.getText().toUpperCase();
		textBox_3.setText(completeMVDs);
		clearError();
		clearOutput();
		if (Relation.isNullOrEmpty(completeRelation)) {
			displayError("Input relation schema is empty.");
			return;
		}
		if (!Relation.schemaContainsSafeChars(completeRelation)) {
			displayError("Input relation schema must only contain letters, commas, and parenthesis.");
			return;
		}
		if (!Relation.schemaContainsParenthesisPair(completeRelation)) {
			displayError("Input relation schema must contain only one pair of properly formatted parenthesis: R( )");
			return;
		}
		relation = new Relation(completeRelation);
		if (relation.getAttributes().isEmpty()) {
			displayError("Input relation schema has no attributes.");
			return;
		}
		if (!Relation.functionalContainsSafeChars(completeFDs)) {
			displayError("Input functional dependencies must only contain letters, commas, semi-colons, hyphens, and greater-than.");
			return;
		}
		if (!Relation.functionalContainsSafeChars(completeMVDs)) {
			displayError("Input multivalued dependencies must only contain letters, commas, semi-colons, hyphens, and greater-than.");
			return;
		}
		if (!relation.hasPassedIntegrityChecks()) {
			displayError(relation.getIntegrityCheckErrorMsg());
			return;
		}
		boolean functionalCheck = Relation.functionalContainsAtLeastOneDependency(completeFDs);
		boolean multivaluedCheck = Relation.functionalContainsAtLeastOneDependency(completeMVDs);
		if (completeMVDs.isEmpty()) {
			multivaluedCheck = true;
		}
		String outputErrorCheck = "";
		if (!functionalCheck) {
			outputErrorCheck += "Warning: encountered a functional dependency that is "
					+ "incomplete or improperly formatted or input functional dependencies is empty. ";
		}
		if (!multivaluedCheck) {
			outputErrorCheck += "Warning: encountered a multivalued dependency that is incomplete or improperly formatted.";
		}
		if (!outputErrorCheck.isEmpty()) {
			displayError(outputErrorCheck);
		}
		relation.addFunctionalDependencies(completeFDs);
		relation.addMultivaluedDependencies(completeMVDs);
		if (!relation.hasPassedIntegrityChecks()) {
			displayError(relation.getIntegrityCheckErrorMsg());
			return;
		}

		// Print out list of attributes
		List<Attribute> attributes = relation.getAttributes();
		String having;
		if (attributes.size() == 1) {
			having = " having attribute: ";
		} else {
			having = " having attributes: ";
		}
		displayOutput("Calculating information for Relation " + relation.getName() + having);
		for (int i = 0; i < attributes.size(); i++) {
			appendOutput(attributes.get(i).getName(), false);
			if (i < attributes.size() - 1) {
				appendOutput(", ", false);
			}
		}
		appendOutput(".", false);
		
		// Print out list of given functional dependencies
		List<FunctionalDependency> fds = relation.getInputFDs();
		if (fds.isEmpty()) {
			appendOutput("No input functional dependencies.", true);
		} else {
			String functional_dependency;
			if (fds.size() == 1) {
				functional_dependency = "functional dependency: ";
			} else {
				functional_dependency = "functional dependencies: ";
			}
			appendOutput("Given input " + functional_dependency, true);
			for (int i = 0; i < fds.size(); i++) {
				appendOutput(fds.get(i).getFDName(), false);
				if (i < fds.size() - 1) {
					appendOutput("; ", false);
				}
			}
			appendOutput(".", false);
		}
		
		// Print out list of given multivalued dependencies
		List<MultivaluedDependency> mvds = relation.getMVDs();
		if (mvds.isEmpty()) {
			appendOutput("No input multivalued dependencies.", true);
		} else {
			String multivalued_dependency;
			if (mvds.size() == 1) {
				multivalued_dependency = "multivalued dependency: ";
			} else {
				multivalued_dependency = "multivalued dependencies: ";
			}
			appendOutput("Given input " + multivalued_dependency, true);
			for (int i = 0; i < mvds.size(); i++) {
				appendOutput(mvds.get(i).getName(), false);
				if (i < mvds.size() - 1) {
					appendOutput("; ", false);
				}
			}
			appendOutput(".", false);
		}
		
		
		// Print out closure of given attributes and keys
		appendMajorBreak();
		CalculateClosure.improvedCalculateClosures(relation);
		appendOutput("Calculating attribute closures: ", true);
		List<Closure> calculatedClosures = relation.getClosures();
		List<Closure> minimumKeys = relation.getMinimumKeyClosures();
		List<Closure> superKeys = relation.getSuperKeyClosures();
		for (Closure closure : calculatedClosures) {
			List<Attribute> left = closure.getClosureOf();
			List<Attribute> right = closure.getClosure();
			appendOutput("{", true);
			for (int i = 0; i < left.size(); i++) {
				appendOutput(left.get(i).getName(), false);
				if (i < left.size() - 1) {
					appendOutput(", ", false);
				}
			}
			appendOutput("}+ = {", false);
			for (int i = 0; i < right.size(); i++) {
				appendOutput(right.get(i).getName(), false);
				if (i < right.size() - 1) {
					appendOutput(", ", false);
				}
			}
			appendOutput("}", false);
			if (minimumKeys.contains(closure)) {
				if (closure.getClosureOf().size() == 1) {
					appendOutput(" " + RDTUtils.LONG_LEFTWARDS_ARROW + " Minimum candidate key", false);
				} else {
					appendOutput(" " + RDTUtils.LONG_LEFTWARDS_ARROW + " Composite minimum candidate key", false);
				}
			}
			if (superKeys.contains(closure)) {
				appendOutput(" " + RDTUtils.LONG_LEFTWARDS_ARROW + " Superkey", false);
			}
		}
		

		// Print out number of minimum candidate keys and superkeys
		appendMajorBreak();
		int minKeys = relation.getMinimumKeyClosures().size();
		int minCompKeys = 0;
		for (Closure c : relation.getMinimumKeyClosures()) {
			if (c.getClosureOf().size() > 1) {
				minCompKeys++;
			}
		}
		if (minKeys == 1) {
			if (minCompKeys == 0) {
				appendOutput("Found 1 minimum candidate key. There are no composite minimum candidate keys.", true);
			} else {
				appendOutput("Found 1 composite minimum candidate key. There are no non-composite minimum candidate keys.", true);
			}
		} else {
			appendOutput("Found " + relation.getMinimumKeyClosures().size() + " minimum candidate keys, ", true);
			if (minCompKeys == 1) {
				appendOutput("of which 1 is a composite minimum candidate key.", false);
			} else {
				appendOutput("of which " + minCompKeys + " are composite minimum candidate keys.", false);
			}
		}
		int numSuperKeys = relation.getSuperKeyClosures().size();
		if (numSuperKeys == 1) {
			appendOutput("Found 1 superkey (excluding minimum candidate keys).", true);
		} else {
			appendOutput("Found " + relation.getSuperKeyClosures().size() + " superkeys (excluding minimum candidate keys).", true);
		}
		

		// Print out prime and non-prime attributes
		appendMajorBreak();
		appendOutput("List of prime attributes (attributes that are part of a minimum candidate key): ", true);
		List<Attribute> primes = relation.getPrimeAttributes();
		for (int i = 0; i < primes.size(); i++) {
			appendOutput(primes.get(i).getName(), false);
			if (i < primes.size() - 1) {
				appendOutput(", ", false);
			}
		}
		appendOutput(".", false);
		appendOutput("List of non-prime attributes (attributes that are not part of any minimum candidate key): ", true);
		List<Attribute> nonPrimes = relation.getNonPrimeAttributes();
		if (nonPrimes.isEmpty()) {
			appendOutput("(none).", false);
		} else {
			for (int i = 0; i < nonPrimes.size(); i++) {
				appendOutput(nonPrimes.get(i).getName(), false);
				if (i < nonPrimes.size() - 1) {
					appendOutput(", ", false);
				}
			}
			appendOutput(".", false);
		}
		

		// Print out minimal cover of functional dependencies
		appendMajorBreak();
		MinimalFDCover.determineMinimalCover(relation);
		appendOutput("Calculating a minimal cover set (F_min) of functional dependencies from given input: (note that functional dependencies with common left-hand sides have their right-hand sides combined)", true);
		List<FunctionalDependency> minimalCover = relation.getMinimalCover();
		if (minimalCover.isEmpty()) {
			appendOutput("There are no functional dependencies in the minimal cover set.", true);
		} else {
			appendOutput("F_min = { ", true);
			for (int i = 0; i < minimalCover.size(); i++) {
				appendOutput(minimalCover.get(i).getFDName(), false);
				if (i < minimalCover.size() - 1) {
					appendOutput("; ", false);
				}
			}
			appendOutput(" }", false);
		}
		List<FunctionalDependency> givenToMinCoverLostFDs = relation.getGivenToMinCoverLostFDs();
		if (givenToMinCoverLostFDs.isEmpty()) {
			appendOutput("All input functional dependencies were included in the minimal cover set.", true);
		} else {
			boolean showRemovalNote = false;
			if (givenToMinCoverLostFDs.size() == 1) {
				appendOutput("The following input functional dependency was not included in the minimal cover set: ", true);
			} else {
				appendOutput("The following input functional dependencies were not included in the minimal cover set: ", true);
			}
			for (int i = 0; i < givenToMinCoverLostFDs.size(); i++) {
				FunctionalDependency fuD = givenToMinCoverLostFDs.get(i);
				if (!showRemovalNote && fuD.getLeftHandAttributes().size() > 1) {
					showRemovalNote = true;
				}
				appendOutput(fuD.getFDName(), false);
				if (i < givenToMinCoverLostFDs.size() - 1) {
					appendOutput("; ", false);
				}
			}
			appendOutput(".", false);
			if (showRemovalNote) {
				appendOutput("Note that an excluded input functional dependency having more than one attribute on its left-hand side might still have part of its left-hand side (the part that is part of the minimal cover) included.", true);
			}
		}
		

		// Print out derived functional dependencies
		appendMajorBreak();
		CalculateFDs.calculateDerivedFDs(relation);
		appendOutput("Calculating complete set of non-trivial functional dependencies based on the given ones: ", true);
		List<FunctionalDependency> derivedFDs = relation.getDerivedFDs();
		if (derivedFDs.isEmpty()) {
			appendOutput("There are no new functional dependencies aside from the pre-existing ones.", true);
		} else {
			appendOutput("Full set of non-trivial functional dependencies: ", true);
			for (int i = 0; i < derivedFDs.size(); i++) {
				appendOutput(derivedFDs.get(i).getFDName(), true);
			}
		}
		

		// Display normal forms
		appendMajorBreak();
		appendOutput("Determining highest normal form of relation: ", true);
		relation.determineNormalForms();
		DetermineNormalForms normalForms = relation.getNormalFormsResults();
		appendOutput(normalForms.getFirstNormalFormMsg(), true);
		appendMinorBreak();
		appendOutput(normalForms.getSecondNormalFormMsg(), true);
		appendMinorBreak();
		appendOutput(normalForms.getThirdNormalFormMsg(), true);
		appendMinorBreak();
		appendOutput(normalForms.getBCNFMsg(), true);
		appendMinorBreak();
		appendOutput(normalForms.getFourthNormalFormMsg(), true);
		
		
		// Output 3NF decomposition
		appendMajorBreak();
		appendOutput("Decomposing input relation into 3NF using canonical functional dependency cover (lossless and preserving all minimal cover set functional dependencies): ", true);
		Calculate3NFDecomposition threeNF = new Calculate3NFDecomposition(relation);
		if (normalForms.isIn3NF()) {
			appendOutput("Input relation is already in 3NF. No decomposition necessary. ", true);
		} else {
			threeNF.decompose();
			appendOutput(threeNF.getOutputMsg(), true);
			List<Relation> output3NFRelations = threeNF.getOutputRelations();
			for (Relation r : output3NFRelations) {
				appendOutput(r.printRelation(), true);
			}
		}
		
		
		// Output BCNF decomposition
		appendMajorBreak();
		appendOutput("Decomposing input relation into BCNF relations (lossless but not necessarily functional dependency preserving). Will attempt two parallel decompositions: one from the input relation and the second from the set of decomposed 3NF relations: ", true);
		appendMinorBreak();
		if (normalForms.isInBCNF()) {
			appendOutput("Input relation is already in BCNF. No decomposition necessary. ", true);
		} else {
			if (normalForms.isIn3NF()) {
				threeNF.decompose();
			}
			CalculateBCNFDecomposition bcnf = new CalculateBCNFDecomposition(threeNF);
			bcnf.decompose();
			
			// Start with input relation source BCNF decomposition
			appendOutput("Decomposing input relation into BCNF relations using input relation as source. ", true);
			if (bcnf.getBcnfDecomposedWithDuplicates().size() == bcnf.getPureBCNFDecomposedRs().size()) {
				appendOutput("Final set of decomposed BCNF relations: ", true);
				for (Relation r : bcnf.getPureBCNFDecomposedRs()) {
					appendOutput(r.printRelation(), true);
				}
			} else {
				appendOutput("Initial set of decomposed BCNF relations: ", true);
				for (Relation r : bcnf.getBcnfDecomposedWithDuplicates()) {
					appendOutput(r.printRelation(), true);
				}
				appendOutput("Final set of decomposed BCNF relations (removing duplicate and subset relations): ", true);
				for (Relation r : bcnf.getPureBCNFDecomposedRs()) {
					appendOutput(r.printRelation(), true);
				}
			}
			List<FunctionalDependency> pureBCNFLostFDs = bcnf.getPureBCNFLostFDs();
			if (pureBCNFLostFDs.isEmpty()) {
				appendOutput("No input functional dependencies were lost.", true);
			} else {
				appendOutput("The following input functional dependencies were lost (note that a lost input functional dependency can be safely ignored if it is not part of the minimal cover set of functional dependencies): ", true);
				for (int i = 0; i < pureBCNFLostFDs.size(); i++) {
					appendOutput(pureBCNFLostFDs.get(i).getFDName(), false);
					if (i < pureBCNFLostFDs.size() - 1) {
						appendOutput("; ", false);
					}
				}
				appendOutput(".", false);
			}
			
			appendMinorBreak();
			
			// Next display 3NF relation source BCNF decomposition
			appendOutput("Decomposing input relation into BCNF relations using decomposed 3NF relations as sources. ", true);
			if (bcnf.getThreeNFDecomposedWithDuplicates().size() == bcnf.getThreeNFDecomposedRs().size()) {
				appendOutput("Final set of decomposed BCNF relations: ", true);
				for (Relation r : bcnf.getThreeNFDecomposedRs()) {
					appendOutput(r.printRelation(), true);
				}
			} else {
				appendOutput("Initial set of decomposed BCNF relations: ", true);
				for (Relation r : bcnf.getThreeNFDecomposedWithDuplicates()) {
					appendOutput(r.printRelation(), true);
				}
				appendOutput("Final set of decomposed BCNF relations (removing duplicate and subset relations): ", true);
				for (Relation r : bcnf.getThreeNFDecomposedRs()) {
					appendOutput(r.printRelation(), true);
				}
			}
			List<FunctionalDependency> threeNFLostFDs = bcnf.getThreeNFLostFDs();
			if (threeNFLostFDs.isEmpty()) {
				appendOutput("No functional dependencies from the minimal cover set were lost.", true);
			} else {
				appendOutput("The following minimal cover set functional dependencies were lost: ", true);
				for (int i = 0; i < pureBCNFLostFDs.size(); i++) {
					appendOutput(pureBCNFLostFDs.get(i).getFDName(), false);
					if (i < pureBCNFLostFDs.size() - 1) {
						appendOutput("; ", false);
					}
				}
				appendOutput(".", false);
			}
			
			appendOutput(bcnf.getOutputMsg(), true);
			List<Relation> resultsWithDuplicates = bcnf.getResultWithDuplicates();
			List<Relation> outputBCNFRelations = bcnf.getOutputRelations();
			if (resultsWithDuplicates.size() == outputBCNFRelations.size()) {
				for (Relation r : outputBCNFRelations) {
					appendOutput(r.printRelation(), true);
				}
			} else {
				appendOutput("Initial set of decomposed BCNF relations: ", true);
				for (Relation r : resultsWithDuplicates) {
					appendOutput(r.printRelation(), true);
				}
				appendOutput("Final set of decomposed BCNF relations (removing duplicate and subset relations): ", true);
				for (Relation r : outputBCNFRelations) {
					appendOutput(r.printRelation(), true);
				}
			}
			
		}
		
	}
	
	private void displayOutput(final String output) {
		clearOutput();
		appendOutput(output, false);
	}
	
	private void appendOutput(final String output, final boolean newLine) {
		if (newLine) {
			Label nextLabel = new Label(output);
			outputs.add(nextLabel);
			outputPanel.add(nextLabel);
			return;
		} else {
			Label lastLabel = outputs.get(outputs.size()-1);
			String existingOutput = lastLabel.getText();
			lastLabel.setText(existingOutput + output);
		}
	}
	
	private void appendOutput(final Label label) {
		outputs.add(label);
		outputPanel.add(label);
		return;
	}
	
	private void appendMajorBreak() {
		appendOutput(new HTML("<br>"));
		appendOutput("------------------------------", true);
	}
	
	private void appendMinorBreak() {
		appendOutput(new HTML("<br>"));
	}
	
	private void clearOutput() {
		for (Label label : outputs) {
			label.setText("");
		}
		for (int i = outputs.size()-1; i >= 1; i--) {
			outputPanel.remove(outputs.get(i));
			outputs.remove(i);
		}
	}
	
	private void displayError(final String error) {
		errorLabel.setVisible(true);
		errorLabel.setText(error);
	}
	
	private void clearError() {
		errorLabel.setText("");
		errorLabel.setVisible(false);
	}
	
	private void clearInput() {
		textBox_1.setText("");
		textBox_2.setText("");
		textBox_3.setText("");
	}
	
	private void resetDisplay() {
		clearInput();
		clearOutput();
		clearError();
	}
}
