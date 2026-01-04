package com.watsonllc.chestlock.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HopperDecisionKey {
        private final List<BlockKey> source;
        private final List<BlockKey> destination;
        private final String initiatorId;

        public HopperDecisionKey(Collection<BlockKey> source, Collection<BlockKey> destination, String initiatorId) {
                this.source = normalize(source);
                this.destination = normalize(destination);
                this.initiatorId = initiatorId;
        }

        private List<BlockKey> normalize(Collection<BlockKey> keys) {
                if (keys == null || keys.isEmpty())
                        return Collections.emptyList();

                return keys.stream().filter(Objects::nonNull).sorted().collect(Collectors.toList());
        }

        public List<BlockKey> getSource() {
                return new ArrayList<>(source);
        }

        public List<BlockKey> getDestination() {
                return new ArrayList<>(destination);
        }

        public String getInitiatorId() {
                return initiatorId;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o)
                        return true;
                if (!(o instanceof HopperDecisionKey))
                        return false;
                HopperDecisionKey that = (HopperDecisionKey) o;
                return Objects.equals(source, that.source) && Objects.equals(destination, that.destination) && Objects.equals(initiatorId, that.initiatorId);
        }

        @Override
        public int hashCode() {
                return Objects.hash(source, destination, initiatorId);
        }
}
