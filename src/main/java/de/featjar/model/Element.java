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

import de.featjar.model.util.Attributable;
import de.featjar.model.util.Attribute;
import de.featjar.model.util.Identifiable;
import de.featjar.model.util.Identifier;
import java.util.*;

/**
 * Implements identification and attribute valuation. Each {@link FeatureModel}
 * and all its {@link Feature features} and {@link Constraint constraints} are
 * uniquely identified by an {@link Identifier}. Also, each element can be
 * annotated with arbirtrary {@link Attribute attributes}.
 *
 * @author Elias Kuiter
 */
public abstract class Element implements Identifiable, Attributable {
    protected final Identifier identifier;
    protected final Map<Attribute<?>, Object> attributeToValueMap = new HashMap<>();

    public Element(Identifier identifier) {
        Objects.requireNonNull(identifier);
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public Map<Attribute<?>, Object> getAttributeToValueMap() {
        return attributeToValueMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element that = (Element) o;
        return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier());
    }
}
