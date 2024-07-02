package de.featjar.feature.model.io;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.feature.model.Constraint;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.mixins.IHasCommonAttributes;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.formula.IFormula;
import de.featjar.formula.structure.formula.connective.And;
import de.featjar.formula.structure.formula.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;

public class XmlFeatureModelFormat implements IFormat<IFeatureModel> {

    @Override
    public Result<String> serialize(IFeatureModel featureModel) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("featureModel");
            doc.appendChild(rootElement);

            Element propertiesElement = doc.createElement("properties");
            rootElement.appendChild(propertiesElement);
            serializeProperties(doc, propertiesElement, featureModel);

            Element structElement = doc.createElement("struct");
            rootElement.appendChild(structElement);
            serializeStruct(doc, structElement, featureModel);

            Element constraintsElement = doc.createElement("constraints");
            rootElement.appendChild(constraintsElement);
            serializeConstraints(doc, constraintsElement, featureModel.getConstraints());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            return Result.of(writer.toString());
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    private void serializeProperties(Document doc, Element parent, IFeatureModel featureModel) {
        featureModel.getAttributes().ifPresent(attributes -> {
            attributes.forEach((key, value) -> {
                Element propertyElement = doc.createElement(key.getNamespace());
                propertyElement.setAttribute("key", key.getName());
                propertyElement.setAttribute("value", value.toString());
                parent.appendChild(propertyElement);
            });
        });
    }

    private void serializeStruct(Document doc, Element parent, IFeatureModel featureModel) {
        for (IFeatureTree root : featureModel.getRoots()) {
            serializeFeatureTree(doc, parent, root);
        }
    }

    private void serializeFeatureTree(Document doc, Element parent, IFeatureTree tree) {
        Element andElement = doc.createElement("and");
        andElement.setAttribute("mandatory", "true");
        andElement.setAttribute("name", tree.getFeature().getName().get());
        parent.appendChild(andElement);

        for (IFeatureTree child : tree.getChildren()) {
            Element featureElement = doc.createElement("feature");
            featureElement.setAttribute("name", ((IHasCommonAttributes) child).getName().get());
            andElement.appendChild(featureElement);
        }
    }

    private void serializeConstraints(Document doc, Element parent, Collection<IConstraint> constraints) {
        for (IConstraint constraint : constraints) {
            Element ruleElement = doc.createElement("rule");
            serializeFormula(doc, ruleElement, constraint.getFormula());
            parent.appendChild(ruleElement);
        }
    }

    private void serializeFormula(Document doc, Element parent, IFormula formula) {
        if (formula instanceof And) {
            Element conjElement = doc.createElement("conj");
            for (IExpression child : ((And) formula).getChildren()) {
                serializeFormula(doc, conjElement, (IFormula) child);
            }
            parent.appendChild(conjElement);
        } else if (formula instanceof Literal) {
            Literal literal = (Literal) formula;
            Element varElement = doc.createElement("var");
            varElement.setTextContent(((Variable) literal.getExpression()).getName());
            parent.appendChild(varElement);
        }
    }

    @Override
    public Result<IFeatureModel> parse(AInputMapper inputMapper) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputMapper.get().getInputStream());

            FeatureModel featureModel = new FeatureModel();

            // Parse properties
            NodeList propertiesNodes = doc.getElementsByTagName("properties");
            if (propertiesNodes.getLength() > 0) {
                Element propertiesElement = (Element) propertiesNodes.item(0);
                parseProperties(propertiesElement, featureModel);
            }

            // Parse features
            NodeList structNodes = doc.getElementsByTagName("struct");
            if (structNodes.getLength() > 0) {
                Element structElement = (Element) structNodes.item(0);
                parseStruct(structElement, featureModel);
            }

            // Parse constraints
            NodeList constraintsNodes = doc.getElementsByTagName("constraints");
            if (constraintsNodes.getLength() > 0) {
                Element constraintsElement = (Element) constraintsNodes.item(0);
                parseConstraints(constraintsElement, featureModel);
            }

            return Result.of(featureModel);
        } catch (Exception e) {
            return Result.empty(new ParseException(e.getMessage(), 0));
        }
    }

    private void parseProperties(Element parent, FeatureModel featureModel) {
        NodeList calculations = parent.getElementsByTagName("calculations");
        for (int i = 0; i < calculations.getLength(); i++) {
            Element property = (Element) calculations.item(i);
            String key = property.getAttribute("key");
            String value = property.getAttribute("value");
            featureModel.setAttributeValue(new Attribute<>(property.getTagName(), key, String.class), value);
        }

        NodeList graphics = parent.getElementsByTagName("graphics");
        for (int i = 0; i < graphics.getLength(); i++) {
            Element property = (Element) graphics.item(i);
            String key = property.getAttribute("key");
            String value = property.getAttribute("value");
            featureModel.setAttributeValue(new Attribute<>(property.getTagName(), key, String.class), value);
        }
    }

    private void parseStruct(Element parent, FeatureModel featureModel) {
        NodeList andNodes = parent.getElementsByTagName("and");
        for (int i = 0; i < andNodes.getLength(); i++) {
            Element andElement = (Element) andNodes.item(i);
            String name = andElement.getAttribute("name");
            boolean mandatory = Boolean.parseBoolean(andElement.getAttribute("mandatory"));
            IFeature rootFeature = featureModel.addFeature(name);
            IFeatureTree rootTree = featureModel.addFeatureTreeRoot(rootFeature);
            rootTree.setMandatory(mandatory);

            NodeList featureNodes = andElement.getElementsByTagName("feature");
            for (int j = 0; j < featureNodes.getLength(); j++) {
                Element featureElement = (Element) featureNodes.item(j);
                String featureName = featureElement.getAttribute("name");
                featureModel.addFeature(featureName);
            }
        }
    }

    private void parseConstraints(Element parent, FeatureModel featureModel) {
        NodeList ruleNodes = parent.getElementsByTagName("rule");
        for (int i = 0; i < ruleNodes.getLength(); i++) {
            Element ruleElement = (Element) ruleNodes.item(i);
            IFormula formula = parseFormula(ruleElement);
            featureModel.addConstraint(new Constraint(featureModel, formula));
        }
    }

    private IFormula parseFormula(Element parent) {
        NodeList conjNodes = parent.getElementsByTagName("conj");
        if (conjNodes.getLength() > 0) {
            Element conjElement = (Element) conjNodes.item(0);
            NodeList varNodes = conjElement.getElementsByTagName("var");
            List<Literal> literals = new ArrayList<>();
            for (int i = 0; i < varNodes.getLength(); i++) {
                Element varElement = (Element) varNodes.item(i);
                String varName = varElement.getTextContent();
                literals.add(new Literal(new Variable(varName), true));
            }
            return new And(literals.toArray(new Literal[0]));
        }
        return null; // Adjust according to your formula handling logic
    }

    @Override
    public boolean supportsSerialize() {
        return true;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public String getName() {
        return "XML Feature Model Format";
    }
}
