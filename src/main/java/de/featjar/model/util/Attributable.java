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
package de.featjar.model.util;

import de.featjar.model.Constraint;
import de.featjar.model.Feature;
import de.featjar.model.FeatureModel;
import java.util.Map;
import java.util.Optional;

/**
 * An object that can be annotated with {@link Attribute} values. For example,
 * attributes are used in {@link FeatureModel feature models}, {@link Feature
 * features}, and {@link Constraint constraints} to store additional metadata.
 *
 * @author Elias Kuiter
 */
public interface Attributable {
    Map<Attribute<?>, Object> getAttributeToValueMap();

    default <T> Optional<T> getAttributeValue(Attribute<T> attribute) {
        return attribute.apply(getAttributeToValueMap());
    }

    default <T> T getAttributeValue(Attribute.WithDefaultValue<T> attribute) {
        return attribute.applyWithDefaultValue(getAttributeToValueMap(), this);
    }

    default boolean hasAttributeValue(Attribute<?> attribute) {
        return getAttributeValue(attribute).isPresent();
    }

    interface Mutator<T extends Attributable> extends de.featjar.model.util.Mutator<T> {
        default <U> void setAttributeValue(Attribute<U> attribute, U value) {
            if (value == null) removeAttributeValue(attribute);
            else getMutable().getAttributeToValueMap().put(attribute, value);
        }

        default void setArbitraryAttributeValue(Attribute<?> attribute, Object value) {
            if (!attribute.getType().equals(value.getClass()))
                throw new IllegalArgumentException("cannot set attribute of type " + attribute.getType()
                        + " to value of type " + value.getClass());
        }

        default <U> Object removeAttributeValue(Attribute<U> attribute) {
            return getMutable().getAttributeToValueMap().remove(attribute);
        }

        default boolean toggleAttributeValue(Attribute.WithDefaultValue<Boolean> attribute) {
            boolean value = getMutable().getAttributeValue(attribute);
            setAttributeValue(attribute, !value);
            return !value;
        }
    }
}
