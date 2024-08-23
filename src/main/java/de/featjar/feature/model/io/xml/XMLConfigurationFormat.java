package de.featjar.feature.model.io.xml;

import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.xml.AXMLFormat;
import de.featjar.feature.model.Configuration;
import de.featjar.feature.model.ConfigurationEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.regex.Pattern;

/**
 * Parses and writes configurations from and to XML files.
 *
 * @author Andreas Gerasimow
 */
public class XMLConfigurationFormat extends AXMLFormat<Configuration> {

    public static final String ROOT_TAG = "configuration";
    public static final String NODE_FEATURE = "feature";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_SELECTED = "selected";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_VALUE = "value";

    private ConfigurationEntry.SelectedStatus getSelectedStatus(String name) throws ParseException {
        if (name.equals(ConfigurationEntry.SelectedStatus.UNSELECTED.getName())) {
            return ConfigurationEntry.SelectedStatus.UNSELECTED;
        } else if (name.equals(ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED.getName())) {
            return ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED;
        } else if (name.equals(ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED.getName())) {
            return ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED;
        } else {
            throw new ParseException("selectedStatus " + name + " does not exist");
        }
    }

    @Override
    protected Configuration parseDocument(Document document) throws ParseException {
        Configuration cfg = new Configuration();

        Element root = document.getDocumentElement();
        if (!root.getTagName().equals(ROOT_TAG)) {
            throw new ParseException("Root element must be <" + ROOT_TAG + ">");
        }

        for (Element feature : getElements(root.getElementsByTagName(NODE_FEATURE))) {
            if (!feature.hasAttribute(ATTRIBUTE_NAME)) {
                throw new ParseException("Feature element must have attribute <" + ATTRIBUTE_NAME + ">");
            }
            if (!feature.hasAttribute(ATTRIBUTE_VALUE)) {
                throw new ParseException("Feature element must have attribute <" + ATTRIBUTE_VALUE + ">");
            }
            if (!feature.hasAttribute(ATTRIBUTE_TYPE)) {
                throw new ParseException("Feature element must have attribute <" + ATTRIBUTE_TYPE + ">");
            }
            if (!feature.hasAttribute(ATTRIBUTE_SELECTED)) {
                throw new ParseException("Feature element must have attribute <" + ATTRIBUTE_SELECTED + ">");
            }

            try {

                cfg.addEntry(new ConfigurationEntry(
                        feature.getAttribute(ATTRIBUTE_NAME),
                        feature.getAttribute(ATTRIBUTE_VALUE),
                        Class.forName(feature.getAttribute(ATTRIBUTE_TYPE)),
                        getSelectedStatus(feature.getAttribute(ATTRIBUTE_SELECTED))
                ));
            } catch (ClassNotFoundException e) {
                throw new ParseException("Class " + feature.getAttribute(ATTRIBUTE_TYPE) + " not found");
            }
        }

        return cfg;
    }

    @Override
    protected void writeDocument(Configuration object, Document doc) {
        final Element root = doc.createElement(ROOT_TAG);
        doc.appendChild(root);
        for (final ConfigurationEntry entry : object.getConfigurationEntries()) {
            final Element featureNode = doc.createElement(NODE_FEATURE);
            featureNode.setAttribute(ATTRIBUTE_NAME, entry.getFeatureName());
            featureNode.setAttribute(ATTRIBUTE_SELECTED, entry.getSelectedStatus().getName());
            featureNode.setAttribute(ATTRIBUTE_TYPE, entry.getType().getName());
            featureNode.setAttribute(ATTRIBUTE_VALUE, entry.getValue().toString());
            root.appendChild(featureNode);

        }
    }

    @Override
    protected Pattern getInputHeaderPattern() {
        return Pattern.compile("");
    }

    @Override
    public String getName() {
        return "Configuration";
    }
}

// https://github.com/FeatureIDE/FeatureIDE/blob/develop/plugins/de.ovgu.featureide.fm.core/src/de/ovgu/featureide/fm/core/configuration/XMLConfFormat.java
