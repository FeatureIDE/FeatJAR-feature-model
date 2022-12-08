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

/**
 * Reads / Writes a feature model in the FeatureIDE XML format
 *
 * @author Jens Meinicke
 * @author Marcus Pinnecke
 * @author Sebastian Krieter
 * @author Marlen Bernier
 * @author Dawid Szczepanski
 * @deprecated
 */
public class OldXmlFeatureModelFormat {
    //	@Override
    //	protected void writeDocument(Document doc) {
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
    //	protected void writeProperties(Document doc, final Element root) {
    //		if (!object.getProperty().getProperties().isEmpty()) {
    //			final Element properties = doc.createElement(PROPERTIES);
    //			root.appendChild(properties);
    //			addProperties(doc, object.getProperty(), properties);
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
    //		if (!object.getConstraints().isEmpty()) {
    //			final Element constraints = doc.createElement(CONSTRAINTS);
    //			root.appendChild(constraints);
    //			for (final IConstraint constraint : object.getConstraints()) {
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
    //
    //	protected void writeComments(Document doc, final Element root) {
    //		if (!object.getProperty().getComments().isEmpty()) {
    //			final Element comments = doc.createElement(COMMENTS);
    //			root.appendChild(comments);
    //			for (final String comment : object.getProperty().getComments()) {
    //				final Element c = doc.createElement(C);
    //				comments.appendChild(c);
    //				final Text text = doc.createTextNode(comment);
    //				c.appendChild(text);
    //			}
    //		}
    //	}
    //
    //	protected void writeFeatureOrder(Document doc, final Element root) {
    //		if (object.isFeatureOrderUserDefined()) {
    //			final Element order = doc.createElement(FEATURE_ORDER);
    //			order.setAttribute(USER_DEFINED, Boolean.toString(object.isFeatureOrderUserDefined()));
    //			root.appendChild(order);
    //			Collection<String> featureOrderList = object.getFeatureOrderList();
    //
    //			if (featureOrderList.isEmpty()) {
    //				featureOrderList = FeatureUtils.extractConcreteFeaturesAsStringList(object);
    //			}
    //
    //			for (final String featureName : featureOrderList) {
    //				final Element feature = doc.createElement(FEATURE);
    //				feature.setAttribute(NAME, featureName);
    //				order.appendChild(feature);
    //			}
    //		}
    //	}
    //
    //	/**
    //	 * Inserts the tags concerning propositional constraints into the DOM document representation
    //	 *
    //	 * @param doc
    //	 * @param node Parent node for the propositional nodes
    //	 */
    //	protected void createPropositionalConstraints(Document doc, Element xmlNode, org.prop4j.Node node) {
    //		if (node == null) {
    //			return;
    //		}
    //
    //		final Element op;
    //		if (node instanceof Literal) {
    //			final Literal literal = (Literal) node;
    //			if (!literal.positive) {
    //				final Element opNot = doc.createElement(NOT);
    //				xmlNode.appendChild(opNot);
    //				xmlNode = opNot;
    //			}
    //			op = doc.createElement(VAR);
    //			op.appendChild(doc.createTextNode(String.valueOf(literal.var)));
    //			xmlNode.appendChild(op);
    //			return;
    //		} else if (node instanceof Or) {
    //			op = doc.createElement(DISJ);
    //		} else if (node instanceof Equals) {
    //			op = doc.createElement(EQ);
    //		} else if (node instanceof Implies) {
    //			op = doc.createElement(IMP);
    //		} else if (node instanceof And) {
    //			op = doc.createElement(CONJ);
    //		} else if (node instanceof Not) {
    //			op = doc.createElement(NOT);
    //		} else if (node instanceof AtMost) {
    //			op = doc.createElement(ATMOST1);
    //		} else {
    //			op = doc.createElement(UNKNOWN);
    //		}
    //		xmlNode.appendChild(op);
    //
    //		for (final org.prop4j.Node child : node.getChildren()) {
    //			createPropositionalConstraints(doc, op, child);
    //		}
    //	}
    //
    //	/**
    //	 * Creates document based on feature model step by step
    //	 *
    //	 * @param doc document to write
    //	 * @param node parent node
    //	 * @param feat current feature
    //	 */
    //	protected void writeFeatureTreeRec(Document doc, Element node, IFeature feat) {
    //		if (feat == null) {
    //			return;
    //		}
    //
    //		final List<IFeature> children = FeatureUtils.convertToFeatureList(feat.getStructure().getChildren());
    //
    //		final Element fnod;
    //		if (children.isEmpty()) {
    //			fnod = doc.createElement(FEATURE);
    //			writeFeatureProperties(doc, node, feat, fnod);
    //		} else {
    //			if (feat.getStructure().isAnd()) {
    //				fnod = doc.createElement(AND);
    //			} else if (feat.getStructure().isOr()) {
    //				fnod = doc.createElement(OR);
    //			} else if (feat.getStructure().isAlternative()) {
    //				fnod = doc.createElement(ALT);
    //			} else {
    //				fnod = doc.createElement(UNKNOWN);// Logger.logInfo("creatXMlDockRec: Unexpected error!");
    //			}
    //
    //			writeFeatureProperties(doc, node, feat, fnod);
    //
    //			for (final IFeature feature : children) {
    //				writeFeatureTreeRec(doc, fnod, feature);
    //			}
    //
    //		}
    //
    //	}
    //
    //	protected void writeFeatureProperties(Document doc, Element node, IFeature feat, final Element fnod) {
    //		addDescription(doc, feat.getProperty().getDescription(), fnod);
    //		addProperties(doc, feat.getCustomProperties(), fnod);
    //		writeAttributes(node, fnod, feat);
    //	}
    //
    //	protected void addDescription(Document doc, String description, Element fnod) {
    //		if ((description != null) && !description.trim().isEmpty()) {
    //			final Element descr = doc.createElement(DESCRIPTION);
    //			descr.setTextContent(description);
    //			fnod.appendChild(descr);
    //		}
    //	}
    //
    //	protected void addProperties(Document doc, IPropertyContainer properties, Element fnod) {
    //		for (final Entry property : properties.getProperties()) {
    //			if (property.getValue() != null) {
    //				final Element propNode;
    //				if (GRAPHICS.equals(property.getType())) {
    //					propNode = doc.createElement(GRAPHICS);
    //				} else if (CALCULATIONS.equals(property.getType())) {
    //					propNode = doc.createElement(CALCULATIONS);
    //				} else {
    //					propNode = doc.createElement(PROPERTY);
    //					propNode.setAttribute(TYPE, property.getType());
    //				}
    //				propNode.setAttribute(KEY, property.getKey());
    //				propNode.setAttribute(VALUE, property.getValue());
    //				fnod.appendChild(propNode);
    //			}
    //		}
    //	}
    //
    //	/**
    //	 * Adds the tags of a constraint to the xml file
    //	 */
    //	private void addTags(Document doc, Set<String> tags, Element fnod) {
    //		if ((tags != null) && !tags.isEmpty()) {
    //			final Element tag = doc.createElement(TAGS);
    //			String finalTags = "";
    //			for (final String tagString : tags) {
    //				if (finalTags.equals("")) {
    //					finalTags += tagString;
    //					continue;
    //				}
    //				finalTags += "," + tagString;
    //			}
    //			tag.setTextContent(finalTags);
    //			fnod.appendChild(tag);
    //		}
    //	}
    //
    //	protected void writeAttributes(Element node, Element fnod, IFeature feat) {
    //		fnod.setAttribute(NAME, feat.getName());
    //		if (feat.getStructure().isHidden()) {
    //			fnod.setAttribute(HIDDEN, TRUE);
    //		}
    //		if (feat.getStructure().isMandatory()) {
    //			if ((feat.getStructure().getParent() != null) && feat.getStructure().getParent().isAnd()) {
    //				fnod.setAttribute(MANDATORY, TRUE);
    //			} else if (feat.getStructure().getParent() == null) {
    //				fnod.setAttribute(MANDATORY, TRUE);
    //			}
    //		}
    //		if (feat.getStructure().isAbstract()) {
    //			fnod.setAttribute(ABSTRACT, TRUE);
    //		}
    //
    //		node.appendChild(fnod);
    //	}

}
