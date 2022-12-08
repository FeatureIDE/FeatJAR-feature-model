/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/model> for further information.
 */
package de.featjar.model.io.xml;

import de.featjar.formula.io.xml.AbstractXMLFeatureModelFormat;
import de.featjar.formula.structure.Formula;
import de.featjar.formula.structure.atomic.literal.VariableMap;
import de.featjar.model.*;
import de.featjar.model.Constraint;
import de.featjar.model.Feature;
import de.featjar.model.FeatureModel;
import de.featjar.model.FeatureOrder;
import de.featjar.model.io.AttributeIO;
import de.featjar.model.util.Identifier;
import de.featjar.util.data.Problem;
import de.featjar.util.data.Result;
import de.featjar.util.io.InputMapper;
import de.featjar.util.io.format.ParseException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Parses and writes feature models from and to FeatureIDE XML files.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class XMLFeatureModelFormat extends AbstractXMLFeatureModelFormat<FeatureModel, Feature, Constraint> {
    public static final String NAMESPACE = XMLFeatureModelFormat.class.getCanonicalName();
    public static final String GRAPHICS_NAMESPACE = "<graphics>"; // todo
    public static final String CALCULATIONS_NAMESPACE = "<calculations>"; // todo
    protected static final String COMMENTS = "comments";
    protected static final String C = "c";
    protected static final String FEATURE_ORDER = "featureOrder";
    protected static final String USER_DEFINED = "userDefined";
    protected static final String KEY = "key";
    protected static final String VALUE = "value";
    protected static final String DATA_TYPE = "data-type";
    protected static final String NAMESPACE_TAG = "namespace";

    protected static final String PROPERTIES = "properties";
    protected static final String CALCULATIONS = "calculations";
    protected static final String CALCULATE_FEATURES = "Features";
    protected static final String CALCULATE_REDUNDANT = "Redundant";
    protected static final String CALCULATE_TAUTOLOGY = "Tautology";
    protected static final String CALCULATE_CONSTRAINTS = "Constraints";
    protected static final String CALCULATE_AUTO = "Auto";
    protected FeatureModel featureModel;
    protected Map<String, Identifier> nameToIdentifierMap;
    private VariableMap variableMap; // todo remove in favor of FeatureModel.variableMap

    @Override
    public XMLFeatureModelFormat getInstance() {
        return new XMLFeatureModelFormat();
    }

    @Override
    public String getName() {
        return "FeatureIDE";
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsSerialize() {
        return true;
    }

    @Override
    public Result<FeatureModel> parse(InputMapper inputMapper, Supplier<FeatureModel> supplier) {
        featureModel = supplier.get();
        return parse(inputMapper);
    }

    @Override
    public FeatureModel parseDocument(Document document) throws ParseException {
        if (featureModel == null) featureModel = new FeatureModel(Identifier.newCounter());
        variableMap = new VariableMap();
        nameToIdentifierMap = new HashMap<>();
        final Element featureModelElement = getDocumentElement(document, FEATURE_MODEL);
        parseFeatureTree(getElement(featureModelElement, STRUCT));
        Optional<Element> element = getOptionalElement(featureModelElement, CONSTRAINTS);
        if (element.isPresent()) parseConstraints(element.get(), variableMap);
        element = getOptionalElement(featureModelElement, COMMENTS);
        if (element.isPresent()) parseComments(element.get());
        element = getOptionalElement(featureModelElement, FEATURE_ORDER);
        if (element.isPresent()) parseFeatureOrder(List.of(element.get()));
        element = getOptionalElement(featureModelElement, PROPERTIES);
        if (element.isPresent()) parseFeatureModelProperties(element.get());
        element = getOptionalElement(featureModelElement, CALCULATIONS);
        element.ifPresent(this::parseCalculations);
        return featureModel;
    }

    protected Optional<Feature> getFeature(String name) {
        return Optional.ofNullable(nameToIdentifierMap.get(name)).flatMap(featureModel::getFeature);
    }

    protected Optional<VariableMap.Variable> getVariable(String name) {
        return variableMap.getVariable(Optional.ofNullable(nameToIdentifierMap.get(name))
                .map(Identifier::toString)
                .orElse(""));
    }

    @Override
    public Pattern getInputHeaderPattern() {
        return inputHeaderPattern;
    }

    @Override
    protected Feature createFeatureLabel(
            String name, Feature parentFeatureLabel, boolean mandatory, boolean _abstract, boolean hidden)
            throws ParseException {
        Feature feature;
        if (parentFeatureLabel == null) {
            feature = featureModel.getRootFeature();
        } else {
            feature = parentFeatureLabel.mutate().createFeatureBelow();
        }
        feature.mutate(mutator -> {
            mutator.setName(name);
            mutator.setAbstract(_abstract);
            mutator.setHidden(hidden);
        });
        feature.getFeatureTree().mutate().setMandatory(mandatory);
        if (nameToIdentifierMap.get(name) != null) {
            throw new ParseException("Duplicate feature name!");
        }
        nameToIdentifierMap.put(name, feature.getIdentifier());
        variableMap.addBooleanVariable(feature.getIdentifier().toString());
        return feature;
    }

    @Override
    protected void addAndGroup(Feature featureLabel, List<Feature> childFeatureLabels) {
        featureLabel.getFeatureTree().mutate().setAnd();
    }

    @Override
    protected void addOrGroup(Feature featureLabel, List<Feature> childFeatureLabels) {
        featureLabel.getFeatureTree().mutate().setOr();
    }

    @Override
    protected void addAlternativeGroup(Feature featureLabel, List<Feature> childFeatureLabels) {
        featureLabel.getFeatureTree().mutate().setAlternative();
    }

    @Override
    protected void addFeatureMetadata(Feature featureLabel, Element e) throws ParseException {
        switch (e.getNodeName()) {
            case DESCRIPTION:
                featureLabel.mutate().setDescription(getDescription(e));
                break;
            case GRAPHICS:
                parseProperty(featureLabel, e, GRAPHICS_NAMESPACE);
                break;
            case PROPERTY:
                parseProperty(featureLabel, e, NAMESPACE);
                break;
        }
    }

    @Override
    protected Constraint createConstraintLabel() {
        return featureModel.mutate().createConstraint();
    }

    @Override
    protected void addConstraint(Constraint constraintLabel, Formula formula) {
        constraintLabel.mutate().setFormula(formula);
    }

    @Override
    protected void addConstraintMetadata(Constraint constraintLabel, Element e) throws ParseException {
        switch (e.getNodeName()) {
            case DESCRIPTION:
                constraintLabel.mutate().setDescription(getDescription(e));
                break;
            case GRAPHICS:
                parseProperty(constraintLabel, e, GRAPHICS_NAMESPACE);
                break;
            case PROPERTY:
                parseProperty(constraintLabel, e, NAMESPACE);
                break;
            case TAGS:
                constraintLabel.mutate().setTags(getTags(e));
        }
    }

    protected void parseComments(Element element) throws ParseException {
        for (final Element e1 : getElements(element.getChildNodes())) {
            if (e1.getNodeName().equals(C)) {
                featureModel
                        .mutate()
                        .setDescription(featureModel.getDescription().orElse("") + "\n" + e1.getTextContent());
            } else {
                addParseProblem("Unknown comment attribute: " + e1.getNodeName(), e1, Problem.Severity.WARNING);
            }
        }
    }

    protected void parseFeatureOrder(List<Element> elements) throws ParseException {
        final ArrayList<String> order = new ArrayList<>(featureModel.getNumberOfFeatures());
        boolean userDefined = false;
        for (final Element e : elements) {
            if (e.hasAttributes()) {
                final NamedNodeMap nodeMap = e.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    final Node node = nodeMap.item(i);
                    final String attributeName = node.getNodeName();
                    final String attributeValue = node.getNodeValue();
                    if (attributeName.equals(USER_DEFINED)) {
                        userDefined = attributeValue.equals(TRUE);
                    } else if (attributeName.equals(NAME)) {
                        if (getFeature(attributeValue).isPresent()) {
                            order.add(attributeValue);
                        } else {
                            addParseProblem(
                                    "Feature \"" + attributeValue + "\" does not exists", e, Problem.Severity.ERROR);
                        }
                    } else {
                        addParseProblem("Unknown feature order attribute: " + attributeName, e, Problem.Severity.ERROR);
                    }
                }
            }
            if (e.hasChildNodes()) {
                parseFeatureOrder(getElements(e.getChildNodes()));
            }
        }
        if (!order.isEmpty()) {
            List<Feature> featureList = order.stream()
                    .map(nameToIdentifierMap::get)
                    .map(featureModel::getFeature)
                    .map(Optional::orElseThrow)
                    .collect(Collectors.toList());
            featureModel.mutate().setFeatureOrder(FeatureOrder.ofList(featureList));
        }
        featureModel.getFeatureOrder().mutate().setUserDefined(userDefined);
    }

    protected String getDescription(Node e) {
        String description = e.getTextContent();
        // NOTE: THe following code is used for backwards compatibility. It replaces
        // spaces and tabs that were added to the XML for indentation, but don't
        // belong to the actual description.
        if (description != null) {
            description = description.replaceAll("(\r\n|\r|\n)\\s*", "\n").replaceAll("\\A\n|\n\\Z", "");
        }
        return description;
    }

    protected Set<String> getTags(final Node e) {
        final String[] tagArray = e.getTextContent().split(",");
        return new HashSet<>(Arrays.asList(tagArray));
    }

    protected void parseProperty(de.featjar.model.Element element, Element e, String fallbackNamespace)
            throws ParseException {
        if (!e.hasAttribute(KEY) || !e.hasAttribute(VALUE)) {
            addParseProblem(
                    "Missing one of the required attributes: " + KEY + " or " + VALUE, e, Problem.Severity.WARNING);
        } else {
            String typeString = e.hasAttribute(DATA_TYPE) ? e.getAttribute(DATA_TYPE) : "string";
            final String namespace = e.hasAttribute(NAMESPACE_TAG) ? e.getAttribute(NAMESPACE_TAG) : fallbackNamespace;
            final String name = e.getAttribute(KEY);
            final String valueString = e.getAttribute(VALUE);
            parseProblems.addAll(
                    AttributeIO.parseAndSetAttributeValue(element, namespace, name, typeString, valueString));
        }
    }

    protected void parseFeatureModelProperties(Element e) throws ParseException {
        for (final Element propertyElement : getElements(e.getChildNodes())) {
            final String nodeName = propertyElement.getNodeName();
            switch (nodeName) {
                case GRAPHICS:
                    parseProperty(featureModel, propertyElement, GRAPHICS_NAMESPACE);
                    break;
                case CALCULATIONS:
                    parseProperty(featureModel, propertyElement, CALCULATIONS_NAMESPACE);
                    break;
                case PROPERTY:
                    parseProperty(featureModel, propertyElement, NAMESPACE);
                    break;
            }
        }
    }

    protected void parseCalculations(Element e) {
        parseAttribute(e, CALCULATE_AUTO);
        parseAttribute(e, CALCULATE_CONSTRAINTS);
        parseAttribute(e, CALCULATE_FEATURES);
        parseAttribute(e, CALCULATE_REDUNDANT);
        parseAttribute(e, CALCULATE_TAUTOLOGY);
    }

    private void parseAttribute(final Element e, final String key) {
        if (e.hasAttribute(key)) {
            parseProblems.addAll(AttributeIO.parseAndSetAttributeValue(
                    featureModel, CALCULATIONS_NAMESPACE, key, "bool", e.getAttribute(key)));
        }
    }

    @Override
    public void writeDocument(FeatureModel featureModel, Document doc) {
        throw new UnsupportedOperationException();
    }

    //	@Override
    //	public void writeDocument(FeatureModel featureModel, Document doc) {
    //		this.featureModel = featureModel;
    //		nameToIdentifierMap = new HashMap<>();
    //		final Element root = doc.createElement(FEATURE_MODEL);
    //		doc.appendChild(root);
    //
    //		writeProperties(doc, root);
    //		writeFeatures(doc, root);
    //		writeConstraints(doc, root);
    //		writeComments(doc, root);
    //		writeFeatureOrder(doc, root);
    //	}
    //
    //
    //	protected void writeProperties(Document doc, final Element root) {
    //		if (!featureModel.getAttributeToValueMap().isEmpty()) {
    //			final Element properties = doc.createElement(PROPERTIES);
    //			root.appendChild(properties);
    //			addProperties(doc, featureModel, properties);
    //		}
    //	}
    //
    //	protected void writeFeatures(Document doc, final Element root) {
    //		final Element struct = doc.createElement(STRUCT);
    //		root.appendChild(struct);
    //		writeFeatureTreeRec(doc, struct, FeatureUtils.getRoot(object));
    //	}
    //
    //	protected void writeConstraints(Document doc, final Element root) {
    //		if (!featureModel.getConstraints().isEmpty()) {
    //			final Element constraints = doc.createElement(CONSTRAINTS);
    //			root.appendChild(constraints);
    //			for (final IConstraint constraint : featureModel.getConstraints()) {
    //				Element rule;
    //				rule = doc.createElement(RULE);
    //
    //				constraints.appendChild(rule);
    //				addDescription(doc, constraint.getDescription(), rule);
    //				addProperties(doc, constraint.getCustomProperties(), rule);
    //				addTags(doc, constraint.getTags(), rule);
    //				createPropositionalConstraints(doc, rule, constraint.getNode());
    //			}
    //		}
    //	}
    ////
    ////	protected void writeComments(Document doc, final Element root) {
    ////		if (!featureModel.getProperty().getComments().isEmpty()) {
    ////			final Element comments = doc.createElement(COMMENTS);
    ////			root.appendChild(comments);
    ////			for (final String comment : featureModel.getProperty().getComments()) {
    ////				final Element c = doc.createElement(C);
    ////				comments.appendChild(c);
    ////				final Text text = doc.createTextNode(comment);
    ////				c.appendChild(text);
    ////			}
    ////		}
    ////	}
    ////
    ////	protected void writeFeatureOrder(Document doc, final Element root) {
    ////		if (featureModel.isFeatureOrderUserDefined()) {
    ////			final Element order = doc.createElement(FEATURE_ORDER);
    ////			order.setAttribute(USER_DEFINED, Boolean.toString(featureModel.isFeatureOrderUserDefined()));
    ////			root.appendChild(order);
    ////			Collection<String> featureOrderList = featureModel.getFeatureOrderList();
    ////
    ////			if (featureOrderList.isEmpty()) {
    ////				featureOrderList = FeatureUtils.extractConcreteFeaturesAsStringList(object);
    ////			}
    ////
    ////			for (final String featureName : featureOrderList) {
    ////				final Element feature = doc.createElement(FEATURE);
    ////				feature.setAttribute(NAME, featureName);
    ////				order.appendChild(feature);
    ////			}
    ////		}
    ////	}
    ////
    ////	/**
    ////	 * Inserts the tags concerning propositional constraints into the DOM document representation
    ////	 *
    ////	 * @param doc
    ////	 * @param node Parent node for the propositional nodes
    ////	 */
    ////	protected void createPropositionalConstraints(Document doc, Element xmlNode, org.prop4j.Node node) {
    ////		if (node == null) {
    ////			return;
    ////		}
    ////
    ////		final Element op;
    ////		if (node instanceof Literal) {
    ////			final Literal literal = (Literal) node;
    ////			if (!literal.positive) {
    ////				final Element opNot = doc.createElement(NOT);
    ////				xmlNode.appendChild(opNot);
    ////				xmlNode = opNot;
    ////			}
    ////			op = doc.createElement(VAR);
    ////			op.appendChild(doc.createTextNode(String.valueOf(literal.var)));
    ////			xmlNode.appendChild(op);
    ////			return;
    ////		} else if (node instanceof Or) {
    ////			op = doc.createElement(DISJ);
    ////		} else if (node instanceof Equals) {
    ////			op = doc.createElement(EQ);
    ////		} else if (node instanceof Implies) {
    ////			op = doc.createElement(IMP);
    ////		} else if (node instanceof And) {
    ////			op = doc.createElement(CONJ);
    ////		} else if (node instanceof Not) {
    ////			op = doc.createElement(NOT);
    ////		} else if (node instanceof AtMost) {
    ////			op = doc.createElement(ATMOST1);
    ////		} else {
    ////			op = doc.createElement(UNKNOWN);
    ////		}
    ////		xmlNode.appendChild(op);
    ////
    ////		for (final org.prop4j.Node child : node.getChildren()) {
    ////			createPropositionalConstraints(doc, op, child);
    ////		}
    ////	}
    ////
    ////	/**
    ////	 * Creates document based on feature model step by step
    ////	 *
    ////	 * @param doc document to write
    ////	 * @param node parent node
    ////	 * @param feat current feature
    ////	 */
    ////	protected void writeFeatureTreeRec(Document doc, Element node, IFeature feat) {
    ////		if (feat == null) {
    ////			return;
    ////		}
    ////
    ////		final List<IFeature> children = FeatureUtils.convertToFeatureList(feat.getStructure().getChildren());
    ////
    ////		final Element fnod;
    ////		if (children.isEmpty()) {
    ////			fnod = doc.createElement(FEATURE);
    ////			writeFeatureProperties(doc, node, feat, fnod);
    ////		} else {
    ////			if (feat.getStructure().isAnd()) {
    ////				fnod = doc.createElement(AND);
    ////			} else if (feat.getStructure().isOr()) {
    ////				fnod = doc.createElement(OR);
    ////			} else if (feat.getStructure().isAlternative()) {
    ////				fnod = doc.createElement(ALT);
    ////			} else {
    ////				fnod = doc.createElement(UNKNOWN);// Logger.logInfo("creatXMlDockRec: Unexpected error!");
    ////			}
    ////
    ////			writeFeatureProperties(doc, node, feat, fnod);
    ////
    ////			for (final IFeature feature : children) {
    ////				writeFeatureTreeRec(doc, fnod, feature);
    ////			}
    ////
    ////		}
    ////
    ////	}
    ////
    ////	protected void writeFeatureProperties(Document doc, Element node, IFeature feat, final Element fnod) {
    ////		addDescription(doc, feat.getProperty().getDescription(), fnod);
    ////		addProperties(doc, feat.getCustomProperties(), fnod);
    ////		writeAttributes(node, fnod, feat);
    ////	}
    ////
    ////	protected void addDescription(Document doc, String description, Element fnod) {
    ////		if ((description != null) && !description.trim().isEmpty()) {
    ////			final Element descr = doc.createElement(DESCRIPTION);
    ////			descr.setTextContent(description);
    ////			fnod.appendChild(descr);
    ////		}
    ////	}
    ////
    //	protected void addProperties(Document doc, de.featjar.model.Element element, Element fnod) {
    //		for (final Map.Entry<Attribute<?>, Object> property : element.getAttributeToValueMap().entrySet()) {
    //			final Element propNode;
    //			String namespace = property.getKey().getNamespace();
    //			if (GRAPHICS_NAMESPACE.equals(namespace)) {
    //				propNode = doc.createElement(GRAPHICS);
    //			} else if (CALCULATIONS_NAMESPACE.equals(namespace)) {
    //				propNode = doc.createElement(CALCULATIONS);
    //			} else {
    //				propNode = doc.createElement(PROPERTY);
    //				propNode.setAttribute(NAMESPACE_TAG, property.getKey().getNamespace());
    //				propNode.setAttribute(DATA_TYPE,
    // AttributeIO.getTypeString(property.getKey().getType()).orElseThrow(IllegalArgumentException::new)); // todo
    //			}
    //			propNode.setAttribute(KEY, property.getKey().getName());
    //			propNode.setAttribute(VALUE, property.getValue().toString()); // todo
    //			fnod.appendChild(propNode);
    //		}
    //	}
    ////
    ////	/**
    ////	 * Adds the tags of a constraint to the xml file
    ////	 */
    ////	private void addTags(Document doc, Set<String> tags, Element fnod) {
    ////		if ((tags != null) && !tags.isEmpty()) {
    ////			final Element tag = doc.createElement(TAGS);
    ////			String finalTags = "";
    ////			for (final String tagString : tags) {
    ////				if (finalTags.equals("")) {
    ////					finalTags += tagString;
    ////					continue;
    ////				}
    ////				finalTags += "," + tagString;
    ////			}
    ////			tag.setTextContent(finalTags);
    ////			fnod.appendChild(tag);
    ////		}
    ////	}
    ////
    ////	protected void writeAttributes(Element node, Element fnod, IFeature feat) {
    ////		fnod.setAttribute(NAME, feat.getName());
    ////		if (feat.getStructure().isHidden()) {
    ////			fnod.setAttribute(HIDDEN, TRUE);
    ////		}
    ////		if (feat.getStructure().isMandatory()) {
    ////			if ((feat.getStructure().getParent() != null) && feat.getStructure().getParent().isAnd()) {
    ////				fnod.setAttribute(MANDATORY, TRUE);
    ////			} else if (feat.getStructure().getParent() == null) {
    ////				fnod.setAttribute(MANDATORY, TRUE);
    ////			}
    ////		}
    ////		if (feat.getStructure().isAbstract()) {
    ////			fnod.setAttribute(ABSTRACT, TRUE);
    ////		}
    ////
    ////		node.appendChild(fnod);
    ////	}
}
