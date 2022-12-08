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
package de.featjar.model.mixins;

import de.featjar.model.Attributes;
import de.featjar.model.Constraint;
import de.featjar.model.Feature;
import de.featjar.model.FeatureModel;
import de.featjar.model.util.Attributable;
import de.featjar.model.util.Attribute;
import java.util.Optional;

/**
 * Implements accessors for commonly used {@link Attribute attributes}. For
 * example, all {@link FeatureModel feature models}, {@link Feature features},
 * and {@link Constraint constraints} can have names and descriptions.
 *
 * @author Elias Kuiter
 */
public interface CommonAttributesMixin extends Attributable {
    default String getName() {
        return getAttributeValue(Attributes.NAME);
    }

    default Optional<String> getDescription() {
        return getAttributeValue(Attributes.DESCRIPTION);
    }

    interface Mutator<T extends Attributable> extends Attributable.Mutator<T> {
        default void setName(String name) {
            setAttributeValue(Attributes.NAME, name);
        }

        default void setDescription(String description) {
            setAttributeValue(Attributes.DESCRIPTION, description);
        }
    }
}
