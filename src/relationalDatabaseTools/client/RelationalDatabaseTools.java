package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RelationalDatabaseTools implements EntryPoint {
	
	private final VerticalPanel mainPanel = new VerticalPanel();
	private final HorizontalPanel panel_1 = new HorizontalPanel();
	private final HorizontalPanel panel_2 = new HorizontalPanel();
	private final HorizontalPanel panel_3 = new HorizontalPanel();
	
	private final Label label_1 = new Label("Enter the relation schema in form R(A,B,C,AB,ABC)");
	private final Label label_1b = new Label("Use commas to separate attributes. Spaces are optional. Inputs are case-insensitive.");
	private final TextBox textBox_1 = new TextBox();
	
	private final Label label_2 = new Label("Enter all given functional dependencies in form A -> B; AB -> C; B,C -> A");
	private final Label label_2b = new Label("Use commas to separate attributes that belong to the same side of the same functional dependency.");
	private final Label label_2c = new Label("Use semi-colons to separate different functional dependencies.");
	private final TextBox textBox_2 = new TextBox();
	private final VerticalPanel secondaryFDPanel = new VerticalPanel();
	
	private final Button button_1 = new Button("Calculate");
	private final Button resetButton = new Button("Reset");
	
	private Label outputLabel = new Label();
	private final Label errorLabel = new Label();
	
	private final List<Label> outputs = new ArrayList<>();
	
	private Relation relation;
	private String schemaInput;
	private String fdInput;
	
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
		secondaryFDPanel.addStyleName("panels");
		
		panel_2.add(label_2);
		panel_2.add(textBox_2);
		textBox_2.addStyleName("textboxes");
		panel_2.add(secondaryFDPanel);
		panel_2.addStyleName("panels");
		
		panel_3.add(button_1);
		button_1.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				calculate();
			}
		});
		panel_3.addStyleName("panels");
		
		mainPanel.add(resetButton);
		mainPanel.add(panel_1);
		mainPanel.add(panel_2);
		mainPanel.add(panel_3);
		mainPanel.add(errorLabel);
		mainPanel.add(outputLabel);
		outputs.add(outputLabel);
		
		errorLabel.addStyleName("errorLabel");
		mainPanel.addStyleName("panels");
		
		RootPanel.get("relationalTools").add(mainPanel);
		relation = null;
		schemaInput = null;
		fdInput = null;
	}

	private void calculate() {
		String completeRelation = textBox_1.getText().toUpperCase();
		textBox_1.setText(completeRelation);
		String completeFDs = textBox_2.getText().toUpperCase();
		textBox_2.setText(completeFDs);
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
			displayError("Input relation schema must contain only one pair of parenthesis.");
			return;
		}
		boolean recalculate = false;
		if (relation == null || !schemaInput.equals(completeRelation) || !fdInput.equals(completeFDs)) {
			relation = new Relation(completeRelation);
			recalculate = true;
		}
		if (relation.getAttributes().isEmpty()) {
			displayError("Input relation schema has no attributes.");
			return;
		}
		if (!Relation.functionalContainsSafeChars(completeFDs)) {
			displayError("Input functional dependencies must only contain letters, commas, semi-colons, hyphens, and greater-than.");
			return;
		}
		if (!relation.hasPassedIntegrityChecks()) {
			displayError(relation.getIntegrityCheckErrorMsg());
			return;
		}
		if (!Relation.functionalContainsAtLeastOneDependency(completeFDs)) {
			displayError("Warning: encountered a functional dependency that is "
					+ "incomplete or improperly formatted or input functional dependencies is empty.");
		}
		if (recalculate) {
			relation.addFunctionalDependencies(completeFDs);
		}
		if (!relation.hasPassedIntegrityChecks()) {
			displayError(relation.getIntegrityCheckErrorMsg());
			return;
		}
		schemaInput = completeRelation;
		fdInput = completeFDs;
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
		
		
		appendOutput("---------------", true);
		// Print out closure of given attributes and keys
		if (recalculate) {
			CalculateClosure.improvedCalculateClosures(relation);
		}
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
					appendOutput(" <--- Minimum candidate key", false);
				} else {
					appendOutput(" <--- Composite minimum candidate key", false);
				}
			}
			if (superKeys.contains(closure)) {
				appendOutput(" <--- Superkey", false);
			}
		}
		
		appendOutput("---------------", true);
		// Print out number of minimum candidate keys and superkeys
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
		
		appendOutput("---------------", true);
		// Print out prime and non-prime attributes
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
		
		appendOutput("---------------", true);
		// Print out minimal cover of functional dependencies
		if (recalculate) {
			MinimalFDCover.determineMinimalCover(relation);
		}
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
		
		appendOutput("---------------", true);
		// Print out derived functional dependencies
		if (recalculate) {
			CalculateFDs.calculateDerivedFDs(relation);
		}
		appendOutput("Calculating non-trivial single-attribute right-hand side "
				+ "functional dependencies that follow from the given ones: "
				+ "(note that input functional dependencies with more than one right-hand side attribute "
				+ "can be split apart and those split functional dependencies therefore may count but are not included here)", true);
		List<FunctionalDependency> derivedFDs = relation.getDerivedFDs();
		if (derivedFDs.isEmpty()) {
			appendOutput("There are no new functional dependencies aside from the pre-existing ones.", true);
		} else {
			String dependency;
			if (derivedFDs.size() == 1) {
				dependency = "dependency: ";
			} else {
				dependency = "dependencies: ";
			}
			appendOutput("New derived functional " + dependency, true);
			for (int i = 0; i < derivedFDs.size(); i++) {
				appendOutput(derivedFDs.get(i).getFDName(), false);
				if (i < derivedFDs.size() - 1) {
					appendOutput("; ", false);
				}
			}
		}
		
		appendOutput("---------------", true);
		// Display normal forms
		appendOutput("Determining highest normal form of relation: ", true);
		DetermineNormalForms normalForms = new DetermineNormalForms(relation);
		normalForms.calculateNormalForms();
		appendOutput(normalForms.getFirstNormalFormMsg(), true);
		appendOutput(normalForms.getSecondNormalFormMsg(), true);
		appendOutput(normalForms.getThirdNormalFormMsg(), true);
		appendOutput(normalForms.getBCNFMsg(), true);

	}
	
	private void displayOutput(final String output) {
		clearOutput();
		appendOutput(output, false);
	}
	
	private void appendOutput(final String output, final boolean newLine) {
		if (newLine) {
			Label nextLabel = new Label(output);
			outputs.add(nextLabel);
			mainPanel.add(nextLabel);
			return;
		} else {
			Label lastLabel = outputs.get(outputs.size()-1);
			String existingOutput = lastLabel.getText();
			lastLabel.setText(existingOutput + output);
		}
	}
	
	private void clearOutput() {
		for (Label label : outputs) {
			label.setText("");
		}
		for (int i = outputs.size()-1; i >= 1; i--) {
			mainPanel.remove(outputs.get(i));
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
	}
	
	private void resetDisplay() {
		clearInput();
		clearOutput();
		clearError();
	}
}
