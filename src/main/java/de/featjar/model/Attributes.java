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
package de.featjar.model;

import de.featjar.model.util.Attribute;
import de.featjar.model.util.Identifiable;

/**
 * Defines some useful {@link Attribute attributes} for {@link FeatureModel
 * feature models}, {@link Feature features}, and {@link Constraint
 * constraints}.
 *
 * @author Elias Kuiter
 */
public class Attributes {
    public static final String NAMESPACE = Attributes.class.getCanonicalName();
    public static final Attribute.WithDefaultValue<String> NAME = new Attribute.WithDefaultValue<String>(
            NAMESPACE,
            "name",
            String.class,
            identifiable -> "@" + ((Identifiable) identifiable).getIdentifier().toString());
    public static final Attribute<String> DESCRIPTION = new Attribute<>(NAMESPACE, "description", String.class);
    public static final Attribute.Set<String> TAGS = new Attribute.Set<>(NAMESPACE, "tags");
    public static final Attribute.WithDefaultValue<Boolean> HIDDEN =
            new Attribute.WithDefaultValue<>(NAMESPACE, "hidden", Boolean.class, false);
    public static final Attribute.WithDefaultValue<Boolean> ABSTRACT =
            new Attribute.WithDefaultValue<>(NAMESPACE, "abstract", Boolean.class, false);
}
