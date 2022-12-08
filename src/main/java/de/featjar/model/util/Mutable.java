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

import java.util.function.Consumer;

/**
 * An object that can be mutated (i.e., changed) with a {@link Mutator}. While
 * not strictly necessary, this helps to distinguish safe mutation (via a
 * mutator) from potentially unsafe mutation (via immediate API members). To
 * mutate an object o, call o.mutate(). To mutate a copy of o, call
 * o.clone().mutate().
 *
 * @param <T> the type of the mutable object
 * @param <U> the type of the mutator object
 * @author Elias Kuiter
 */
public interface Mutable<T, U extends Mutator<T>> {
    U getMutator();

    void setMutator(U mutator);

    default void finishInternalMutation() {}

    default U mutate() {
        return getMutator();
    }

    default T mutate(Consumer<U> mutatorConsumer) {
        mutatorConsumer.accept(getMutator());
        return getMutator().getMutable();
    }

    default void mutateInternal(Runnable r) {
        try {
            r.run();
        } finally {
            finishInternalMutation();
        }
    }
}
