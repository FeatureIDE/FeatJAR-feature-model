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

import de.featjar.model.Feature;
import de.featjar.model.FeatureModel;
import de.featjar.model.FeatureOrder;
import java.util.List;

/**
 * Implements a {@link FeatureModel} mixin for considering a
 * {@link FeatureOrder}.
 *
 * @author Elias Kuiter
 */
public interface FeatureModelFeatureOrderMixin extends FeatureModelFeatureTreeMixin {
    FeatureOrder getFeatureOrder();

    default List<Feature> getOrderedFeatures() {
        return getFeatureOrder().apply(this);
    }

    interface Mutator {
        void setFeatureOrder(FeatureOrder featureOrder);
    }
}
