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

import de.featjar.model.Feature;
import de.featjar.model.FeatureModel;
import de.featjar.model.FeatureTree;
import de.featjar.util.io.IO;
import de.featjar.util.io.format.Format;
import de.featjar.util.tree.Trees;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes feature models to GraphViz DOT files.
 *
 * @author Elias Kuiter
 */
public class GraphVizFeatureModelFormat implements Format<FeatureModel> {
    @Override
    public GraphVizFeatureModelFormat getInstance() {
        return new GraphVizFeatureModelFormat();
    }

    public static void openInBrowser(FeatureModel featureModel) {
        try {
            String dot = IO.print(featureModel, new GraphVizFeatureModelFormat());
            URI uri = new URI("https", "edotor.net", "", "engine=dot", dot);
            Desktop.getDesktop().browse(uri);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFileExtension() {
        return "dot";
    }

    @Override
    public String getName() {
        return "GraphViz";
    }

    @Override
    public boolean supportsSerialize() {
        return true;
    }

    @Override
    public String serialize(FeatureModel featureModel) {
        List<Feature> features = Trees.getLevelOrderList(featureModel.getFeatureTree()).stream()
                .map(FeatureTree::getFeature)
                .collect(Collectors.toList());
        return String.format(
                "digraph {\n  graph%s;\n  node%s;\n  edge%s;\n%s\n%s\n}",
                options(option("splines", "false"), option("ranksep", "0.2")),
                options(
                        option("fontname", "Arial"),
                        option("style", "filled"),
                        option("fillcolor", "#ccccff"),
                        option("shape", "box")),
                options(option("arrowhead", "none")),
                features.stream().map(this::getNode).collect(Collectors.joining("\n")),
                features.stream().map(this::getEdge).filter(s -> !s.isEmpty()).collect(Collectors.joining("\n")));
    }

    public String getNode(Feature feature) {
        String nodeString = "";
        nodeString += String.format(
                "  %s%s;",
                quote(feature.getIdentifier().toString()),
                options(
                        option("label", feature.getName()),
                        option("fillcolor", feature.isAbstract() ? "#f2f2ff" : null)));
        nodeString += String.format(
                "\n  %s%s;",
                quote(feature.getIdentifier().toString() + "_group"),
                options(
                        option("shape", "diamond"),
                        option(
                                "style",
                                !feature.getFeatureTree().isGroup()
                                        ? "invis"
                                        : feature.getFeatureTree().isAlternative() ? "" : null),
                        option("fillcolor", feature.getFeatureTree().isOr() ? "#000000" : null),
                        option("label", ""),
                        option("width", ".15"),
                        option("height", ".15")));
        return nodeString;
    }

    public String getEdge(Feature feature) {
        String edgeString = "";
        if (feature.getFeatureTree().hasParent()) {
            String parentNode = feature.getFeatureTree()
                    .getParent()
                    .get()
                    .getFeature()
                    .getIdentifier()
                    .toString();
            edgeString += getEdge(
                    parentNode + "_group",
                    feature,
                    option("style", feature.getFeatureTree().getParent().get().isGroup() ? null : "invis"));
            if (!feature.getFeatureTree().getParent().get().isGroup())
                edgeString += getEdge(
                        parentNode + (feature.getFeatureTree().getParent().get().isGroup() ? "_group" : ""),
                        feature,
                        "");
        }
        edgeString += String.format(
                "  %s:s -> %s:n%s;",
                quote(feature.getIdentifier().toString()),
                quote(feature.getIdentifier().toString() + "_group"),
                options(option("style", feature.getFeatureTree().isGroup() ? null : "invis")));
        return edgeString;
    }

    public String getEdge(String parentNode, Feature childFeature, String option) {
        return String.format(
                "  %s:s -> %s:n%s;\n",
                quote(parentNode),
                quote(childFeature.getIdentifier().toString()),
                options(
                        option(
                                "arrowhead",
                                childFeature.getFeatureTree().getParent().get().isGroup()
                                        ? null
                                        : childFeature.getFeatureTree().isMandatory() ? "dot" : "odot"),
                        option));
    }

    protected String quote(String str) {
        return String.format("\"%s\"", str.replace("\"", "\\\""));
    }

    protected String options(String... options) {
        List<String> optionsList =
                Arrays.stream(options).filter(o -> !o.isEmpty()).collect(Collectors.toList());
        if (String.join("", optionsList).trim().isEmpty()) return "";
        return String.format(" [%s]", String.join(" ", optionsList));
    }

    protected String option(String name, String value) {
        return value != null ? String.format("%s=%s", name, quote(value)) : "";
    }
}
